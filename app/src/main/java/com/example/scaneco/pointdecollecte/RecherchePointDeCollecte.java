package com.example.scaneco.pointdecollecte;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;


import com.example.scaneco.MainActivity;
import com.example.scaneco.R;
import com.example.scaneco.animations.AccueilAnimations;
import com.example.scaneco.horrampoubelles.AccueilHorRamPoubelles;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

/// TODO à finir !!!

public class RecherchePointDeCollecte extends AppCompatActivity implements LocationListener {

    private MapView osm;
    private MapController mc;
    List<PointDeCollecte> listePointsDeCollecte;
    private static final int PERMISSAO_REQUERIDA = 1;
    Marker markerPosition;
    ArrayList<Marker> listeMarkerPoubelleNoire = new ArrayList<>();
    ArrayList<Marker> listeMarkerPoubelleJaune = new ArrayList<>();
    ArrayList<Marker> listeMarkerPoubelleVerte = new ArrayList<>();
    ArrayList<Marker> listeMarkerPoubelleBleue = new ArrayList<>();
    ArrayList<Marker> listeMarkerDechetterie = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recherche_point_de_collecte);

        //onde mostra a imagem do mapa
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setOsm(findViewById(R.id.mapView));
        getOsm().setTileSource(TileSourceFactory.MAPNIK);
        getOsm().setMultiTouchControls(true);

        MapListener mapListener = new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                Log.i("Script", "onScroll()");
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                Log.i("Script", "onZoom()");
                return false;
            }
        };
        mc = (MapController) getOsm().getController();
        markerPosition = new Marker(getOsm());

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    1
            );
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        getOsm().addMapListener(mapListener);
        ///////////////Recuperation de la BD en JSON\\\\\\\\\\\\\\\
        ConnexionJsonPointDeCollecte baseDeDonneesPdtCollectes = new ConnexionJsonPointDeCollecte(this);
        try {
            //TODO deprecated async task
            baseDeDonneesPdtCollectes.execute("https://api.npoint.io/6696673b4c1fdcfcff8e");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Initialisation de la barre de menu
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item ->{
            int itemId = item.getItemId();
            if (itemId == R.id.accueilHorRamPoubelles) {
                ouvrirHorRamPoubelles();
            } else if (itemId == R.id.accueilAnimations) {
                ouvrirAnimations();
            }
            return true;
        });
        //Bouton de retour
        ImageButton boutonRetourScan = findViewById(R.id.boutonRetourScan);
        boutonRetourScan.setOnClickListener(v -> ouvrirLeScan());

    }

    @Override
    //TODO à changer car deprecated
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (
            requestCode == PERMISSAO_REQUERIDA &&
            grantResults.length > 0 &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {// Se a solicitação de permissão foi cancelada o array vem vazio.
            // Permissão cedida, recria a activity para carregar o mapa, só será executado uma vez
            this.recreate();
        }
    }

    @Override
    public void onResume() {
        getOsm().onResume();
        super.onResume();

    }


    @Override
    public void onPause() {
        getOsm().onPause();
        super.onPause();
    }



    @Override
    public void onLocationChanged(Location location) {
        getOsm().getOverlays().remove(markerPosition);
        GeoPoint point = new GeoPoint(location.getLatitude(),location.getLongitude());
        markerPosition.setPosition(point);

        mc.setZoom(16);
        mc.animateTo(point);
        getOsm().getOverlays().add(markerPosition);

    }
    @Override
    public void onProviderEnabled(String provider) {
        //Nothing because not needed
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Nothing because not needed
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        int id = view.getId();
        if (id == R.id.check1) {
            if (checked) {
                for (Marker pdt : listeMarkerPoubelleNoire) {
                    getOsm().getOverlays().add(pdt);
                }
            } else {
                for (Marker pdt : listeMarkerPoubelleNoire) {
                    getOsm().getOverlays().remove(pdt);
                }
            }
        } else if (id == R.id.check2) {
            if (checked) {
                for (Marker pdt : listeMarkerPoubelleJaune) {
                    getOsm().getOverlays().add(pdt);
                }
            } else {
                for (Marker pdt : listeMarkerPoubelleJaune) {
                    getOsm().getOverlays().remove(pdt);
                }
            }
        } else if (id == R.id.check3) {
            if (checked) {
                for (Marker pdt : listeMarkerPoubelleVerte) {
                    getOsm().getOverlays().add(pdt);
                }
            } else {
                for (Marker pdt : listeMarkerPoubelleVerte) {
                    getOsm().getOverlays().remove(pdt);
                }
            }
        } else if (id == R.id.check4) {
            if (checked) {
                for (Marker pdt : listeMarkerPoubelleBleue) {
                    getOsm().getOverlays().add(pdt);
                }
            } else {
                for (Marker pdt : listeMarkerPoubelleBleue) {
                    getOsm().getOverlays().remove(pdt);
                }
            }
        } else if (id == R.id.check5) {
            if (checked) {
                for (Marker pdt : listeMarkerDechetterie) {
                    getOsm().getOverlays().add(pdt);
                }
            } else {
                for (Marker pdt : listeMarkerDechetterie) {
                    getOsm().getOverlays().remove(pdt);
                }
            }
        }
    }

    public void json(String fichierJson) {
        try {
            listePointsDeCollecte = JsonPointDeCollecte.valeurRenvoyeeJson(fichierJson);

        } catch (Exception e) {
            listePointsDeCollecte = new ArrayList<>();
        }
        for (int i = 0; i < listePointsDeCollecte.size(); i++) {
            Marker m = new Marker(getOsm());
            GeoPoint coordonnes = new GeoPoint(listePointsDeCollecte.get(i).getLongitude(), listePointsDeCollecte.get(i).getLatitude());
            m.setPosition(coordonnes);
            m.setTitle(listePointsDeCollecte.get(i).getAdresse() +"-"+ listePointsDeCollecte.get(0).getVille());
            if ("Noire".equals(listePointsDeCollecte.get(i).getType())) {
                m.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_ping_noire));
                listeMarkerPoubelleNoire.add(m);
            } else if ("Jaune".equals(listePointsDeCollecte.get(i).getType())) {
                m.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_ping_jaune));
                listeMarkerPoubelleJaune.add(m);
            } else if ("Verte".equals(listePointsDeCollecte.get(i).getType())) {
                m.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_ping_verre));
                listeMarkerPoubelleVerte.add(m);
            } else if ("Bleue".equals(listePointsDeCollecte.get(i).getType())) {
                m.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_ping_bleue));
                listeMarkerPoubelleBleue.add(m);
            } else if ("Dechetterie".equals(listePointsDeCollecte.get(i).getType())) {
                m.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_ping_dechetterie));
                listeMarkerDechetterie.add(m);
            }
        }}

    /**
     * Fonction permettant d'aller sur une autre page(vue)
     * Ici cette fonction permettra d'accéder aux Animations
     **/
    public void ouvrirAnimations()
    {
        Intent intent = new Intent(this, AccueilAnimations.class);
        startActivity(intent);
    }

    /**
     * Fonction permettant d'aller sur une autre page(vue)
     * Ici cette fonction permettra d'accéder à la page des horaires de ramassage des poubelles
     **/
    public void ouvrirHorRamPoubelles()
    {
        Intent intent = new Intent(this, AccueilHorRamPoubelles.class);
        startActivity(intent);
    }

    protected void ouvrirLeScan()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public MapView getOsm() {
        return osm;
    }

    public void setOsm(MapView osm) {
        this.osm = osm;
    }
}

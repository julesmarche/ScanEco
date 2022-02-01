package com.example.scaneco;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.example.scaneco.animations.AccueilAnimations;
import com.example.scaneco.horRamPoubelles.AccueilHorRamPoubelles;
import com.example.scaneco.recherchesansscan.AccueilRechercheSansScan;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.Result;


public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    private TextView _nomProduit;
    private CodeScanner _mCodeScanner;
    private CodeScannerView _mCodeScannerView;
    private ImageButton _boutonRechercheSansScan;
    private ImageView _imageEmballage;
    private TextView _marqueProduit;
    private GestureDetector _gestureUtilisateur;
    private float y1,y2;
    private static int MIN_DISTANCE = 150;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Création et initialisation du scanner
        _mCodeScannerView = findViewById(R.id.scanner_view);
        _mCodeScanner = new CodeScanner(this, _mCodeScannerView);

        //Initialisation des variables qui nous permettront d'affocher les
        //caracteristiques du produit
        _nomProduit = findViewById(R.id.textView);
        _imageEmballage = findViewById(R.id.imageView_EmballageScan);
        _marqueProduit = findViewById(R.id.textView_marqueProduit);

        //Initialisation du swipe de l'utilisateur
        this._gestureUtilisateur = new GestureDetector(MainActivity.this,this);

        //Initialisation de la barre de menu
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item ->{
            switch (item.getItemId()){
                case R.id.accueilHorRamPoubelles:
                    ouvrirHorRamPoubelles();
                    break;

                case R.id.accueilAnimations:
                    ouvrirAnimations();
                    break;
            }
            return true;
        });


        //Initialisation du bouton qui ouvre la page recherche sans scan
        _boutonRechercheSansScan = findViewById(R.id.bouton1);
        //Quand le bouton est cliqué alors il sera redirigé vers la page recherche sans scan
        _boutonRechercheSansScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ouvrirRechercheSansScan();
            }
        });


        //Pour se servir du scan, l'utilisateur doit autoriser l'accès à la caméra
        //On vérifie s'il a autorisé ou non l'accès à la caméra
        //S'il a refusé on redemande si on peut l'utilser, mais on ne peut pas se servir du scan
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 123);
        }
        //S'il accepte, on lance le scan
        else{
            startScanning();
        }
    }


    /**
     * Fonction qui permet de scanner des codes-barres
     * Le carré blanc apparait au premier scan
     * Attention ! Il le carré blanc restera tant que l'utilisateur n'a pas changé de page(appelé vue)
     */

    private void startScanning() {
        //Au départ quand aucun article n'est scanné le texte n'apparait pas
        //Donc on lui demande d'être invisible
        _nomProduit.setVisibility(View.INVISIBLE);
        _marqueProduit.setVisibility(View.INVISIBLE);
        _mCodeScanner.startPreview();

        //Le scan décode un code-barres
        _mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //On récupère les chiffres scannés
                        String _nomProduitRecupere;
                        String _marqueProduitRecupere;
                        try {
                            Produit _produitObtenu = Produit.getProductFromBarCode(result.getText());
                            _nomProduitRecupere = _produitObtenu.getNom();
                            _marqueProduitRecupere = _produitObtenu.getMarque();

                            _produitObtenu.loadImage();
                            _imageEmballage.setImageDrawable(_produitObtenu.getImage());

                        }
                        catch (Exception e){
                            _marqueProduitRecupere = null;
                            _nomProduitRecupere = "Erreur, le produit n'est pas répertoriée dans la base de données OpenFoodFacts";
                        }
                        MainActivity.this._nomProduit.setText(_nomProduitRecupere);
                        MainActivity.this._marqueProduit.setText(_marqueProduitRecupere);
                        //On les affiche en rendant le texte visible
                        MainActivity.this._nomProduit.setVisibility(View.VISIBLE);
                        MainActivity.this._marqueProduit.setVisibility(View.VISIBLE);
                        _mCodeScanner.startPreview();
                    }
                });
            }
        });

    }

    /**
     * Fonction qui vérifie si on a la permission d'acceder à la caméra
     * La fonction s'active au premier lancement de l'application
     * Elle s'active ensuite à chaque fois qu'on arrive sur le scan et qu'on n'a toujours pas la permission
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //Un petit message lui indique que c'est bon, et on lance le scan
                Toast.makeText(this,"Permission accordée", Toast.LENGTH_SHORT).show();
                startScanning();
            }
            else{
                //Un message lui indique qu'il ne peut toujours pas se servir du scan
                Toast.makeText(this,"Permission refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
    * Fonction permettant d'aller sur une autre page(vue)
    * Ici cette fonction permettra d'accéder à la page recherche sans scan
    **/
    public void ouvrirRechercheSansScan(){
        Intent intent = new Intent(this, AccueilRechercheSansScan.class);
        startActivity(intent);
    }

    public void ouvrirAnimations()
    {
        Intent intent = new Intent(this, AccueilAnimations.class);
        startActivity(intent);
    }

    public void ouvrirHorRamPoubelles()
    {
        Intent intent = new Intent(this, AccueilHorRamPoubelles.class);
        startActivity(intent);
    }
    public void ouvrirProduitDetail()
    {
        Intent intent = new Intent(this, ProduitDetails.class);
        startActivity(intent);
    }

    //Gesture detector
    @Override
    public boolean onTouchEvent(MotionEvent event){
        _gestureUtilisateur.onTouchEvent(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                y1 = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                y2 = event.getY();
                float valueY = y2-y1;
                if(Math.abs(valueY) > MIN_DISTANCE){
                    if(y2<y1) {
                        ouvrirProduitDetail();
                        Toast.makeText(this, "Top Swipe", Toast.LENGTH_SHORT).show();
                    }
                }
        }
        return super.onTouchEvent(event);
    }




    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}
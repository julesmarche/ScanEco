package com.example.scaneco.horRamPoubelles;

import android.content.Context;
import android.os.Parcel;
import android.util.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;



public class FichierJsonManager {









    public static List<List<Ville>> valeurRenvoyeeJson(String fichierJson) throws Exception{



        List<List<Ville>> listeListeVilles = new ArrayList<>();

        try (JsonReader reader = new JsonReader(new StringReader(fichierJson)))
        {
            reader.beginObject();
            while (reader.hasNext()) {
                reader.nextName();
                reader.beginArray();
                while (reader.hasNext()) {
                    reader.beginObject();
                    reader.nextName();
                    reader.beginArray();
                    List<Ville> listeRenvoyee= new ArrayList<>();
                    while (reader.hasNext()) {
                        reader.beginObject();
                        Ville v = new Ville();
                        while (reader.hasNext()) {
                            String varibale = reader.nextName();
                            switch (varibale) {
                                case "nom":
                                    v.nom = reader.nextString();
                                    break;
                                case "codePostal":
                                    v.codePostal = reader.nextString();
                                    break;
                                case "joursSelectifs":
                                    v.joursSelectifs = reader.nextString();
                                    break;
                                case "heuresSelectifs":
                                    v.heuresSelectifs = reader.nextString();
                                    break;
                                case "joursMenagers":
                                    v.joursMenagers = reader.nextString();
                                    break;
                                case "heuresMenagers":
                                    v.heuresMenagers = reader.nextString();
                                    break;
                            }

                        }
                        listeRenvoyee.add(v);
                        reader.endObject();
                    }
                    listeListeVilles.add(listeRenvoyee);
                    reader.endArray();
                    reader.endObject();
                }
                reader.endArray();
            }
            reader.endObject();
        }
        catch (Exception e)
        {
            throw e;
        }
        return listeListeVilles;
    }
}



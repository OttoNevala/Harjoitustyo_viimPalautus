package com.example.olioohjelmointiharjoitusty;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class SearchValidity {

    public void checkCityValidity(Context context, String city, TextView targetView) {
        new Thread(() -> {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode metadata = objectMapper.readTree(
                        new URL("https://pxdata.stat.fi/PxWeb/api/v1/fi/StatFin/tyokay/statfin_tyokay_pxt_115x.px")
                );

                JsonNode alueVariable = null;
                for (JsonNode variable : metadata.get("variables")) {
                    if (variable.get("code").asText().equals("Alue")) {
                        alueVariable = variable;
                        break;
                    }
                }

                if (alueVariable == null) return;

                ArrayList<String> keys = new ArrayList<>();
                for (JsonNode node : alueVariable.get("valueTexts")) {
                    keys.add(node.asText().trim());
                }

                boolean found = false;
                for (String name : keys) {
                    if (name.equalsIgnoreCase(city.trim())) {
                        found = true;
                        break;
                    }
                }

                boolean finalFound = found;
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (finalFound) {
                        targetView.setText("✅ Kunta löytyi Tilastokeskukselta");
                        targetView.setTextColor(0xFF00FF00); // vihreä
                    } else {
                        targetView.setText("❌ Kuntaa ei löytynyt");
                        targetView.setTextColor(0xFFFF0000); // punainen
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> {
                    targetView.setText("⚠️ Virhe tarkistuksessa");
                    targetView.setTextColor(0xFFFFA500); // oranssi
                });
            }
        }).start();
    }
}



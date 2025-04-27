package com.example.olioohjelmointiharjoitusty.ShowData;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.example.olioohjelmointiharjoitusty.R;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class PopulationDataRetriever {

    private PopulationData populationData;
    private OnDataLoadedListener onDataLoadedListener;
    public interface OnDataLoadedListener {
        void onDataLoaded(PopulationData data);
    }

    public void setOnDataLoadedListener(OnDataLoadedListener listener) {
        this.onDataLoadedListener = listener;
    }

    // This retrieves the population and population change data from Tilastokeskus API
    // and updates the provided TextView (populationText) on the main thread.
    public void getData(final Context context, final String municipality, final TextView populationText) {
        new Thread(() -> {
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode areas = null;
            try {
                areas = objectMapper.readTree(
                        new URL("https://pxdata.stat.fi:443/PxWeb/api/v1/fi/StatFin/synt/statfin_synt_pxt_12dy.px")
                );
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            ArrayList<String> keys = new ArrayList<>();
            ArrayList<String> values = new ArrayList<>();

            for (JsonNode node : areas.get("variables").get(1).get("values")) {
                values.add(node.asText());
            }
            for (JsonNode node : areas.get("variables").get(1).get("valueTexts")) {
                keys.add(node.asText());
            }

            HashMap<String, String> municipalityCodes = new HashMap<>();
            for (int i = 0; i < keys.size(); i++) {
                municipalityCodes.put(keys.get(i), values.get(i));
            }
            String code = municipalityCodes.get(municipality);

            try {
                URL url = new URL("https://pxdata.stat.fi:443/PxWeb/api/v1/fi/StatFin/synt/statfin_synt_pxt_12dy.px");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json; utf-8");
                con.setRequestProperty("Accept", "application/json");
                con.setDoOutput(true);

                JsonNode jsonInputString = objectMapper.readTree(
                        context.getResources().openRawResource(R.raw.population_query)
                );
                ((ObjectNode) jsonInputString.get("query").get(0).get("selection"))
                        .putArray("values").add(code);

                byte[] input = objectMapper.writeValueAsBytes(jsonInputString);
                OutputStream os = con.getOutputStream();
                os.write(input, 0, input.length);
                os.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
                br.close();

                JsonNode municipalityData = objectMapper.readTree(response.toString());

                ArrayList<String> years = new ArrayList<>();
                for (JsonNode node : municipalityData.get("dimension").get("Vuosi").get("category").get("label")) {
                    years.add(node.asText());
                }

                JsonNode valuesNode = municipalityData.get("value");
                int index = municipalityData.get("dimension")
                        .get("Tiedot").get("category").get("label").size();

                if (years.size() > 0) {
                    int lastIndex = years.size() - 1;
                    int baseIndex = lastIndex * index;

                    int population = Integer.valueOf(valuesNode.get(baseIndex + 1).asText());

                    double percentChange = 0;
                    if (lastIndex > 0) {
                        int previousPopulation = Integer.valueOf(valuesNode.get((lastIndex - 1) * index + 1).asText());
                        if (previousPopulation > 0) {
                            percentChange = (population - previousPopulation) / (double) previousPopulation * 100;
                            percentChange = Math.round(percentChange * 100.0) / 100.0;
                        }
                    }

                    populationData = new PopulationData(0, 0);
                    populationData.setPopulation(population);
                    populationData.setPopulationChangePercent(percentChange);

                    String resultText = "Väestö: " + populationData.getPopulation()
                            + "\nVäestön muutos: " + populationData.getPopulationChangePercent() + "%";

                    new Handler(Looper.getMainLooper()).post(() -> {
                        populationText.setText(resultText);
                        if (onDataLoadedListener != null) {
                            onDataLoadedListener.onDataLoaded(populationData);
                        }
                    });
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
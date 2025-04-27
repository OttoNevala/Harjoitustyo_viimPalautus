//Read me
//This is created for showing the work self-sufficiency data.

package com.example.olioohjelmointiharjoitusty.ShowData;

import android.content.Context;
import android.widget.TextView;
import android.os.Handler;
import android.os.Looper;

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
import com.example.olioohjelmointiharjoitusty.R;

public class WorkSelfSufficiencyDataRetriever {

    // This retrieves the work self-sufficiency data from Tilastokeskus API
    // and updates the provided TextView (workSelfSufficiencyText) on the main thread.
    public void getData(final Context context, final String municipality, final TextView workSelfSufficiencyText) {
        new Thread(() -> {
            ObjectMapper objectMapper = new ObjectMapper();

            // This retrieves municipality codes
            JsonNode areas = null;
            try {
                areas = objectMapper.readTree(
                        new URL("https://pxdata.stat.fi:443/PxWeb/api/v1/fi/StatFin/tyokay/statfin_tyokay_pxt_125s.px")
                );
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            System.out.println(areas.toPrettyString());

            ArrayList<String> keys = new ArrayList<>();
            ArrayList<String> values = new ArrayList<>();

            // This reads through the municipality codes
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

            // This connects to the Tilastokeskus web
            try {
                URL url = new URL("https://pxdata.stat.fi:443/PxWeb/api/v1/fi/StatFin/tyokay/statfin_tyokay_pxt_125s.px");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json; utf-8");
                con.setRequestProperty("Accept", "application/json");
                con.setDoOutput(true);

                // This uses the R.raw.sufficiency_query to conduct its JSON query from Tilastokeskus.
                JsonNode jsonInputString = objectMapper.readTree(
                        context.getResources().openRawResource(R.raw.sufficiency_query)
                );

                // This adds the municipality code to the "Alue" dimension.
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

                // This reads through the response we got.
                JsonNode municipalityData = objectMapper.readTree(response.toString());

                // This reads through the year codes from our year dimension.
                ArrayList<String> years = new ArrayList<>();
                for (JsonNode node : municipalityData.get("dimension").get("Vuosi").get("category").get("label")) {
                    years.add(node.asText());
                }

                // This searches for the value table
                JsonNode valuesNode = municipalityData.get("value");
                int index = municipalityData.get("dimension")
                        .get("Tiedot").get("category").get("label").size();

                // This searches the data from the latest year. Currently it's 2023 as of writing this code.
                if (years.size() > 0) {
                    int lastIndex = years.size() - 1;
                    int baseIndex = lastIndex * index;
                    double workSelfSufficiencyPercentage = Double.valueOf(valuesNode.get(baseIndex).asText());

                    // This creates a WorkSelfSufficiencyData instance.
                    WorkSelfSufficiencyData workData = new WorkSelfSufficiencyData(0.0);
                    workData.setWorkSelfSufficiencyPercentage(workSelfSufficiencyPercentage);

                    // Use getter from WorkSelfSufficiencyData here
                    String resultText = "Työomavaraisuus: " + workData.getWorkSelfSufficiencyPercentage() + "%";

                    // This refreshes the UI in our main thread.
                    new Handler(Looper.getMainLooper()).post(() -> {
                        workSelfSufficiencyText.setText(resultText);
                    });
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
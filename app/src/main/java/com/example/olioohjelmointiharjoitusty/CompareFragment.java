package com.example.olioohjelmointiharjoitusty.comparison;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.olioohjelmointiharjoitusty.R;
import com.example.olioohjelmointiharjoitusty.ShowData.EmploymentRateDataRetriever;
import com.example.olioohjelmointiharjoitusty.ShowData.PopulationDataRetriever;
import com.example.olioohjelmointiharjoitusty.ShowData.WorkSelfSufficiencyDataRetriever;

public class CompareFragment extends Fragment {

    private EditText cityOneInput, cityTwoInput;
    private Button compareCitiesButton;

    private TextView cityName, temperature, humidity, windText, populationText, employmentRateText, workSelfSufficiencyText;
    private TextView cityName2, temperature2, humidity2, windText2, populationText2, employmentRateText2, workSelfSufficiencyText2;

    private PopulationDataRetriever populationDataRetriever;
    private EmploymentRateDataRetriever employmentRateDataRetriever;
    private WorkSelfSufficiencyDataRetriever workSelfSufficiencyDataRetriever;

    public CompareFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_compare, container, false);

        cityOneInput = view.findViewById(R.id.cityOneInput);
        cityTwoInput = view.findViewById(R.id.cityTwoInput);
        compareCitiesButton = view.findViewById(R.id.compareCitiesButton);

        cityName = view.findViewById(R.id.cityName);
        temperature = view.findViewById(R.id.temperature);
        humidity = view.findViewById(R.id.humidity);
        windText = view.findViewById(R.id.windText);
        populationText = view.findViewById(R.id.populationText);
        employmentRateText = view.findViewById(R.id.employmentRateText);
        workSelfSufficiencyText = view.findViewById(R.id.workSelfSufficiencyText);

        cityName2 = view.findViewById(R.id.cityName2);
        temperature2 = view.findViewById(R.id.temperature2);
        humidity2 = view.findViewById(R.id.humidity2);
        windText2 = view.findViewById(R.id.windText2);
        populationText2 = view.findViewById(R.id.populationText2);
        employmentRateText2 = view.findViewById(R.id.employmentRateText2);
        workSelfSufficiencyText2 = view.findViewById(R.id.workSelfSufficiencyText2);

        populationDataRetriever = new PopulationDataRetriever();
        employmentRateDataRetriever = new EmploymentRateDataRetriever();
        workSelfSufficiencyDataRetriever = new WorkSelfSufficiencyDataRetriever();

        compareCitiesButton.setOnClickListener(v -> {
            String city1 = cityOneInput.getText().toString().trim();
            String city2 = cityTwoInput.getText().toString().trim();

            if (city1.isEmpty() || city2.isEmpty()) {
                Toast.makeText(getContext(), "Syötä molemmat kaupunkien nimet", Toast.LENGTH_SHORT).show();
                return;
            }

            cityName.setText(city1);
            cityName2.setText(city2);

            populationDataRetriever.getData(requireContext(), city1, populationText);
            employmentRateDataRetriever.getData(requireContext(), city1, employmentRateText);
            workSelfSufficiencyDataRetriever.getData(requireContext(), city1, workSelfSufficiencyText);

            populationDataRetriever.getData(requireContext(), city2, populationText2);
            employmentRateDataRetriever.getData(requireContext(), city2, employmentRateText2);
            workSelfSufficiencyDataRetriever.getData(requireContext(), city2, workSelfSufficiencyText2);
        });

        return view;
    }
}

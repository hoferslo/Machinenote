package com.example.machinenote.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.machinenote.BaseFragment;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentOrodjaBinding;


public class OrodjaFragment extends BaseFragment {

    public String TAG = "Orodja";
    FragmentOrodjaBinding binding;
    Context context;

    // SharePoint povezave - prilagodi po potrebi
    private static final String SHAREPOINT_NAVODILA = "https://unichemsi.sharepoint.com/sites/TehnicnaDokumentacija/Delovna%20navodila/Forms/AllItems.aspx";
    private static final String SHAREPOINT_VARNOSTNI_LISTI = "https://unichemsi.sharepoint.com/:u:/r/sites/Razvoj/Varnostni_listi/SitePages/Home.aspx?csf=1&web=1&share=IQDwgg4DHofsQZsbjGuZ8VsCAZA87rG0HV7sWhy5ESi3-fY&e=x5gGij";
    private static final String SHAREPOINT_FILMI = "https://unichemsi.sharepoint.com/sites/Vzdrevanje/SitePages/Vzdr%C5%BEevanje.aspx";

    public OrodjaFragment() {
        // Required empty public constructor
    }

    public static OrodjaFragment newInstance(Context context) {
        OrodjaFragment fragment = new OrodjaFragment();
        fragment.context = context;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = FragmentOrodjaBinding.inflate(getLayoutInflater());

        setupClickListeners();

        return binding.getRoot();
    }

    private void setupClickListeners() {
        // Slike Linij kartica - now opens Linije fragment
        binding.cardSlikeLinij.setOnClickListener(v -> openLinijeFragment());

        // Navodila kartica
        binding.cardNavodila.setOnClickListener(v ->
                openSharePointLink(SHAREPOINT_NAVODILA, "Navodila linij"));

        // Varnostni listi kartica
        binding.cardVarnostni.setOnClickListener(v ->
                openSharePointLink(SHAREPOINT_VARNOSTNI_LISTI, "Varnostni listi"));

        // Filmi kartica
        binding.cardFilmi.setOnClickListener(v ->
                openSharePointLink(SHAREPOINT_FILMI, "Filmi"));
    }

    private void openLinijeFragment() {
        try {
            MainActivity mainActivity = (MainActivity) requireActivity();
            FragmentManager fragmentManager = mainActivity.getSupportFragmentManager();

            // Create new instance of LinijeFragment
            Fragment linijeFragment = LinijeFragment.newInstance(mainActivity);

            // Load the fragment
            mainActivity.loadFragment(linijeFragment);

        } catch (Exception e) {
            Toast.makeText(getContext(),
                    "Napaka pri odpiranju Linij: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void openSharePointLink(String url, String title) {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        } catch (Exception e) {
            Toast.makeText(getContext(),
                    "Napaka pri odpiranju " + title + ": " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.binding.toolbarTitle.setText(TAG);
        mainActivity.showBackArrow();
    }
}
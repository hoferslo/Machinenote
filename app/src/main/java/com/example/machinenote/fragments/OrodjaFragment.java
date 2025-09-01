package com.example.machinenote.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.machinenote.BaseFragment;
import com.example.machinenote.activities.MainActivity;
import com.example.machinenote.databinding.FragmentOrodjaBinding;


public class OrodjaFragment extends BaseFragment {

    public String TAG = "Orodja";
    FragmentOrodjaBinding binding;
    Context context;

    // SharePoint povezave - prilagodi po potrebi
    private static final String SHAREPOINT_SLIKE_LINIJ = "https://youtube.com"; //TODO: CHANGE TO ACTUAL FRAGMENT WITH SLIKE LINIJ
    private static final String SHAREPOINT_NAVODILA = "https://unichemsi.sharepoint.com/sites/TehnicnaDokumentacija/Delovna%20navodila/Forms/AllItems.aspx";
    private static final String SHAREPOINT_VARNOSTNI_LISTI = "https://unichemsi.sharepoint.com/sites/Vzdrevanje/Shared%20Documents/Forms/AllItems.aspx?newTargetListUrl=%2Fsites%2FVzdrevanje%2FShared%20Documents&viewpath=%2Fsites%2FVzdrevanje%2FShared%20Documents%2FForms%2FAllItems%2Easpx&id=%2Fsites%2FVzdrevanje%2FShared%20Documents%2FVPD%5FVPP%2Fvarnost%20pri%20delu%2Fnavodila%5Fza%5Fvarno%5Fdelo%2FVL&viewid=0c6ba4ea%2D9c32%2D485f%2D9b2c%2D90125bc183c3";
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
        // Slike Linij kartica
        binding.cardSlikeLinij.setOnClickListener(v ->
                openSharePointLink(SHAREPOINT_SLIKE_LINIJ, "Slike Linij"));

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
    }
}
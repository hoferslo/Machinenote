package com.example.machinenote.customFragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.machinenote.ApiManager;
import com.example.machinenote.R;
import com.example.machinenote.Utility.ImageCandidateAdapter;
import com.example.machinenote.databinding.FragmentRezervniDeliBottomSheetBinding;
import com.example.machinenote.models.ImageCandidate;
import com.example.machinenote.models.ImageResponse;
import com.example.machinenote.models.RezervniDel;
import com.example.machinenote.models.SaveImageResponse;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class RezervniDeliBottomSheetFragment extends BottomSheetDialogFragment {

    public FragmentRezervniDeliBottomSheetBinding binding;
    private Context context;
    private RezervniDel rezervniDel;
    private DecimalFormat currencyFormat;
    private ApiManager apiManager;

    // Seznami in adapter za iskanje slik (mreža 3x2)
    private final List<ImageCandidate> vsiKandidati = new ArrayList<>();
    private final List<ImageCandidate> trenutnoPrikazani = new ArrayList<>();
    private ImageCandidateAdapter imageAdapter;
    private int imageOffset = 0;

    public static RezervniDeliBottomSheetFragment newInstance(Context context, RezervniDel rezervniDel) {
        RezervniDeliBottomSheetFragment fragment = new RezervniDeliBottomSheetFragment();
        fragment.rezervniDel = rezervniDel;
        fragment.context = context;
        fragment.apiManager = new ApiManager(context);
        return fragment;
    }

    @SuppressLint({"StringFormatInvalid", "SetTextI18n"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRezervniDeliBottomSheetBinding.inflate(inflater, container, false);
        currencyFormat = new DecimalFormat("#,##0.00 €");

        // Polnjenje tekstovnih podatkov glave in lokacije
        binding.partIdHeader.setText(getString(R.string.id_rezervnega_dela) + ": " + rezervniDel.getId());

        String enota = rezervniDel.getEnotaNaziv();
        binding.warehouse.setText(String.valueOf(rezervniDel.getSkladišče()));
        if (rezervniDel.getRegal() != null && !rezervniDel.getRegal().isEmpty()) {
            binding.shelf.setText(rezervniDel.getRegal());
        }

        // Artikel in opis
        if (rezervniDel.getArtikel() != null && !rezervniDel.getArtikel().isEmpty()) {
            binding.articleName.setText(rezervniDel.getArtikel());
        }
        if (rezervniDel.getArtikel_dolgi_text() != null && !rezervniDel.getArtikel_dolgi_text().isEmpty()) {
            binding.longDescription.setText(rezervniDel.getArtikel_dolgi_text());
        }

        // Partnerji (proizvajalec in dobavitelj)
        if (rezervniDel.getProizvajalec() != null && !rezervniDel.getProizvajalec().isEmpty()) {
            binding.manufacturer.setText(rezervniDel.getProizvajalec());
        }
        if (rezervniDel.getDobavitelj() != null && !rezervniDel.getDobavitelj().isEmpty()) {
            binding.supplier.setText(rezervniDel.getDobavitelj());
        }

        // Cene
        if (rezervniDel.getZnesek() > 0) {
            binding.unitPrice.setText(currencyFormat.format(rezervniDel.getZnesek()));
        }
        double totalPrice = rezervniDel.getZnesek() * rezervniDel.getRealZalogo();
        if (totalPrice > 0) {
            binding.totalPrice.setText(currencyFormat.format(totalPrice));
        }

        // Zaloge in statistika
        if (rezervniDel.getMinimalna_zaloga() > 0) {
            binding.minStock.setText(rezervniDel.getMinimalna_zaloga() + " " + enota);
        }
        if (rezervniDel.getDobava() > 0) {
            binding.supply.setText(rezervniDel.getDobava() + " " + enota);
        }
        if (rezervniDel.getPoraba() > 0) {
            binding.consumption.setText(rezervniDel.getPoraba() + " " + enota);
        }
        if (rezervniDel.getInventura() > 0) {
            binding.inventory.setText(String.valueOf(rezervniDel.getInventura()));
        }

        double realStock = rezervniDel.getRealZalogo();
        binding.actualStock.setText(String.valueOf(realStock) + " " + enota);

        // Barvno kodiranje dejanske zaloge glede na minimalno zalogo
        if (realStock < rezervniDel.getMinimalna_zaloga()) {
            binding.actualStock.setTextColor(getResources().getColor(R.color.error_primary, null));
        } else if (realStock < rezervniDel.getMinimalna_zaloga() * 1.5) {
            binding.actualStock.setTextColor(getResources().getColor(R.color.warning_primary, null));
        } else {
            binding.actualStock.setTextColor(getResources().getColor(R.color.success_primary, null));
        }

        // Nastavitev RecyclerView-ja za kandidate slik (3 stolpci)
        if (binding.rvImageCandidates != null) {
            binding.rvImageCandidates.setLayoutManager(new GridLayoutManager(getContext(), 3));
            imageAdapter = new ImageCandidateAdapter(trenutnoPrikazani, this::onImageCandidateSelected);
            binding.rvImageCandidates.setAdapter(imageAdapter);
        }

        setupClickListeners();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prikažiTrenutnoSliko();
        loadPartImage();
    }

    private void prikažiTrenutnoSliko() {
        if (rezervniDel == null || binding == null) return;

        String trenutnaSlikaUrl = rezervniDel.getSlikaUrl();

        if (trenutnaSlikaUrl != null && !trenutnaSlikaUrl.isEmpty()) {
            binding.imageProgressBar.setVisibility(View.VISIBLE);

            Picasso.get()
                    .load(trenutnaSlikaUrl)
                    .placeholder(R.drawable.ic_factory)
                    .error(R.drawable.ic_theme_switcher)
                    .into(binding.partImageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            if (binding != null) binding.imageProgressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            if (binding != null) {
                                binding.imageProgressBar.setVisibility(View.GONE);
                                // IZPIS NAPAKE V LOGCAT:
                                Log.e("PicassoError", "Slika se ni naložila. URL: " + trenutnaSlikaUrl, e);
                            }
                        }
                    });
        } else {
            // Če v bazi še ni slike, prikaži privzeto ikono
            binding.partImageView.setImageResource(R.drawable.ic_factory);
            binding.imageProgressBar.setVisibility(View.GONE);
        }
    }

    private void loadPartImage() {
        if (rezervniDel == null || apiManager == null) return;

        // Pripravimo vse tri parametre za SerpApi iskanje
        String naziv = (rezervniDel.getArtikel() != null) ? rezervniDel.getArtikel() : "";
        String proizvajalec = (rezervniDel.getProizvajalec() != null) ? rezervniDel.getProizvajalec() : "";
        String fallbackQuery = (rezervniDel.getArtikel_dolgi_text() != null) ? rezervniDel.getArtikel_dolgi_text() : "";

        if (naziv.isEmpty() && proizvajalec.isEmpty() && fallbackQuery.isEmpty()) return;

        // Pokličemo prenovljeno metodo iz ApiManagerja s tremi parametri
        apiManager.searchGoogleImage(naziv, proizvajalec, fallbackQuery, new ApiManager.ImageSearchCallback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(ImageResponse response) {
                if (binding == null) return;

                vsiKandidati.clear();
                if (response.getKandidati() != null) {
                    vsiKandidati.addAll(response.getKandidati());
                }
                if (response.getRezerva() != null) {
                    vsiKandidati.addAll(response.getRezerva());
                }

                imageOffset = 0;
                prikažiNaslednjoSkupinoSlik();

                // Gumb za preklop prikažemo samo, če imamo več kot 6 slik
                if (binding.btnNextImages != null && vsiKandidati.size() > 6) {
                    binding.btnNextImages.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                if (binding == null) return;
                Log.e("ImageSearch", "Napaka pri iskanju: " + errorMessage);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void prikažiNaslednjoSkupinoSlik() {
        if (vsiKandidati.isEmpty() || getActivity() == null) return;

        // Prisili izvajanje na UI (glavni) niti, da se RecyclerView pravilno osveži
        getActivity().runOnUiThread(() -> {
            trenutnoPrikazani.clear();
            int limit = Math.min(imageOffset + 6, vsiKandidati.size());

            for (int i = imageOffset; i < limit; i++) {
                trenutnoPrikazani.add(vsiKandidati.get(i));
            }

            if (imageAdapter != null) {
                imageAdapter.notifyDataSetChanged();
                Log.d("ImageSearch", "Adapter osvežen s " + trenutnoPrikazani.size() + " slikami.");
            }

            imageOffset += 6;
            if (imageOffset >= vsiKandidati.size()) {
                imageOffset = 0; // Krožno preklapljanje (zavrti se na začetek)
            }
        });
    }

    private void onImageCandidateSelected(ImageCandidate candidate) {
        if (candidate == null || candidate.getOriginal() == null) return;

        // Uporabimo čisto wrapper metodo iz ApiManagerja
        apiManager.saveImage(rezervniDel.getId(), candidate.getOriginal(), new ApiManager.SaveImageCallback() {
            @Override
            public void onSuccess(SaveImageResponse response) {
                if (binding == null) return;
                Toast.makeText(context, "Slika uspešno shranjena!", Toast.LENGTH_SHORT).show();

                // Posodobimo lokalno instanco modela s shranjenim URL naslovom slike
                rezervniDel.setSlikaUrl(candidate.getOriginal());

                // Takoj posodobimo zgornji glavni ImageView
                prikažiTrenutnoSliko();
            }

            @Override
            public void onFailure(String errorMessage) {
                if (binding == null) return;
                Toast.makeText(context, "Napaka pri shranjevanju: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        // Gumb za nalaganje naslednjih 6 slik iz seznama
        if (binding.btnNextImages != null) {
            binding.btnNextImages.setOnClickListener(v -> prikažiNaslednjoSkupinoSlik());
        }

        binding.warehouse.setOnClickListener(v -> {
            Toast.makeText(context, getString(R.string.skladisce) + ": " + rezervniDel.getSkladišče(), Toast.LENGTH_SHORT).show();
        });

        binding.shelf.setOnClickListener(v -> {
            if (rezervniDel.getRegal() != null && !rezervniDel.getRegal().isEmpty()) {
                Toast.makeText(context, getString(R.string.regal) + ": " + rezervniDel.getRegal(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.manufacturer.setOnClickListener(v -> {
            if (rezervniDel.getProizvajalec() != null && !rezervniDel.getProizvajalec().isEmpty()) {
                Toast.makeText(context, getString(R.string.proizvajalec) + ": " + rezervniDel.getProizvajalec(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.supplier.setOnClickListener(v -> {
            if (rezervniDel.getDobavitelj() != null && !rezervniDel.getDobavitelj().isEmpty()) {
                Toast.makeText(context, getString(R.string.dobavitelj) + ": " + rezervniDel.getDobavitelj(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.actualStock.setOnClickListener(v -> {
            double realStock = rezervniDel.getRealZalogo();
            String stockInfo = getString(R.string.dejanska_zaloga) + ": " + realStock;
            if (realStock < rezervniDel.getMinimalna_zaloga()) {
                stockInfo += "\n" + getString(R.string.nizka_zaloga_opozorilo);
            }
            Toast.makeText(context, stockInfo, Toast.LENGTH_LONG).show();
        });

        binding.longDescription.setOnLongClickListener(v -> {
            if (rezervniDel.getArtikel_dolgi_text() != null && !rezervniDel.getArtikel_dolgi_text().isEmpty()) {
                Toast.makeText(context, rezervniDel.getArtikel_dolgi_text(), Toast.LENGTH_LONG).show();
            }
            return true;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
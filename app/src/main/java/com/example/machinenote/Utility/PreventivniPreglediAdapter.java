package com.example.machinenote.Utility;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.machinenote.R;
import com.example.machinenote.models.PreventivniPregled;
import com.example.machinenote.models.Linija;

import java.util.List;

public class PreventivniPreglediAdapter extends RecyclerView.Adapter<PreventivniPreglediAdapter.ViewHolder> {

    private Context context;
    private List<Object> itemList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Object item);
        void onButtonClick(Object item);
    }

    public PreventivniPreglediAdapter(Context context, List<Object> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<Object> newList) {
        this.itemList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_preventivni_pregledi, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Object item = itemList.get(position);
        if (item instanceof PreventivniPregled) {
            holder.bindPreventivniPregled((PreventivniPregled) item);
        } else if (item instanceof Linija) {
            holder.bindLinija((Linija) item);
        }
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView opisText;
        private TextView sklopText;
        private TextView statusText;
        private TextView zadnjiPregledText;
        private TextView zadnjiPregled;
        private TextView naslednjiPregledText;
        private TextView naslenjdiPregledText;
        private LinearLayout naslednjiPregledTab;
        private View statusIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            opisText = itemView.findViewById(R.id.opisText);
            sklopText = itemView.findViewById(R.id.sklopText);
            statusText = itemView.findViewById(R.id.statusText);
            zadnjiPregledText = itemView.findViewById(R.id.zadnjiPregledText);
            naslenjdiPregledText = itemView.findViewById(R.id.naslenjdiPregledText);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
            zadnjiPregled = itemView.findViewById(R.id.zadnjiPregled);
            naslednjiPregledTab = itemView.findViewById(R.id.naslednjiPregledTab);

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(itemList.get(getAdapterPosition()));
                }
            });

            // Click listener za status/SAP text
            statusText.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onButtonClick(itemList.get(getAdapterPosition()));
                }
            });
        }

        public void bindPreventivniPregled(PreventivniPregled pregled) {
            // Nastavi opis
            if (pregled.getOpis() != null && !pregled.getOpis().isEmpty()) {
                opisText.setText(pregled.getOpis());
            } else {
                opisText.setText("Preventivni pregled");
            }

            // Nastavi sklop
            if (pregled.getSklopLinije() != null && !pregled.getSklopLinije().isEmpty()) {
                sklopText.setText(pregled.getSklopLinije());
                sklopText.setVisibility(View.VISIBLE);
            } else {
                sklopText.setVisibility(View.GONE);
            }

            // Nastavi status
            if (pregled.getStatusText() != null && !pregled.getStatusText().isEmpty()) {
                statusText.setText(pregled.getStatusText());
                statusText.setVisibility(View.VISIBLE);
                updateStatusIndicatorForPregled(pregled.getStatusText());
            } else {
                statusText.setVisibility(View.GONE);
            }

            // Nastavi zadnji pregled
            if (pregled.getDatum() != null && !pregled.getDatum().isEmpty()) {
                zadnjiPregledText.setText(pregled.getDatum());
            } else {
                zadnjiPregledText.setText("Ni podatka");
            }

            // Nastavi naslednji pregled
            if (pregled.getNaslenjniPregled() != null && !pregled.getNaslenjniPregled().isEmpty()) {
                naslenjdiPregledText.setText(pregled.getNaslenjniPregled());
            } else {
                naslenjdiPregledText.setText("Ni podatka");
            }
        }

        public void bindLinija(Linija linija) {
            // Nastavi ime linije (opisText)
            if (linija.getNaziv_linije() != null && !linija.getNaziv_linije().isEmpty()) {
                opisText.setText(linija.getNaziv_linije());
            } else {
                opisText.setText("Linija");
            }

            // Nastavi lokacijo (sklopText)
            String lokacija = "";
            if (linija.getLokacija_naziv() != null && !linija.getLokacija_naziv().isEmpty()) {
                lokacija = linija.getLokacija_naziv();
                if (linija.getProstor_naziv() != null && !linija.getProstor_naziv().isEmpty()) {
                    lokacija += " - " + linija.getProstor_naziv();
                }
            } else if (linija.getProstor_naziv() != null && !linija.getProstor_naziv().isEmpty()) {
                lokacija = linija.getProstor_naziv();
            }

            if (!lokacija.isEmpty()) {
                sklopText.setText(lokacija);
                sklopText.setVisibility(View.VISIBLE);
            } else {
                sklopText.setVisibility(View.GONE);
            }

            // Nastavi SAP kodo (statusText)
            if (linija.getLinija_SAP() != null && !linija.getLinija_SAP().isEmpty()) {
                statusText.setText(linija.getLinija_SAP());
                statusText.setVisibility(View.VISIBLE);
                updateStatusIndicatorForLinija(linija);
            } else {
                statusText.setVisibility(View.GONE);
            }

            // Nastavi število sklopov (zadnjiPregledText)
            if (linija.getStevilo_sklopov() > 0) {
                zadnjiPregled.setText("Število sklopov");
                zadnjiPregledText.setText(String.valueOf(linija.getStevilo_sklopov()));
            } else {
                zadnjiPregledText.setText("0");
            }

            naslednjiPregledTab.setVisibility(View.GONE);
        }

        private void updateStatusIndicatorForPregled(String status) {
            if (status == null) return;

            int colorResource;
            switch (status.toLowerCase()) {
                case "aktualen":
                case "končan":
                    colorResource = R.color.success_primary;
                    break;
                case "zamuda":
                case "kritičen":
                    colorResource = R.color.error_primary;
                    break;
                case "opozorilo":
                case "kmalu":
                    colorResource = R.color.warning_primary;
                    break;
                default:
                    colorResource = R.color.action_primary;
                    break;
            }

            statusIndicator.setBackgroundTintList(
                    context.getResources().getColorStateList(colorResource, null)
            );
        }

        private void updateStatusIndicatorForLinija(Linija linija) {
            int colorResource;

            // Preveri če je linija aktivna
            if (linija.getLinija_aktivna() != null &&
                    (linija.getLinija_aktivna().equalsIgnoreCase("true") ||
                            linija.getLinija_aktivna().equalsIgnoreCase("aktivna") ||
                            linija.getLinija_aktivna().equals("1"))) {
                colorResource = R.color.success_primary; // Zelena za aktivne
            } else {
                colorResource = R.color.error_primary; // Rdeča za neaktivne
            }

            statusIndicator.setBackgroundTintList(
                    context.getResources().getColorStateList(colorResource, null)
            );
        }
    }
}
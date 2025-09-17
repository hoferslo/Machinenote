package com.example.machinenote.Utility;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.machinenote.R;
import com.example.machinenote.models.PreventivniPregled;

import java.util.List;

public class PreventivniPreglediAdapter extends RecyclerView.Adapter<PreventivniPreglediAdapter.PreventivniPregledViewHolder> {

    private Context context;
    private List<PreventivniPregled> preventivniPreglediList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(PreventivniPregled pregled);
        void onButtonClick(PreventivniPregled pregled);
    }

    public PreventivniPreglediAdapter(Context context, List<PreventivniPregled> preventivniPreglediList) {
        this.context = context;
        this.preventivniPreglediList = preventivniPreglediList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<PreventivniPregled> newList) {
        this.preventivniPreglediList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PreventivniPregledViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_preventivni_pregledi, parent, false);
        return new PreventivniPregledViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PreventivniPregledViewHolder holder, int position) {
        PreventivniPregled pregled = preventivniPreglediList.get(position);
        holder.bind(pregled);
    }

    @Override
    public int getItemCount() {
        return preventivniPreglediList != null ? preventivniPreglediList.size() : 0;
    }

    public class PreventivniPregledViewHolder extends RecyclerView.ViewHolder {
        private TextView opisText;
        private TextView sklopText;
        private TextView statusText;
        private TextView zadnjiPregledText;
        private TextView naslenjdiPregledText;
        private View statusIndicator;

        public PreventivniPregledViewHolder(@NonNull View itemView) {
            super(itemView);
            opisText = itemView.findViewById(R.id.opisText);
            sklopText = itemView.findViewById(R.id.sklopText);
            statusText = itemView.findViewById(R.id.statusText);
            zadnjiPregledText = itemView.findViewById(R.id.zadnjiPregledText);
            naslenjdiPregledText = itemView.findViewById(R.id.naslenjdiPregledText);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(preventivniPreglediList.get(getAdapterPosition()));
                }
            });

            // Lahko dodaš še dodatne click listenere za različne dele CardView-a
            statusText.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onButtonClick(preventivniPreglediList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(PreventivniPregled pregled) {
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

                // Nastavi barvo status indicator-ja glede na status
                updateStatusIndicator(pregled.getStatusText());
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

        private void updateStatusIndicator(String status) {
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
    }
}
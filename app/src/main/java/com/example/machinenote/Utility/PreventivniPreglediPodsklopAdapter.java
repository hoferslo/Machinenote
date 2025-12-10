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
import com.example.machinenote.models.SklopLinije;

import java.util.List;

public class PreventivniPreglediPodsklopAdapter extends RecyclerView.Adapter<PreventivniPreglediPodsklopAdapter.ViewHolder> {

    private Context context;
    private List<SklopLinije> itemList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SklopLinije sklop);
    }

    public PreventivniPreglediPodsklopAdapter(Context context, List<SklopLinije> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<SklopLinije> newList) {
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
        SklopLinije sklop = itemList.get(position);
        holder.bind(sklop);
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

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(itemList.get(getAdapterPosition()));
                }
            });
        }

        public void bind(SklopLinije sklop) {
            // Nastavi ime podsklopa
            if (sklop.getSklopLinije() != null && !sklop.getSklopLinije().isEmpty()) {
                opisText.setText(sklop.getSklopLinije());
            } else {
                opisText.setText("Podsklop");
            }

            // Skrij sklop text (ni potreben za podsklope)
            sklopText.setVisibility(View.GONE);

            // Nastavi ID podsklopa kot status
            if (sklop.getId() != 0) {
                statusText.setText("ID: " + sklop.getId());
                statusText.setVisibility(View.VISIBLE);
                // Modra barva za podsklope
                statusIndicator.setBackgroundTintList(
                        context.getResources().getColorStateList(R.color.action_primary, null)
                );
            } else {
                statusText.setVisibility(View.GONE);
            }

            // Nastavi število opravil
            if (sklop.getOpravilaCount() > 0) {
                zadnjiPregled.setText("Število opravil");
                zadnjiPregledText.setText(String.valueOf(sklop.getOpravilaCount()));
            } else {
                zadnjiPregled.setText("Število opravil");
                zadnjiPregledText.setText("0");
            }

            // Skrij naslednji pregled tab (ni potreben za podsklope)
            naslednjiPregledTab.setVisibility(View.GONE);
        }
    }
}
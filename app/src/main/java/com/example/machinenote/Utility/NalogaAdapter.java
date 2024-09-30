package com.example.machinenote.Utility;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.machinenote.R;
import com.example.machinenote.models.Naloga;

import java.util.List;

public class NalogaAdapter extends RecyclerView.Adapter<NalogaAdapter.ViewHolder> {

    private final Context context;
    private List<Naloga> nalogaList;
    private final OnItemClickListener onItemClickListener;

    // Interface za klike na elemente in gumbe
    public interface OnItemClickListener {
        void onItemClick(Naloga naloga);

        void onButtonClick(Naloga naloga);
    }

    // Konstruktor za inicializacijo konteksta, seznama nalog in poslušalca klikov
    public NalogaAdapter(Context context, List<Naloga> nalogaList, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.nalogaList = nalogaList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflata postavitev za posamezen element naloge
        View view = LayoutInflater.from(context).inflate(R.layout.item_naloga_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Naloga naloga = nalogaList.get(position);

        // Nastavi vidljivost in besedilo za vsak TextView
        setTextViewVisibility(holder.vzdrzevalec, naloga.getVzdrzevalec());
        setTextViewVisibility(holder.naloga, naloga.getNaloga());
        setTextViewVisibility(holder.rokZaIzvedbo, naloga.getRokZaIzvedbo());
        setTextViewVisibility(holder.izvedeno, naloga.getIzvedeno());
        setTextViewVisibility(holder.komentar, naloga.getKomentar());

        // Prikaži sliko pred izvedbo naloge, če obstaja
        if (naloga.getSlikePredIzpolnitvijoNaloge() != null && naloga.getSlikePredIzpolnitvijoNaloge().length != 0) {
            //Picasso.get().load(naloga.getSlikePredIzpolnitvijoNaloge()[0]).into(holder.slikaPred); // todo do a for in the array getSlikePredIzpolnitvijoNaloge
        }

        // Dodeli klike elementom in gumbom
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(naloga));
        holder.buttonAction.setOnClickListener(v -> onItemClickListener.onButtonClick(naloga));
    }

    // Metoda za nastavitev vidljivosti TextView glede na vsebino
    private void setTextViewVisibility(TextView textView, String text) {
        if (text == null || text.isEmpty()) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(text);
        }
    }

    @Override
    public int getItemCount() {
        return nalogaList.size();
    }

    // Metoda za osvežitev seznama nalog
    public void updateList(List<Naloga> newList) {
        nalogaList = newList;
        notifyDataSetChanged();
    }

    // Razred ViewHolder za povezovanje komponent iz layout datoteke
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView vzdrzevalec;
        TextView naloga;
        TextView rokZaIzvedbo;
        TextView izvedeno;
        TextView komentar;
        ImageView slikaPred;
        Button buttonAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            vzdrzevalec = itemView.findViewById(R.id.vzdrzevalec);
            naloga = itemView.findViewById(R.id.naloga);
            rokZaIzvedbo = itemView.findViewById(R.id.rokZaIzvedbo);
            izvedeno = itemView.findViewById(R.id.izvedeno);
            komentar = itemView.findViewById(R.id.komentar);
            //  slikaPred = itemView.findViewById(R.id.slikaPred);
            buttonAction = itemView.findViewById(R.id.buttonAction);
        }
    }
}

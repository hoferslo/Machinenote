package com.example.machinenote.Utility;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.machinenote.R;
import com.example.machinenote.models.RezervniDel;

import java.util.List;

public class RezervniDelAdapter extends RecyclerView.Adapter<RezervniDelAdapter.ViewHolder> {

    private final Context context;
    private List<RezervniDel> rezervniDelList;
    private final OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(RezervniDel rezervniDel);

        void onButtonClick(RezervniDel rezervniDel);
    }

    public RezervniDelAdapter(Context context, List<RezervniDel> rezervniDelList, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.rezervniDelList = rezervniDelList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rezervni_del_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RezervniDel rezervniDel = rezervniDelList.get(position);
        // Set text for each TextView and handle visibility based on content
        setTextViewVisibility(holder.sparePart, rezervniDel.getArtikel());
        setTextViewVisibility(holder.idOfSparePart, String.valueOf(rezervniDel.getId()));
        setTextViewVisibility(holder.rackOfSparePart, rezervniDel.getRegal());
        setTextViewVisibility(holder.stockOfSparePart, String.valueOf(rezervniDel.getRealZalogo()));
        setTextViewVisibility(holder.signOfSparePart, String.valueOf(rezervniDel.getZnaki()));

        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(rezervniDel));
        holder.buttonAction.setOnClickListener(v -> onItemClickListener.onButtonClick(rezervniDel));

    }

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
        return rezervniDelList.size();
    }

    public void updateList(List<RezervniDel> newList) {
        rezervniDelList = newList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView sparePart;
        TextView idOfSparePart;
        TextView rackOfSparePart;
        TextView stockOfSparePart;
        TextView signOfSparePart;
        Button buttonAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sparePart = itemView.findViewById(R.id.sparePart);
            idOfSparePart = itemView.findViewById(R.id.idOfSparePart);
            rackOfSparePart = itemView.findViewById(R.id.rackOfSparePart);
            stockOfSparePart = itemView.findViewById(R.id.stockOfSparePart);
            signOfSparePart = itemView.findViewById(R.id.signOfSparePart);
            buttonAction = itemView.findViewById(R.id.buttonAction);
        }
    }
}

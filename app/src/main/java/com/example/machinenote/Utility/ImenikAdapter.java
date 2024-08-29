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
import com.example.machinenote.models.Imenik;

import java.util.List;

public class ImenikAdapter extends RecyclerView.Adapter<ImenikAdapter.ViewHolder> {

    private final Context context;
    private List<Imenik> imenikList;
    private final OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Imenik imenik);

        void onButtonClick(Imenik imenik);
    }

    public ImenikAdapter(Context context, List<Imenik> imenikList, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.imenikList = imenikList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_imenik_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Imenik imenik = imenikList.get(position);
        // Set text for each TextView and handle visibility based on content
        setTextViewVisibility(holder.companyName, imenik.getNazivPodjetja());
        setTextViewVisibility(holder.nameOfShipper, imenik.getKontaktnaOseba());
        setTextViewVisibility(holder.gmail, imenik.getMail());
        setTextViewVisibility(holder.telephoneNumber, imenik.getTelefon());
        setTextViewVisibility(holder.GSM, imenik.getGsm());

        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(imenik));
        holder.buttonAction.setOnClickListener(v -> onItemClickListener.onButtonClick(imenik));

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
        return imenikList.size();
    }

    public void updateList(List<Imenik> newList) {
        imenikList = newList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView companyName;
        TextView nameOfShipper;
        TextView gmail;
        TextView telephoneNumber;
        TextView GSM;
        Button buttonAction;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            companyName = itemView.findViewById(R.id.companyName);
            nameOfShipper = itemView.findViewById(R.id.nameOfShipper);
            gmail = itemView.findViewById(R.id.gmail);
            telephoneNumber = itemView.findViewById(R.id.telephoneNumber);
            GSM = itemView.findViewById(R.id.GSM);
            buttonAction = itemView.findViewById(R.id.buttonAction);
        }
    }
}

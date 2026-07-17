package com.example.machinenote.Utility;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.machinenote.R;
import com.example.machinenote.models.ImageCandidate;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageCandidateAdapter extends RecyclerView.Adapter<ImageCandidateAdapter.ViewHolder> {

    public interface OnCandidateSelectedListener {
        void onSelected(ImageCandidate candidate);
    }

    private final List<ImageCandidate> candidates;
    private final OnCandidateSelectedListener listener;

    public ImageCandidateAdapter(List<ImageCandidate> candidates, OnCandidateSelectedListener listener) {
        this.candidates = candidates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_candidate, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageCandidate candidate = candidates.get(position);

        Picasso.get()
                .load(candidate.getThumbnail())
                .placeholder(R.drawable.ic_factory)
                .error(R.drawable.ic_theme_switcher)
                .fit()
                .centerCrop()
                .into(holder.imgCandidate);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSelected(candidate);
        });
    }

    @Override
    public int getItemCount() {
        return candidates.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCandidate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCandidate = itemView.findViewById(R.id.imgCandidate);
        }
    }
}
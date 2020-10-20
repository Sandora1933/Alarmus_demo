package com.example.alarmus_demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alarmus_demo.R;
import com.example.alarmus_demo.model.CardInfoEntity;

import java.util.List;

public class InfoCardAdapter extends RecyclerView.Adapter<InfoCardAdapter.InfoCardViewHolder>{

    private Context context;
    private List<CardInfoEntity> cardInfoEntityList;

    public static class InfoCardViewHolder extends RecyclerView.ViewHolder{

        TextView titleTextView, urlTextView, shortInfoTextView;

        public InfoCardViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.cardInfoTitleTextView);
            urlTextView = itemView.findViewById(R.id.cardInfoUrlTextView);
            shortInfoTextView = itemView.findViewById(R.id.cardInfoShortInfoTextView);
        }
    }

    public InfoCardAdapter(Context context, List<CardInfoEntity> cardInfoEntityList) {
        this.context = context;
        this.cardInfoEntityList = cardInfoEntityList;
    }

    @NonNull
    @Override
    public InfoCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View cardInfoView = LayoutInflater.from(context).inflate(R.layout.item_card_info, parent, false);

        return new InfoCardViewHolder(cardInfoView);
    }

    @Override
    public void onBindViewHolder(@NonNull InfoCardViewHolder holder, int position) {
        holder.titleTextView.setText(cardInfoEntityList.get(position).getTitle());
        holder.urlTextView.setText(cardInfoEntityList.get(position).getUrl());
        holder.shortInfoTextView.setText(cardInfoEntityList.get(position).getShortInfo());
    }

    @Override
    public int getItemCount() {
        return cardInfoEntityList.size();
    }


}

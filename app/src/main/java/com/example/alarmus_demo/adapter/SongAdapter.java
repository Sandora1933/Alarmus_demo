package com.example.alarmus_demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alarmus_demo.R;
import com.example.alarmus_demo.model.SongEntity;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder>{

    Context context;
    List<SongEntity> songEntityList; //This list is changeable depending on user search preferences
    List<SongEntity> songEntityListFull; //This list is full and static

    OnSongItemClickListener songClickListener;

    public interface OnSongItemClickListener{
        void onClick(int position);
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder{

        TextView songTitleTextView, singerTextView;
        TextView songPriorityTextView;
        ImageView songIsActiveImageView;

        public SongViewHolder(@NonNull View itemView, final OnSongItemClickListener listener) {
            super(itemView);
            songTitleTextView = itemView.findViewById(R.id.songTitleTextView);
            singerTextView = itemView.findViewById(R.id.singerTextView);
            songPriorityTextView = itemView.findViewById(R.id.songPriorityTextView);
            songIsActiveImageView = itemView.findViewById(R.id.songIsActiveImageView);

            songIsActiveImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onClick(position);
                        }
                    }
                }
            });

        }
    }

    public SongAdapter(Context context, List<SongEntity> songEntityList) {
        this.context = context;
        this.songEntityList = songEntityList;

        this.songEntityListFull = new ArrayList<>(songEntityList);

//        if (songEntityList == null){
//            this.songEntityListFull = new ArrayList<>();
//        }
//        else {
//            this.songEntityListFull = new ArrayList<>(songEntityList);
//        }

    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View songView = LayoutInflater.from(context).inflate(R.layout.item_melody, parent, false);
        return new SongViewHolder(songView, songClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        holder.songTitleTextView.setText(songEntityList.get(position).getSongTitle());
        holder.singerTextView.setText(songEntityList.get(position).getSingerTitle());
        holder.songPriorityTextView.setText(String.valueOf(songEntityList.get(position).getSongPriority()));

        if (songEntityList.get(position).isActive()){
            holder.songIsActiveImageView.setColorFilter(ContextCompat.getColor(context,
                    R.color.colorSelectedTextAndIcon), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        else {
            holder.songIsActiveImageView.setColorFilter(ContextCompat.getColor(context,
                    R.color.colorSelectedWidgetBackground), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        //SongEntity currentSongEntity = songEntityList.get(position);

//        for (SongEntity song : songEntityActiveList){
//            if (song.getSongTitle().equals(currentSongEntity.getSongTitle())){
//                //Present, so it is active
//                holder.songIsActiveImageView.setColorFilter(ContextCompat.getColor(context,
//                        R.color.colorSelectedTextAndIcon), android.graphics.PorterDuff.Mode.SRC_IN);
//            }
//        }

    }

    @Override
    public int getItemCount() {
        return songEntityList.size();
        //return 0;
    }

    public void filterList(ArrayList<SongEntity> filteredList){
        songEntityList = filteredList;
        notifyDataSetChanged();
    }

    public void setOnSongItemClickListener(OnSongItemClickListener listener){
        this.songClickListener = listener;
    }

}

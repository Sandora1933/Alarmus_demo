package com.example.alarmus_demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alarmus_demo.R;
import com.example.alarmus_demo.adapter.InfoCardAdapter;
import com.example.alarmus_demo.model.CardInfoEntity;

import java.util.ArrayList;
import java.util.List;

public class InfoActivity extends AppCompatActivity {

    //************  Views  ****************

    //RecyclerView - infoCards
    RecyclerView cardInfoRecyclerView;
    InfoCardAdapter infoCardRecyclerViewAdapter;
    RecyclerView.LayoutManager infoCardRecyclerViewManager;

    //**************   Variables   *****************

    private List<CardInfoEntity> cardInfoEntityList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_new);

        cardInfoEntityList = new ArrayList<CardInfoEntity>();
        fillCardInfoListWithData();

        cardInfoRecyclerView = findViewById(R.id.infoRecyclerView);
        setUpRecyclerView();
    }

    private void fillCardInfoListWithData(){
        cardInfoEntityList.add(new CardInfoEntity("Alarmus title number 1",
                "http:'//helloworld.com/articles/why", "This is short info for the article, this is short info!"));
        cardInfoEntityList.add(new CardInfoEntity("Alarmus title number 2",
                "http:'//helloworld.com/articles/why", "This is short info for the article, this is short info!"));
        cardInfoEntityList.add(new CardInfoEntity("Alarmus title number 3",
                "http:'//helloworld.com/articles/why", "This is short info for the article, this is short info!"));
        cardInfoEntityList.add(new CardInfoEntity("Alarmus title number 4",
                "http:'//helloworld.com/articles/why", "This is short info for the article, this is short info!"));
        cardInfoEntityList.add(new CardInfoEntity("Alarmus title number 5",
                "http:'//helloworld.com/articles/why", "This is short info for the article, this is short info!"));
        cardInfoEntityList.add(new CardInfoEntity("Alarmus title number 6",
                "http:'//helloworld.com/articles/why", "This is short info for the article, this is short info!"));
        cardInfoEntityList.add(new CardInfoEntity("Alarmus title number 7",
                "http:'//helloworld.com/articles/why", "This is short info for the article, this is short info!"));
        cardInfoEntityList.add(new CardInfoEntity("Alarmus title number 8",
                "http:'//helloworld.com/articles/why", "This is short info for the article, this is short info!"));
        cardInfoEntityList.add(new CardInfoEntity("Alarmus title number 9",
                "http:'//helloworld.com/articles/why", "This is short info for the article, this is short info!"));
    }

    private void setUpRecyclerView() {
        infoCardRecyclerViewAdapter = new InfoCardAdapter(this, cardInfoEntityList);
        infoCardRecyclerViewManager = new LinearLayoutManager(this);

        cardInfoRecyclerView.setAdapter(infoCardRecyclerViewAdapter);
        cardInfoRecyclerView.setLayoutManager(infoCardRecyclerViewManager);
        cardInfoRecyclerView.setHasFixedSize(true);
    }

    //********   activity transition   *********

    public void musicMenuButtonClicked(View view) {
        Intent musicListActivityIntent = new Intent(InfoActivity.this, MusicListActivity.class);
        startActivity(musicListActivityIntent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void alarmMenuButtonClicked(View view){
        Intent alarmMenuActivityIntent = new Intent(InfoActivity.this, AlarmActivity.class);
        startActivity(alarmMenuActivityIntent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    //------------------------------------------

    public void telegramButtonClicked(View view) {
        Toast.makeText(this, "telegram clicked", Toast.LENGTH_SHORT).show();
    }

    public void emailButtonClicked(View view) {
        Intent alarmStartIntent = new Intent(InfoActivity.this, StartAlarmActivity.class);
        startActivity(alarmStartIntent);
    }
}

package com.example.alarmus_demo.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alarmus_demo.R;
import com.example.alarmus_demo.adapter.InfoCardAdapter;
import com.example.alarmus_demo.adapter.SongAdapter;
import com.example.alarmus_demo.model.CardInfoEntity;

import java.util.ArrayList;
import java.util.List;

public class TestingActivity extends AppCompatActivity {

    List<CardInfoEntity> infoEntityList;

    //RecyclerView
    RecyclerView recyclerView;
    InfoCardAdapter infoAdapter;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        fillListWithData();
        setUpRecyclerView();

    }

    private void fillListWithData(){
        infoEntityList = new ArrayList<>();
        infoEntityList.add(new CardInfoEntity("Alarmus title number 1",
                "http:'//helloworld.com/articles/why", "This is short info for the article, this is short info!"));
        infoEntityList.add(new CardInfoEntity("Alarmus title number 2",
                "http:'//helloworld.com/articles/why", "This is short info for the article, this is short info!"));
        infoEntityList.add(new CardInfoEntity("Alarmus title number 3",
                "http:'//helloworld.com/articles/why", "This is short info for the article, this is short info!"));
        infoEntityList.add(new CardInfoEntity("Alarmus title number 4",
                "http:'//helloworld.com/articles/why", "This is short info for the article, this is short info!"));
        infoEntityList.add(new CardInfoEntity("Alarmus title number 5",
                "http:'//helloworld.com/articles/why", "This is short info for the article, this is short info!"));
        infoEntityList.add(new CardInfoEntity("Alarmus title number 6",
                "http:'//helloworld.com/articles/why", "This is short info for the article, this is short info!"));
        infoEntityList.add(new CardInfoEntity("Alarmus title number 7",
                "http:'//helloworld.com/articles/why", "This is short info for the article, this is short info!"));
        infoEntityList.add(new CardInfoEntity("Alarmus title number 8",
                "http:'//helloworld.com/articles/why", "This is short info for the article, this is short info!"));
        infoEntityList.add(new CardInfoEntity("Alarmus title number 9",
                "http:'//helloworld.com/articles/why", "This is short info for the article, this is short info!"));
    }

    private void setUpRecyclerView(){
        recyclerView = findViewById(R.id.recyclerView);
        infoAdapter = new InfoCardAdapter(this, infoEntityList);
        layoutManager = new LinearLayoutManager(this);

        recyclerView.setAdapter(infoAdapter);
        recyclerView.setLayoutManager(layoutManager);
    }


}

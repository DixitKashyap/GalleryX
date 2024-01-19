package com.dixitkumar.galleryxapp.AlbumFragment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.icu.math.BigDecimal;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dixitkumar.galleryxapp.MainActivity;
import com.dixitkumar.galleryxapp.PhotosFragment.Photos_Fragment;
import com.dixitkumar.galleryxapp.R;
import com.dixitkumar.galleryxapp.databinding.ActivityAllVideoListBinding;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AllVideoListActivity extends AppCompatActivity {

    private VideoRecyclerviewAdapter adapter;
    private ArrayList<Video> videoArrayList = new ArrayList<>();
    private ActivityAllVideoListBinding videoListBinding;
    private int sortValue = 0;
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoListBinding = ActivityAllVideoListBinding.inflate(getLayoutInflater());
        setContentView(videoListBinding.getRoot());

        //Setting Up The RecyclerView
        videoListBinding.videoRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        videoArrayList = Album_Fragment.videoArrayList;

        //Setting Up The Adapter
        adapter = new VideoRecyclerviewAdapter(AllVideoListActivity.this,videoArrayList);
        videoListBinding.videoRecyclerView.setAdapter(adapter);


               //Setting Up The Video Search View
        videoListBinding.searchViewVideos.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchFilter(newText);
                return true;
            }
        });

    }


    private void searchFilter(String nextText){
        ArrayList<Video> filteredList = new ArrayList<>();
        for(Video video : videoArrayList){
            if(video.getTitle().toLowerCase().contains(nextText.toLowerCase())){
                filteredList.add(video);
            }
        }
        if(filteredList.isEmpty()){
            Toast.makeText(this, "No Videos Found", Toast.LENGTH_SHORT).show();
        }else{
            adapter.setFilteredList(filteredList);
        }
    }

}
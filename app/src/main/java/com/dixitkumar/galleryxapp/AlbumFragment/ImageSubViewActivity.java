package com.dixitkumar.galleryxapp.AlbumFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.dixitkumar.galleryxapp.PhotosFragment.Images;
import com.dixitkumar.galleryxapp.R;
import com.dixitkumar.galleryxapp.databinding.ActivityImageSubViewBinding;

import java.util.ArrayList;

public class ImageSubViewActivity extends AppCompatActivity {

    private ActivityImageSubViewBinding viewBinding;
    private ImageSubViewAdapter adapter;
    private ArrayList<Images> imagesArrayList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityImageSubViewBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        Intent i = getIntent();
        String title = i.getStringExtra("ALBUM_TITLE");

        //Updating Data Based On Click
        if(title!=null){
            viewBinding.imageTitle.setText(title);
            UpdateData(title);
        }

        adapter = new ImageSubViewAdapter(this,imagesArrayList);
        viewBinding.imageViewerRecyclerview.setLayoutManager(new GridLayoutManager(this,3));
        viewBinding.imageViewerRecyclerview.setAdapter(adapter);

        viewBinding.backButton.setOnClickListener(view -> {
            onBackPressed();
        });

    }
    private void UpdateData(String title){
        imagesArrayList.clear();
        switch (title){
            case "Screenshots":
                imagesArrayList = Album_Fragment.allScreenShots;
                break;
            case "CameraPhotos":
                imagesArrayList = Album_Fragment.cameraPhotos;
                break;
            case "GalleryXApp" :
                imagesArrayList = Album_Fragment.galleryXApp;
                break;
            case "LocalStorageImages" :
                imagesArrayList = Album_Fragment.localStorage;
                break;
            case "Cropped Images" :
                imagesArrayList = Album_Fragment.croppedImages;
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}
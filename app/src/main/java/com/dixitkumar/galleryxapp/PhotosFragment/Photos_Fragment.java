package com.dixitkumar.galleryxapp.PhotosFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalZeroShutterLag;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dixitkumar.galleryxapp.AlbumFragment.Video;
import com.dixitkumar.galleryxapp.MainActivity;
import com.dixitkumar.galleryxapp.R;
import com.dixitkumar.galleryxapp.databinding.FragmentPhotosBinding;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;

public class Photos_Fragment extends Fragment {

    private FragmentPhotosBinding photosBinding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        photosBinding = FragmentPhotosBinding.inflate(getLayoutInflater());



        //Opening Camera on The Click Of Open Camera Button
        photosBinding.openCamera.setOnClickListener(v ->{
            openCamera();
        });

        return photosBinding.getRoot();
    }


    @OptIn(markerClass = ExperimentalZeroShutterLag.class) private void openCamera(){
        Intent intent = new Intent(getContext(), Camera_ViewFinderActivity.class);
        startActivity(intent);
    }
}
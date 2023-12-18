package com.dixitkumar.galleryxapp.PhotosFragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dixitkumar.galleryxapp.R;
import com.dixitkumar.galleryxapp.databinding.FragmentPhotosBinding;

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

    private void openCamera(){

    }
}
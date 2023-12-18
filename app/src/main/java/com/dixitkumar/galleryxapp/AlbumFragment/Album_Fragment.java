package com.dixitkumar.galleryxapp.AlbumFragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dixitkumar.galleryxapp.R;
import com.dixitkumar.galleryxapp.databinding.FragmentAlbumBinding;

public class Album_Fragment extends Fragment {

   private FragmentAlbumBinding albumBinding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        albumBinding = FragmentAlbumBinding.inflate(getLayoutInflater());
        return albumBinding.getRoot();
    }
}
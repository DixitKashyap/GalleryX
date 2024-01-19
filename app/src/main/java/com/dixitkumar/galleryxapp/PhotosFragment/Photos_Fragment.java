package com.dixitkumar.galleryxapp.PhotosFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalZeroShutterLag;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dixitkumar.galleryxapp.AlbumFragment.Album_Fragment;
import com.dixitkumar.galleryxapp.AlbumFragment.Video;
import com.dixitkumar.galleryxapp.MainActivity;
import com.dixitkumar.galleryxapp.R;
import com.dixitkumar.galleryxapp.databinding.FragmentPhotosBinding;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;

public class Photos_Fragment extends Fragment {

    private FragmentPhotosBinding photosBinding;

    private ArrayList<Images> imagesArrayList =new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        photosBinding = FragmentPhotosBinding.inflate(getLayoutInflater());
        //Getting All Images From Storage
        getAllImages();
        photosBinding.photosRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        PhotosAdapter adapter = new PhotosAdapter(getContext(),imagesArrayList);
        photosBinding.photosRecyclerView.setAdapter(adapter);
        //Opening Camera on The Click Of Open Camera Button
        photosBinding.openCamera.setOnClickListener(v ->{
            openCamera();
        });

        return photosBinding.getRoot();
    }

    private void getAllImages(){
        String[] projection = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_TAKEN,
        };

        Cursor cursor = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            cursor = getContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null);
        }

        if (cursor != null) {
            if (cursor.moveToNext()) {
                do {
                    @SuppressLint("Range") String data = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    @SuppressLint("Range") String dateTaken = cursor.getColumnName(cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));

                    try {
                        File file = new File(data);
                        String folderName = file.getParentFile().getName();
                        Uri string = Uri.fromFile(file);

                        if (file.exists()) {
                            Images images = new Images(string.getPath(),folderName,dateTaken);
                            imagesArrayList.add(images);
                        }

                    } catch (Exception e) {

                    }

                } while (cursor.moveToNext());
                cursor.close();
            }
        }
    }
    @OptIn(markerClass = ExperimentalZeroShutterLag.class) private void openCamera(){
        Intent intent = new Intent(getContext(), Camera_ViewFinderActivity.class);
        startActivity(intent);
    }
}
package com.dixitkumar.galleryxapp.AlbumFragment;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dixitkumar.galleryxapp.PhotosFragment.Images;
import com.dixitkumar.galleryxapp.PhotosFragment.Photos_Fragment;
import com.dixitkumar.galleryxapp.R;
import com.dixitkumar.galleryxapp.databinding.FragmentAlbumBinding;

import java.io.File;
import java.util.ArrayList;

public class Album_Fragment extends Fragment {

//    private int totalNo_Of_Videos = Photos_Fragment.videoArrayList.size();
    private FragmentAlbumBinding albumBinding;
    public static Uri artUri;
    private int totalVideos =0;
    public static ArrayList<Video> videoArrayList = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        albumBinding = FragmentAlbumBinding.inflate(getLayoutInflater());

        if(requestRuntimePermission()){
            requestRuntimePermission();
        }else {
            //Getting All The video From Storage
            videoArrayList = getAllVideo();
            totalVideos = videoArrayList.size();
            artUri = videoArrayList.get(totalVideos-1).getArtUri();
            albumBinding.videoThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            albumBinding.totalVideoNumber.setText(totalVideos+"");
            Glide.with(getContext())
                    .load(artUri)
                    .apply(RequestOptions.placeholderOf(R.color.black))
                    .into(albumBinding.videoThumbnail);
        }
        albumBinding.getRoot().setOnClickListener(view -> {
          getContext().startActivity(new Intent(getContext(), AllVideoListActivity.class));
        });
            return albumBinding.getRoot();
  }
    private  boolean requestRuntimePermission(){
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},13);
            return  false;
        }
        return true;
    }

    @SuppressLint("Range")
    private  ArrayList<Video> getAllVideo() {
        ArrayList<Video> tempList= new ArrayList<>();
        String []projection = {
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DURATION,
        };



        Cursor cursor = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            cursor = getContext().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,projection,null,null);
        }

        if (cursor!=null){
            if(cursor.moveToNext()){
                do{


                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                    String size = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                    String id = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                    String bucket_display_name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                    String data = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                    Long duration = Long.parseLong(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)));


                    try{
                        File file = new File(data);

                        artUri = Uri.fromFile(file);
                        Video video = new Video(id,title,duration,bucket_display_name,size,data,artUri);

                        if(file.exists()){
                            tempList.add(video);
                        }
                    }catch (Exception e){

                    }

                }while (cursor.moveToNext());
                cursor.close();
            }
        }

        return tempList;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 13){
            if(grantResults.length !=0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                //Initializing the layout just after getting the permission
            }else{
                requestRuntimePermission();
                ActivityCompat.requestPermissions((Activity) getContext(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},13);
            }
        }
    }
}
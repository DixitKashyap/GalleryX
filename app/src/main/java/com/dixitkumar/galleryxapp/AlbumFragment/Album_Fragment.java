package com.dixitkumar.galleryxapp.AlbumFragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

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

import java.util.ArrayList;

public class Album_Fragment extends Fragment {

    private FragmentAlbumBinding albumBinding;
    public static Uri artUri;
    private int totalVideos =0;
    protected ArrayList<Images> allImageList = Photos_Fragment.imagesArrayList;
    protected static ArrayList<Images> allScreenShots = new ArrayList<>();
    protected static ArrayList<Images> cameraPhotos = new ArrayList<>();
    protected static ArrayList<Images> galleryXApp = new ArrayList<>();
    protected static ArrayList<Images> localStorage = new ArrayList<>();
    protected static ArrayList<Images> croppedImages = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        albumBinding = FragmentAlbumBinding.inflate(getLayoutInflater());

        if(!requestRuntimePermission()){
            inatializeLayout();
            for(int i=0;i<allImageList.size();i++){
                String folder_Name = allImageList.get(i).getFolderName();
                catogeriesImages(folder_Name,allImageList.get(i));
            }


            //Setting Up the Intent
            Intent intent = new Intent(getContext(), ImageSubViewActivity.class);
            if(allScreenShots.size()!=0){
                albumBinding.allScreenShots.setScaleType(ImageView.ScaleType.CENTER_CROP);
                albumBinding.AllScreenShotsNumber.setText(allScreenShots.size()+"");
                Glide.with(getContext())
                        .load(allScreenShots.get(allScreenShots.size()-1).getPath())
                        .apply(RequestOptions.placeholderOf(R.color.black))
                        .into(albumBinding.allScreenShots);

                //Setting Up The Listener
                albumBinding.screenShotLayout.setOnClickListener(view -> {
                    intent.putExtra("ALBUM_TITLE","Screenshots");
                    startActivity(intent);
                });
            }else{
                albumBinding.screenShotLayout.setVisibility(View.GONE);
            }

            if(cameraPhotos.size()!=0){
                albumBinding.allCameraImages.setScaleType(ImageView.ScaleType.CENTER_CROP);
                albumBinding.AllCameraImagesNumber.setText(cameraPhotos.size()+"");
                Glide.with(getContext())
                        .load(cameraPhotos.get(cameraPhotos.size()-1).getPath())
                        .apply(RequestOptions.placeholderOf(R.color.black))
                        .into(albumBinding.allCameraImages);

                albumBinding.cameraImageLayout.setOnClickListener(view -> {
                    intent.putExtra("ALBUM_TITLE","CameraPhotos");
                    startActivity(intent);
                });

            }else {
            albumBinding.cameraImageLayout.setVisibility(View.GONE);
            }

            if(galleryXApp.size()!=0){

                albumBinding.allGalleryXImages.setScaleType(ImageView.ScaleType.CENTER_CROP);
                albumBinding.allGalleryXImageNumber.setText(galleryXApp.size()+"");
                Glide.with(getContext())
                        .load(galleryXApp.get(galleryXApp.size()-1).getPath())
                        .apply(RequestOptions.placeholderOf(R.color.black))
                        .into(albumBinding.allGalleryXImages);

                albumBinding.galleryXImageLayout.setOnClickListener(view -> {
                    intent.putExtra("ALBUM_TITLE","GalleryXApp");
                    startActivity(intent);
                });

            }else{
               albumBinding.galleryXImageLayout.setVisibility(View.GONE);
            }
            if(localStorage.size()!=0){
                albumBinding.allLocalStorageImages.setScaleType(ImageView.ScaleType.CENTER_CROP);
                albumBinding.localStorageNumber.setText(localStorage.size()+"");
                Glide.with(getContext())
                        .load(localStorage.get(localStorage.size()-1).getPath())
                        .apply(RequestOptions.placeholderOf(R.color.black))
                        .into(albumBinding.allLocalStorageImages);

                albumBinding.localImageLayout.setOnClickListener(view -> {
                    intent.putExtra("ALBUM_TITLE","LocalStorageImages");
                    startActivity(intent);
                });

            }else{
                albumBinding.localImageLayout.setVisibility(View.GONE);
            }
            if(croppedImages.size()!=0){

                albumBinding.allCroppedImages.setScaleType(ImageView.ScaleType.CENTER_CROP);
                albumBinding.allCroppedImagesNumber.setText(croppedImages.size()+"");
                Glide.with(getContext())
                        .load(croppedImages.get(croppedImages.size()-1).getPath())
                        .apply(RequestOptions.placeholderOf(R.color.black))
                        .into(albumBinding.allCroppedImages);

                albumBinding.croppedImageLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        intent.putExtra("ALBUM_TITLE","Cropped Images");
                        startActivity(intent);
                    }
                });
            }else{
                albumBinding.croppedImageLayout.setVisibility(View.GONE);
            }
        }
         return albumBinding.getRoot();
  }

    private void catogeriesImages(String folder_Name,Images images) {
        switch (folder_Name){
            case "Screenshots":
                allScreenShots.add(images);
                break;
            case "GalleryXApp":
                galleryXApp.add(images);
                break;
            case "0" :
                localStorage.add(images);
                break;
            case "Camera":
                cameraPhotos.add(images);
                break;
            case "CroppedImages":
                croppedImages.add(images);
                break;
            default:
                Log.d("TAG","Nothings Here");
                break;
        }
    }

    private void inatializeLayout(){
      totalVideos = Photos_Fragment.AllVideoList.size();
      if(totalVideos!=0) {
          artUri = Photos_Fragment.AllVideoList.get(totalVideos - 1).getArtUri();
          albumBinding.videoThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
          albumBinding.totalVideoNumber.setText(totalVideos + "");
          Glide.with(getContext())
                  .load(artUri)
                  .apply(RequestOptions.placeholderOf(R.color.black))
                  .into(albumBinding.videoThumbnail);


          albumBinding.videoThumbnail.setOnClickListener(view -> {
              getContext().startActivity(new Intent(getContext(), AllVideoListActivity.class));
          });
      }else{
          albumBinding.videosLayout.setVisibility(View.GONE);
      }
  }


    private  boolean requestRuntimePermission(){
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},13);
            return  false;
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 13){
            if(grantResults.length !=0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                //Initializing the layout just after getting the permission
                inatializeLayout();
            }else{
                ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},13);
            }
        }
    }
}
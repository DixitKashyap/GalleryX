package com.dixitkumar.galleryxapp.PhotosFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.impl.utils.ExifData;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.dixitkumar.galleryxapp.ImageViewerActivity;
import com.dixitkumar.galleryxapp.R;
import com.dixitkumar.galleryxapp.databinding.ActivityImageDetailBinding;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class image_detail_Activity extends AppCompatActivity {

    private Intent i;
    private String imagePath = "";
    ActivityImageDetailBinding imageDetailBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageDetailBinding = ActivityImageDetailBinding.inflate(getLayoutInflater());
        setContentView(imageDetailBinding.getRoot());

        //Getting Image Uri
        i = getIntent();
        imagePath = i.getStringExtra("IMAGE_URI");
        File imageFile = new File(imagePath);
        //Getting EXIF Data of The Image
         String exif = getExifData(imageFile);

        //Setting Up The ImageDetail
        String imageData = ImageViewerActivity.getImageDate(imagePath);
        String imageTakenTime = ImageViewerActivity.formatDate(imageData)+"\n"+ImageViewerActivity.formatTime(imageData);
        imageDetailBinding.imageTakenTime.setText(imageTakenTime);


        //Setting Up The Image Name Details
        String imageName = imageFile.getName()+"\n";
        imageDetailBinding.imageInfo.setText(imageName);


        //Setting up the local path
        String imagePath = imageFile.getPath();
        imageDetailBinding.filePath.setText(imagePath);


        //Setting up the image size and resolution
        long imageSizeInBytes = imageFile.length();
        double imageSizeInKiloBytes = (double)imageSizeInBytes/1024;
        double imageSizeInMegaBytes = (double)imageSizeInKiloBytes/1024;
        String imageSize = String.valueOf(imageSizeInMegaBytes).substring(0,4)+"MB";

        //Getting Image Resolution
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getPath());
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        String resolution = width +" x "+height+"px";
        imageDetailBinding.imageResAndSize.setText(imageSize+"  "+resolution);

        //Setting Up the Exif Data of The Image
        imageDetailBinding.exifContent.setText(exif);
        //setting Up The Back Button
        imageDetailBinding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });


    }
    private String getImageData(String imagePath){
        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);
            String imageInfo =  exifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION);
            return imageInfo;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    private String getExifData(File imageFile) {
        String ExifData = null;
        try {
            Uri uri = Uri.fromFile(imageFile); // the URI you've received from the other app
            InputStream in = getContentResolver().openInputStream(uri);
            ExifInterface exifInterface = new ExifInterface(in);

            String model = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
            String aperture = exifInterface.getAttribute(ExifInterface.TAG_APERTURE);
            String iso = exifInterface.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS);
            String focalLength = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
            String flash = exifInterface.getAttribute(ExifInterface.TAG_FLASH);

            ExifData = "Model : " + model + "\n" + "Aperture : " + aperture + "\n" + "ISO : " + iso + "\n" + "Focal Length : " + focalLength + "\n" + "Flash : " + flash + "";
            return ExifData;
        } catch (IOException e) {
            e.printStackTrace();
        }
       return  null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
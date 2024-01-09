package com.dixitkumar.galleryxapp;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import com.dixitkumar.galleryxapp.PhotosFragment.image_detail_Activity;
import com.dixitkumar.galleryxapp.databinding.ActivityImageDetailBinding;
import com.dixitkumar.galleryxapp.databinding.ActivityImageViewerBinding;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageViewerActivity extends AppCompatActivity {

    private Intent i;
    private Uri imageUri;
    private static boolean IS_TOUCHED = false;
    private static String formattedDate = "";
    private static String formattedTime = "";
   private ActivityImageViewerBinding imageViewerBinding ;
   private int imageRotation = 0;
   private String filePath="";
    int imageNumber=1;


   private static int REQUEST_CODE_DELETE = 12;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageViewerBinding = ActivityImageViewerBinding.inflate(getLayoutInflater());
        setContentView(imageViewerBinding.getRoot());

        //getting image data
        i = getIntent();
        String uri = i.getStringExtra("IMAGE_URI");
        imageUri = Uri.parse(uri);
        imageViewerBinding.myZoomageView.setImageURI(imageUri);

       //Setting Up date and Time
       String imageData = getImageDate(uri);
       //Setting up Date
        if(formattedDate!=null) {
            formattedDate = formatDate(imageData);
            imageViewerBinding.imageClickedDate.setText(formattedDate);
        }
        //Setting Up Time
        if(formattedTime!=null){
            formattedTime = formatTime(imageData);
            imageViewerBinding.imageClickedTime.setText(formattedTime);
        }

        //Setting on Back Button Listener
        imageViewerBinding.backButton.setOnClickListener(view -> onBackPressed());

        //Setting up image rotation Listener
        imageViewerBinding.rotateImageButton.setOnClickListener(view -> {
            imageRotation += 90;
            try {
                filePath = i.getStringExtra("IMAGE_URI");
                FileInputStream inputStream = new FileInputStream(filePath);
                Log.d("TAG",inputStream.toString());
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                // Create a new matrix
                Matrix matrix = new Matrix();

                // Rotate the bitmap by 90 degrees
                matrix.postRotate(imageRotation);

                // Create a new bitmap with the same dimensions as the original bitmap, but rotated
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                imageViewerBinding.myZoomageView.setImageBitmap(rotatedBitmap);
            } catch (FileNotFoundException e) {
                Log.d("TAG",e.toString());
                throw new RuntimeException(e);
            }
        });

        //Setting up the Image Share Button
        imageViewerBinding.shareImageButton.setOnClickListener(view ->{
            if(imageUri!=null){
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("image/*");
                i.putExtra(Intent.EXTRA_STREAM,imageUri);
                startActivity(Intent.createChooser(i,"Share Image Using :"));
            }else{
                Toast.makeText(this, "Not Valid Image Reference", Toast.LENGTH_SHORT).show();
            }
        });


        //Setting Up The Info Button
        imageViewerBinding.infoButton.setOnClickListener(view -> {
           Intent i = new Intent(ImageViewerActivity.this, image_detail_Activity.class);
           i.putExtra("IMAGE_URI",imageUri.toString());
           startActivity(i);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //For Formatting DateAndTime
     public static String formatDate(String str) {
        SimpleDateFormat readFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        Date date = null;
        try {
            date = readFormat.parse(str);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        SimpleDateFormat writeFormat = new SimpleDateFormat("MMMM d,yyyy ");
        String formattedDate = writeFormat.format(date);
        return formattedDate;
    }

    public static String formatTime(String str){
        SimpleDateFormat readFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        Date date = null;
        try {
            date = readFormat.parse(str);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        SimpleDateFormat writeFormat = new SimpleDateFormat("HH:mm");
        String formattedTime = writeFormat.format(date);

        return formattedTime;
    }



    public static String getImageDate(String imagePath) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            return exif.getAttribute(ExifInterface.TAG_DATETIME);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}

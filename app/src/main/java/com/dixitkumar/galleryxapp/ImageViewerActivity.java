package com.dixitkumar.galleryxapp;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import com.dixitkumar.galleryxapp.PhotosFragment.TextTranslatorActivity;
import com.dixitkumar.galleryxapp.databinding.ActivityImageViewerBinding;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ImageViewerActivity extends AppCompatActivity {

    private Intent i;
    private Uri imageUri;
    private static boolean IS_TOUCHED = false;
    private static String formattedDate = "";
    private static String formattedTime = "";
   private ActivityImageViewerBinding imageViewerBinding ;
   private int imageRotation = 0;
   private String filePath="";
   private File imageFile ;
   private  Bitmap bitmap;
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
        Log.d("TAG",uri);
        imageFile = new File(uri);
        imageUri = Uri.parse(uri);
        imageViewerBinding.myZoomageView.setImageURI(imageUri);


        //Setting Up Folder Name
        imageViewerBinding.imageClickedDate.setText(imageFile.getParentFile().getName()+"");
         //Setting on Back Button Listener
        imageViewerBinding.backButton.setOnClickListener(view -> onBackPressed());

        //Setting up image rotation Listener
        imageViewerBinding.rotateImageButton.setOnClickListener(view -> {
            imageRotation += 90;
            try {
                filePath = i.getStringExtra("IMAGE_URI");
                FileInputStream inputStream = new FileInputStream(filePath);
                Log.d("TAG",inputStream.toString());
                bitmap = BitmapFactory.decodeStream(inputStream);

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

        //Setting Up The Image Edit Button
        imageViewerBinding.translateImageButton.setOnClickListener(view -> {
           Intent i = new Intent(ImageViewerActivity.this, TextTranslatorActivity.class);
           i.putExtra("IMAGE_URI",imageUri.toString());
           startActivity(i);
        });

        imageViewerBinding.cropImageButton.setOnClickListener(view -> {
            CropImage.activity(Uri.fromFile(imageFile)).start(ImageViewerActivity.this);
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri uri = result.getUri();

                try {
                    Bitmap imageBitmap;
                    OutputStream outputStream;

                    FileInputStream inputStream = new FileInputStream(uri.getPath());
                    imageBitmap = BitmapFactory.decodeStream(inputStream);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                        ContentResolver resolver = getContentResolver();
                        ContentValues contentValues = new ContentValues();

                        String name = System.currentTimeMillis() + "";
                        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "Image_" + name + ".jpg");
                        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "CroppedImages");

                        Uri croppedImageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

                        outputStream = resolver.openOutputStream(Objects.requireNonNull(croppedImageUri));

                        imageBitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                        Objects.requireNonNull(outputStream);
                        Toast.makeText(this, "Image Saved", Toast.LENGTH_SHORT).show();

                    }

                }catch (Exception e){

                }

            } else {
                Toast.makeText(this, "Resulted Error", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Error While Cropping The Image", Toast.LENGTH_SHORT).show();
        }
        }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

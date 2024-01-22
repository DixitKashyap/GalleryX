package com.dixitkumar.galleryxapp.PhotosFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalZeroShutterLag;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dixitkumar.galleryxapp.ImageViewerActivity;
import com.dixitkumar.galleryxapp.R;
import com.dixitkumar.galleryxapp.databinding.ActivityCameraViewfinderBinding;
import com.dixitkumar.galleryxapp.databinding.QrCodeLayoutBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

@ExperimentalZeroShutterLag public class Camera_ViewFinderActivity extends AppCompatActivity {
     int cameraFacing = CameraSelector.LENS_FACING_BACK;
  ActivityCameraViewfinderBinding viewFinderBinding;

    private   ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;
    private CameraControl cameraControl;
    private ScaleGestureDetector scaleGestureDetector;
    private CameraInfo cameraInfo;
    private CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
    private ExecutorService cameraExecutor;
    private Handler handler = new Handler();
    private MediaPlayer cameraTime,captureSound;
    private static boolean IS_ENABLED = false;
    private static String LANGUAGE_CODE = "";
    private static boolean IS_TRANSLATION_MODE_ON = false;
     Uri imageUri;
    TextRecognizer textRecognizer;

    private ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        startCamera(cameraFacing);
                    }
                }
            }
    );
//    Creating Bottom Sheet Fragment Object For Menu
    private BottomSheetDialog dialog;
//    Creating Dialog  Object

    //Camera QrCode Bottom Sheet Fragment
    private BottomSheetDialog QrCodeDialog;
    private QrCodeLayoutBinding qrCodeLayoutBinding;


    //Translator Bottom Sheet Fragment

    private BottomSheetDialog translatorDialog;


    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewFinderBinding = ActivityCameraViewfinderBinding.inflate(getLayoutInflater());
        setTheme(R.style.CameraTheme);
        setContentView(viewFinderBinding.getRoot());

        takeKeyEvents(true);
        //setting up the camera Preview
        setPreview();
        //Check The Necessary Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.CAMERA);
        } else {
            startCamera(cameraFacing);
        }


      //Setting up Camera Settings Button
        viewFinderBinding.opnCameraSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Currently not Available", Toast.LENGTH_SHORT).show();
        });

        //Camera QR Code Layout

        viewFinderBinding.cameraQrCode.setOnClickListener(view -> {

            IntentIntegrator intentIntegrator = new IntentIntegrator(this);
            intentIntegrator.setPrompt("Scan a barcode or QR Code");
            intentIntegrator.setOrientationLocked(true);
            intentIntegrator.setBarcodeImageEnabled(true);
            intentIntegrator.initiateScan();

        });

        //Handling Click On Video Activity Button
        viewFinderBinding.videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              startActivity(new Intent(Camera_ViewFinderActivity.this, VideoViewActivity.class));
            }
        });

        //Capture Image after Specific Time delay
         viewFinderBinding.cameraTimer.setOnClickListener(view ->{
             if(!IS_ENABLED &&  !IS_TRANSLATION_MODE_ON){
                 viewFinderBinding.cameraTimer.setImageResource(R.drawable.time_off_icon);
                 IS_ENABLED = true;
             }else{
                 viewFinderBinding.cameraTimer.setImageResource(R.drawable.time_burst_icon);
                 IS_ENABLED = false;
             }
         });

        //Setting Up Picture View Feature
        viewFinderBinding.viewPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageUri!=null){
                    Intent i = new Intent(Camera_ViewFinderActivity.this, ImageViewerActivity.class);
                    i.putExtra("IMAGE_URI",imageUri.toString());
                    startActivity(i);
                }else{
                    Toast.makeText(Camera_ViewFinderActivity.this, "Image Uri is Not", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void setIsEnabled(boolean IS_ENABLED){
        if(IS_ENABLED) {
            cameraTime = MediaPlayer.create(Camera_ViewFinderActivity.this, R.raw.camera_time_sound);
            cameraTime.start();
            cameraTime.setLooping(true);
            handler.postDelayed(() -> {
                captureImage(imageCapture);
                cameraTime.stop();
            },5000);
        }else{
            captureImage(imageCapture);
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                onBackPressed();
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    @SuppressLint("RestrictedApi")
    protected void startCamera(int cameraFacing) {
        int aspectRatio = aspectRatio(viewFinderBinding.viewFinder.getWidth(), viewFinderBinding.viewFinder.getHeight());
        ListenableFuture<ProcessCameraProvider> listenableFuture = ProcessCameraProvider.getInstance(this);

        listenableFuture.addListener(() -> {
            try {
                 cameraProvider = listenableFuture.get();
                Preview preview = new Preview.Builder().setTargetAspectRatio(aspectRatio).build();

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .setTargetRotation(viewFinderBinding.viewFinder.getDisplay().getRotation()).build();

                cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing).build();

                cameraProvider.unbindAll();

                preview.setSurfaceProvider(viewFinderBinding.viewFinder.getSurfaceProvider());
                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

                //For Performing operations that affects all outputs
                 cameraControl = camera.getCameraControl();


                //For Querying information and states
                 cameraInfo = camera.getCameraInfo();


                //Camera Timer Option Added
                viewFinderBinding.captureImage.setOnClickListener(view -> {

                    if(IS_ENABLED) {
                        cameraTime = MediaPlayer.create(Camera_ViewFinderActivity.this, R.raw.camera_time_sound);
                        cameraTime.start();
                        cameraTime.setLooping(true);
                        handler.postDelayed((Runnable) () -> {
                            if (ContextCompat.checkSelfPermission(Camera_ViewFinderActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            }
                            captureImage(imageCapture);
                            cameraTime.stop();
                        }, 5000);
                    }else if(IS_TRANSLATION_MODE_ON){
                        captureImage(imageCapture);
                       // Setting up the Translator mode
                        handler.postDelayed((Runnable) () -> {
                            if(imageUri!=null) {
                                Intent i = new Intent(Camera_ViewFinderActivity.this, TextTranslatorActivity.class);
                                i.putExtra("IMAGE_URI", imageUri.toString());
                                startActivity(i);
                            }else{
                                Toast.makeText(this, "Image Uri is Null", Toast.LENGTH_SHORT).show();
                            }
                        },1500);
                    } else {
                        if (ContextCompat.checkSelfPermission(Camera_ViewFinderActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        }
                        captureImage(imageCapture);
                    }
                });

                //To Change Between Front and Back Camera
                viewFinderBinding.cameraFlip.setOnClickListener(view -> {
                    changeCamera(cameraFacing);
                });

                //To Turn Of And On Flash Mode
                viewFinderBinding.flashButton.setOnClickListener(view -> {
                    toggleFlash(camera);
                });

                //Managing Text Extraction and Translation
                cameraTranslatorOn();
                pinchToZoom();
                setUpZoomSlider();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }


    //Function For Managing Text Extraction and Translation
    private void cameraTranslatorOn(){

        viewFinderBinding.translateModeOn.setOnClickListener(view -> {

            if(!IS_TRANSLATION_MODE_ON && imageCapture.getFlashMode()==ImageCapture.FLASH_MODE_OFF && !IS_ENABLED ){
                viewFinderBinding.translateModeOn.setImageResource(R.drawable.translate_icon_on);
                IS_TRANSLATION_MODE_ON = true;
            }else{
                viewFinderBinding.translateModeOn.setImageResource(R.drawable.translate_icon);
                IS_TRANSLATION_MODE_ON = false;
            }
        });
    }


    private void changeCamera(int cameraFacing){
        if(cameraFacing == CameraSelector.LENS_FACING_BACK){
            cameraFacing = CameraSelector.LENS_FACING_FRONT;
        }else{
            cameraFacing = CameraSelector.LENS_FACING_BACK;
        }
        startCamera(cameraFacing);
    }
    //Setting Up Pinch to Zoom
    private void pinchToZoom() {
        //Pinch Zoom Camera
        ScaleGestureDetector.SimpleOnScaleGestureListener listener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                LiveData<ZoomState> ZoomRatio = cameraInfo.getZoomState();
                float currentZoomRatio = 0;
                try {
                    currentZoomRatio = ZoomRatio.getValue().getZoomRatio();
                } catch (NullPointerException e) {

                }
                float linearValue = ZoomRatio.getValue().getLinearZoom();
                float delta = detector.getScaleFactor();
                cameraControl.setZoomRatio(currentZoomRatio * delta);
                float mat = (linearValue) * (100);
                viewFinderBinding.zoombar.setProgress((int) mat);
                return true;
            }
        };

        scaleGestureDetector = new ScaleGestureDetector(getBaseContext(), listener);
    }

    //Setting up the ZoomSlider
    private void setUpZoomSlider(){
        viewFinderBinding.zoombar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float mat = (float) (progress) / (100);
                cameraControl.setLinearZoom(mat);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }


    private void toggleFlash(Camera camera){
        if(camera.getCameraInfo().hasFlashUnit() && !IS_TRANSLATION_MODE_ON){
            if(imageCapture.getFlashMode() == ImageCapture.FLASH_MODE_OFF){
                viewFinderBinding.flashButton.setImageResource(R.drawable.flash_on_icon);
                imageCapture.setFlashMode(ImageCapture.FLASH_MODE_ON);
            }else if(imageCapture.getFlashMode()==ImageCapture.FLASH_MODE_ON){
                viewFinderBinding.flashButton.setImageResource(R.drawable.flash_auto_icon);
                imageCapture.setFlashMode(ImageCapture.FLASH_MODE_AUTO);
            }else if(imageCapture.getFlashMode() == ImageCapture.FLASH_MODE_AUTO){
                viewFinderBinding.flashButton.setImageResource(R.drawable.no_flash_icon);
                imageCapture.setFlashMode(ImageCapture.FLASH_MODE_OFF);
            }
        }else{
          runOnUiThread(()->  Snackbar.make(viewFinderBinding.getRoot(),"Flash Not Available !",Snackbar.LENGTH_SHORT).show());
        }
    }

    private int aspectRatio(int width, int height) {
        double previewRatio = (double) Math.max(width, height) / Math.min(width, height);

        if (Math.abs(previewRatio - 4.0 / 3.0) <= Math.abs(previewRatio - 16.0 / 9.0)) {
            return AspectRatio.RATIO_4_3;
        } else {
            return AspectRatio.RATIO_16_9;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        startCamera(cameraFacing);
    }

    @SuppressLint("RestrictedApi")
    private void captureImage(ImageCapture imageCapture){
        if(imageCapture == null)return;

        String name = System.currentTimeMillis()+"";

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg");

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.P){
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/GalleryXApp");
        }

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(
                getContentResolver(),MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues
        ).build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(Camera_ViewFinderActivity.this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                captureSound = MediaPlayer.create(Camera_ViewFinderActivity.this,R.raw.capture_image_sound);
                captureSound.start();
                setPreview();
                Toast.makeText(Camera_ViewFinderActivity.this,"Image Saved"+outputFileResults.getSavedUri(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(Camera_ViewFinderActivity.this,"Filled ",Toast.LENGTH_SHORT).show();
            }
        });
    }


    protected void setPreview(){
        int i = 1;
        // Get the path of the most recent image from the public storage
        String[] projection = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE
        };
        final Cursor cursor = getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
                        null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");


// Get the first image file
        if (cursor.moveToFirst()) {
            String imageLocation = cursor.getString(i);
            imageUri = Uri.parse(imageLocation);
            File imageFile = new File(imageLocation);
            if (imageFile.exists()) {
                viewFinderBinding.viewPicture.setScaleType(ImageView.ScaleType.CENTER_CROP);
                // Check if the file exists and has read permission
                Glide.with(this)
                        .load(imageLocation)
                        .apply(RequestOptions.placeholderOf(R.drawable.capture_image_preview_shape))
                        .into(viewFinderBinding.viewPicture);
            }
        }
        cursor.close();

    }
    private void makeQrCodeDialogVisible(){
        QrCodeDialog = new BottomSheetDialog(Camera_ViewFinderActivity.this);
        qrCodeLayoutBinding = QrCodeLayoutBinding.inflate(getLayoutInflater());
        qrCodeLayoutBinding.getRoot().setBackgroundColor(ContextCompat.getColor(this,R.color.white));
        QrCodeDialog.setContentView(qrCodeLayoutBinding.getRoot());
        QrCodeDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        // if the intentResult is null then
        // toast a message as "cancelled"
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
               makeQrCodeDialogVisible();

               qrCodeLayoutBinding.cameraQrCodeContent.setText(intentResult.getContents());
                String text_title = String.valueOf(qrCodeLayoutBinding.cameraQrCodeContent.getText());


                //For Sharing Qr Code Data
                qrCodeLayoutBinding.shareInfo.setOnClickListener(view -> {
                  shareQrCodeData(text_title);
                });
               //For Opening Qr Code Link
                qrCodeLayoutBinding.openLink.setOnClickListener(view -> {
                    if(URLUtil.isValidUrl(text_title)){
                        //initializing object for custom chrome tabs
                         CustomTabsIntent.Builder customIntent = new CustomTabsIntent.Builder();

                       //below line is setting toolbar color
                       //for our custom chrome tab
                       customIntent.setToolbarColor(ContextCompat.getColor(this,R.color.black));

                       //we are calling below method after
                        //setting our toolbar color
                        openCustomTab(this, customIntent.build(),Uri.parse(text_title));
                    }else{
                        Toast.makeText(this, "URL is Not Valid ", Toast.LENGTH_SHORT).show();
                    }
                });

                //For Opening Qr Code Link in Your Phone Browser
                qrCodeLayoutBinding.openInBrowser.setOnClickListener(view -> {
                    if(URLUtil.isValidUrl(text_title)){
                        Intent i = new Intent(Intent.ACTION_VIEW,Uri.parse(text_title));
                        startActivity(i);
                    }else{
                        Toast.makeText(this,"URL is Not Valid",Toast.LENGTH_SHORT).show();
                    }
                });


                //For Loading Image from Url
                qrCodeLayoutBinding.imageSearch.setOnClickListener(view -> {
                    if(URLUtil.isValidUrl(text_title)){
                        qrCodeLayoutBinding.imageFrame.setVisibility(View.VISIBLE);
                        Glide.with(this)
                                .load(Uri.parse(text_title))
                                .apply(RequestOptions.placeholderOf(R.drawable.no_image_avaliable_icon))
                                .into(qrCodeLayoutBinding.cameraQrCodeImage);
                    }else{
                        Toast.makeText(this, "URL is Not Valid", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void openCustomTab(Activity activity, CustomTabsIntent customTabsIntent, Uri uri){
        String packageName ="com.android.chrome";
        if(packageName!=null){
            customTabsIntent.intent.setPackage(packageName);
            customTabsIntent.launchUrl(activity,uri);
        }else{
            //if Custom tabs fails to load then we are simply
            // redirecting our user to userd device default browser
            activity.startActivity(new Intent(Intent.ACTION_VIEW,uri));
        }
    }


    private void shareQrCodeData(String data){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/*");
        i.putExtra(Intent.EXTRA_TEXT,"QR Code Info"+"\n\n" +data+" \n ");
        startActivity(Intent.createChooser(i,"QR Code Info :- \n\n"));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
package com.dixitkumar.galleryxapp.PhotosFragment;

import static android.os.Environment.getStorageDirectory;
import static androidx.camera.core.MirrorMode.MIRROR_MODE_ON;
import static java.lang.System.currentTimeMillis;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.DisplayOrientedMeteringPointFactory;
import androidx.camera.core.DynamicRange;
import androidx.camera.core.ExperimentalZeroShutterLag;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.FocusMeteringResult;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.MirrorMode;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.core.ZoomState;
import androidx.camera.extensions.ExtensionsManager;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Instrumentation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.camera2.CameraCaptureSession;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.Toast;

import com.dixitkumar.galleryxapp.MainActivity;
import com.dixitkumar.galleryxapp.R;
import com.dixitkumar.galleryxapp.databinding.ActivityCameraViewfinderBinding;
import com.dixitkumar.galleryxapp.databinding.TopSheetFragmentBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@ExperimentalZeroShutterLag public class Camera_ViewFinderActivity extends AppCompatActivity {
     int cameraFacing = CameraSelector.LENS_FACING_BACK;
  ActivityCameraViewfinderBinding viewFinderBinding;

    private   ImageCapture imageCapture;
    private CameraControl cameraControl;
    private ScaleGestureDetector scaleGestureDetector;
    private CameraInfo cameraInfo;
    private CameraSelector cameraSelector;
    private ExecutorService cameraExecutor;
    private Handler handler = new Handler();
    private MediaPlayer cameraTime,captureSound;
    private static boolean IS_ENABLED = false;

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
//    Creating Bottom Sheet Fragment Object
    private BottomSheetDialog dialog;
//    Creating Dialog  Object

    //Camera Pro Mode DialogBox
    Dialog dialogBox;
    private TopSheetFragmentBinding topSheetFragmentBinding;
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewFinderBinding = ActivityCameraViewfinderBinding.inflate(getLayoutInflater());
        setContentView(viewFinderBinding.getRoot());

        //Check The Necessary Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.CAMERA);
        } else {
            startCamera(cameraFacing);
        }


        //Initializing Bottom Sheet Dialog
        dialog = new BottomSheetDialog(Camera_ViewFinderActivity.this);
        showDialog();
        //Bottom Sheet Fragment
        viewFinderBinding.opnCameraMenu.setOnClickListener(v -> {

            dialog.setContentView(topSheetFragmentBinding.getRoot());
            dialog.show();

        });


        //Camera Pro Mode
        viewFinderBinding.cameraProMode.setOnClickListener(view -> {
           makeDialogVisible();
        });

        //Handling Click On Video Activity Button
        viewFinderBinding.videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Camera_ViewFinderActivity.this, VideoActivity.class);
                startActivity(i);
                finish();
            }
        });

        //Capture Image after Specific Time delay
         viewFinderBinding.cameraTimer.setOnClickListener(view ->{
             if(!IS_ENABLED){
                 viewFinderBinding.cameraTimer.setImageResource(R.drawable.time_off_icon);
                 IS_ENABLED = true;
             }else{
                 viewFinderBinding.cameraTimer.setImageResource(R.drawable.time_burst_icon);
                 IS_ENABLED = false;
             }
         });

    }

    private void makeDialogVisible(){
        dialogBox = new Dialog(this);
        dialogBox.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogBox.setContentView(R.layout.pro_camera_menu);
        dialogBox.show();
    }
    protected void showDialog() {
        topSheetFragmentBinding = TopSheetFragmentBinding.inflate(getLayoutInflater());
        topSheetFragmentBinding.getRoot().setBackgroundColor(ContextCompat.getColor(this,R.color.white));
    }


    @SuppressLint("RestrictedApi")
    protected void startCamera(int cameraFacing) {
        int aspectRatio = aspectRatio(viewFinderBinding.viewFinder.getWidth(), viewFinderBinding.viewFinder.getHeight());
        ListenableFuture<ProcessCameraProvider> listenableFuture = ProcessCameraProvider.getInstance(this);

        listenableFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = (ProcessCameraProvider)listenableFuture.get();
                Preview preview = new Preview.Builder().setTargetAspectRatio(aspectRatio).build();

                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(cameraFacing).build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing).build();

                cameraProvider.unbindAll();

                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

                //For Performing operations that affects all outputs
                 cameraControl = camera.getCameraControl();



                //For Querying information and states
                 cameraInfo = camera.getCameraInfo();


                //AutoFocus Every X Seconds
                MeteringPointFactory AFfactory = new SurfaceOrientedMeteringPointFactory((float)viewFinderBinding.viewFinder.getWidth(),(float)viewFinderBinding.viewFinder.getHeight());
                float centerWidth = (float)viewFinderBinding.viewFinder.getWidth()/2;
                float centerHeight = (float)viewFinderBinding.viewFinder.getHeight()/2;
                MeteringPoint AFautoFocusPoint = AFfactory.createPoint(centerWidth, centerHeight);
                try {
                    FocusMeteringAction action = new FocusMeteringAction.Builder(AFautoFocusPoint,FocusMeteringAction.FLAG_AF).setAutoCancelDuration(1, TimeUnit.SECONDS).build();
                    cameraControl.startFocusAndMetering(action);
                }catch (Exception e){

                }

                //Camera Timer Option Added
                viewFinderBinding.captureImage.setOnClickListener(view -> {

                    if(IS_ENABLED){
                        cameraTime = MediaPlayer.create(Camera_ViewFinderActivity.this,R.raw.camera_time_sound);
                        cameraTime.start();
                        cameraTime.setLooping(true);
                        handler.postDelayed((Runnable) () -> {
                            if (ContextCompat.checkSelfPermission(Camera_ViewFinderActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            }
                            captureImage(imageCapture);
                            cameraTime.stop();
                        },5000);
                    }else{
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


                preview.setSurfaceProvider(viewFinderBinding.viewFinder.getSurfaceProvider());

                pinchToZoom();
                setUpZoomSlider();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
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
        if(camera.getCameraInfo().hasFlashUnit()){
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

    private void captureImage(ImageCapture imageCapture){
        if(imageCapture == null)return;

        String name = System.currentTimeMillis()+"";

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg");

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.P){
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/GalleryXApp");
        }

        ImageCapture.Metadata metadata = new ImageCapture.Metadata();
        if(cameraFacing != CameraSelector.LENS_FACING_FRONT){
            metadata.setReversedHorizontal(true);
        }


        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(
                getContentResolver(),MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues
        ).setMetadata(metadata).build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(Camera_ViewFinderActivity.this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                captureSound = MediaPlayer.create(Camera_ViewFinderActivity.this,R.raw.capture_image_sound);
                captureSound.start();
                Toast.makeText(Camera_ViewFinderActivity.this,"Image Saved"+outputFileResults.getSavedUri(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(Camera_ViewFinderActivity.this,"Filled ",Toast.LENGTH_SHORT).show();
            }
        });
    }

}
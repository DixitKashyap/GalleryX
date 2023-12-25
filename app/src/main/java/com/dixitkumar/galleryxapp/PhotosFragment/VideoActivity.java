package com.dixitkumar.galleryxapp.PhotosFragment;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.dixitkumar.galleryxapp.MainActivity;
import com.dixitkumar.galleryxapp.R;
import com.dixitkumar.galleryxapp.databinding.ActivityVideoBinding;
import com.dixitkumar.galleryxapp.databinding.BottomSheetNavigationVideoViewBinding;
import com.dixitkumar.galleryxapp.databinding.TopSheetFragmentBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoActivity extends AppCompatActivity {

    ExecutorService service;
    Recording recording = null;
    VideoCapture<Recorder> videoCapture = null;

    int cameraFacing = CameraSelector.LENS_FACING_BACK;

    private final ActivityResultLauncher<String> activityResultLauncher =  registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            if(ActivityCompat.checkSelfPermission(VideoActivity.this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
//                startCamera(cameraFacing);
            }
        }
    });

    //Private Bottom Sheet Dialog
    private BottomSheetDialog dialog;
    private BottomSheetNavigationVideoViewBinding videoViewBinding;

private ActivityVideoBinding activityVideoBinding ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityVideoBinding = ActivityVideoBinding.inflate(getLayoutInflater());
        setContentView(activityVideoBinding.getRoot());

        //Capturing The Video on Click of Record Button
        activityVideoBinding.recordButton.setOnClickListener(view -> {
            if (ActivityCompat.checkSelfPermission(VideoActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                activityResultLauncher.launch(Manifest.permission.CAMERA);
            } else if (ActivityCompat.checkSelfPermission(VideoActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                activityResultLauncher.launch(Manifest.permission.RECORD_AUDIO);
            } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && ActivityCompat.checkSelfPermission(VideoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                captureVideo();
            }
        });

        //Change Camera on Button Click
        activityVideoBinding.FlipButton.setOnClickListener(view -> {

            if(cameraFacing == CameraSelector.LENS_FACING_BACK){
                cameraFacing = CameraSelector.LENS_FACING_FRONT;
            }else{
                cameraFacing = CameraSelector.LENS_FACING_BACK;
            }
            startCamera(cameraFacing);
        });


        if(ActivityCompat.checkSelfPermission(VideoActivity.this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
            activityResultLauncher.launch(Manifest.permission.CAMERA);
        }else{
            startCamera(cameraFacing);
        }

        service = Executors.newSingleThreadExecutor();


        activityVideoBinding.flashButton.setOnClickListener(view -> {

        });

        //Initializing Bottom Sheet Dialog
        dialog = new BottomSheetDialog(VideoActivity.this);
        showDialog();
        //Making Menu Visible on Click on of a button
        activityVideoBinding.videoMenuButton.setOnClickListener(view -> {
            dialog.setContentView(videoViewBinding.getRoot());
            dialog.show();
        });
    }

    protected void showDialog() {
        videoViewBinding = BottomSheetNavigationVideoViewBinding.inflate(getLayoutInflater());
        videoViewBinding.getRoot().setBackgroundColor(ContextCompat.getColor(this,R.color.white));
    }

    private void captureVideo(){
      activityVideoBinding.recordButton.setImageResource(R.drawable.stop_recording_icon);
      Recording recording1 = recording;
      if(recording1!=null){
          recording1.stop();
          recording = null;
          return;
      }

      String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"video/mp4");
        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH,"Movies/CameraX-Video");


        MediaStoreOutputOptions outputOptions = new MediaStoreOutputOptions.Builder(getContentResolver(),MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues).build();

        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
            return;
        }

        recording = videoCapture.getOutput().prepareRecording(VideoActivity.this,outputOptions).withAudioEnabled().start(ContextCompat.getMainExecutor(VideoActivity.this),videoRecordEvent -> {

            if(videoRecordEvent instanceof VideoRecordEvent.Start){
                activityVideoBinding.recordButton.setEnabled(true);
            }else if(videoRecordEvent instanceof VideoRecordEvent.Finalize){
                if(!((VideoRecordEvent.Finalize)videoRecordEvent).hasError()){
                    String msg = "Video Capture Succeeded : "+((VideoRecordEvent.Finalize)videoRecordEvent).getOutputResults().getOutputUri();
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }else{
                    recording.close();
                    recording = null;
                    String msg = "Error: " + ((VideoRecordEvent.Finalize)videoRecordEvent).getError();
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
                activityVideoBinding.recordButton.setImageResource(R.drawable.record_icon);
            }
        });
    }

    public void startCamera(int cameraFacing){
        ListenableFuture<ProcessCameraProvider> processCameraProvider = ProcessCameraProvider.getInstance(VideoActivity.this);

        processCameraProvider.addListener(()->{
            try{
                ProcessCameraProvider cameraProvider = processCameraProvider.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(activityVideoBinding.videoPreview.getSurfaceProvider());

                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                        .build();
                videoCapture = VideoCapture.withOutput(recorder);

                cameraProvider.unbindAll();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing).build();

                Camera camera = cameraProvider.bindToLifecycle(this,cameraSelector,preview,videoCapture);

                activityVideoBinding.flashButton.setOnClickListener(view -> toggleFlash(camera));
            }catch (ExecutionException | InterruptedException e){
                e.printStackTrace();
            }
        },ContextCompat.getMainExecutor(VideoActivity.this));
    }

    private void toggleFlash(Camera camera){
        if(camera.getCameraInfo().hasFlashUnit()){
            if(camera.getCameraInfo().getTorchState().getValue() == 0){
                camera.getCameraControl().enableTorch(true);
                activityVideoBinding.flashButton.setImageResource(R.drawable.no_flash_icon);
            }else{
                camera.getCameraControl().enableTorch(false);
                activityVideoBinding.flashButton.setImageResource(R.drawable.flash_on_icon);
            }
        }else{
            runOnUiThread(() ->  Toast.makeText(this,"Flash Not Available !",Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        service.shutdown();
    }
}
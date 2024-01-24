package com.dixitkumar.galleryxapp.PhotosFragment;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.ExperimentalPersistentRecording;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dixitkumar.galleryxapp.AlbumFragment.Album_Fragment;
import com.dixitkumar.galleryxapp.AlbumFragment.AllVideoListActivity;
import com.dixitkumar.galleryxapp.AlbumFragment.Video;
import com.dixitkumar.galleryxapp.AlbumFragment.VideoPlayerActivity;
import com.dixitkumar.galleryxapp.AlbumFragment.VideoRecyclerviewAdapter;
import com.dixitkumar.galleryxapp.MainActivity;
import com.dixitkumar.galleryxapp.R;
import com.dixitkumar.galleryxapp.databinding.ActivityVideoViewBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class VideoViewActivity extends AppCompatActivity {

    private Uri artUri;
    ExecutorService service;
    Recording recording = null;
    VideoCapture<Recorder> videoCapture = null;
    final Handler handler
            = new Handler();

    private boolean running = false;
    private long seconds = 0;
    int cameraFacing = CameraSelector.LENS_FACING_BACK;
    private ArrayList<Video> videos ;

    private final ActivityResultLauncher<String> activityResultLauncher =  registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            if(ActivityCompat.checkSelfPermission(VideoViewActivity.this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                startCamera(cameraFacing);
            }
        }
    });
    private ActivityVideoViewBinding videoViewBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //For Viewing Video In Phone Notch Area
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        videoViewBinding = ActivityVideoViewBinding.inflate(getLayoutInflater());
        setTheme(R.style.CameraTheme);
        setContentView(videoViewBinding.getRoot());

        //For Immersive Mode
        WindowCompat.setDecorFitsSystemWindows(getWindow(),false);
        new WindowInsetsControllerCompat(getWindow(),videoViewBinding.getRoot()).setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        new WindowInsetsControllerCompat(getWindow(),videoViewBinding.getRoot()).hide(WindowInsetsCompat.Type.systemBars());


        //Capturing The Video on Click of Record Button
        videoViewBinding.recordButton.setOnClickListener(view -> {
            if (ActivityCompat.checkSelfPermission(VideoViewActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                activityResultLauncher.launch(Manifest.permission.CAMERA);
            } else if (ActivityCompat.checkSelfPermission(VideoViewActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                activityResultLauncher.launch(Manifest.permission.RECORD_AUDIO);
            } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && ActivityCompat.checkSelfPermission(VideoViewActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                captureVideo();
                videoViewBinding.videoDuration.setVisibility(View.VISIBLE);
                running = true;
                runTimer();
            }
        });
        //Change Camera on Button Click
        videoViewBinding.FlipButton.setOnClickListener(view -> {

            if (cameraFacing == CameraSelector.LENS_FACING_BACK) {
                cameraFacing = CameraSelector.LENS_FACING_FRONT;
            } else {
                cameraFacing = CameraSelector.LENS_FACING_BACK;
            }
            startCamera(cameraFacing);
        });
        if (ActivityCompat.checkSelfPermission(VideoViewActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.CAMERA);
        } else {
            startCamera(cameraFacing);
        }
        //Setting Up Video Preview
        setVideoPreview();
        service = Executors.newSingleThreadExecutor();

        //Setting Up Video Preview Listener
        videoViewBinding.videoViewPreview.setOnClickListener(view -> {
            if(videos.size()!=0) {
                Intent i = new Intent(VideoViewActivity.this, VideoPlayerActivity.class);
                VideoPlayerActivity.pipStatus = 1;
                int pos = Photos_Fragment.AllVideoList.size()-1;
                VideoPlayerActivity.pos = pos;
                VideoPlayerActivity.fromVideoView = true;
                i.putExtra("POS", pos);
                startActivity(i);
            }else{
                Toast.makeText(this, "No Videos To Play!!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void captureVideo(){
        videoViewBinding.recordButton.setImageResource(R.drawable.stop_recording_icon);
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

        recording = videoCapture.getOutput().prepareRecording(VideoViewActivity.this,outputOptions).withAudioEnabled().start(ContextCompat.getMainExecutor(VideoViewActivity.this), videoRecordEvent -> {

            if(videoRecordEvent instanceof VideoRecordEvent.Start){
                videoViewBinding.recordButton.setEnabled(true);
            }else if(videoRecordEvent instanceof VideoRecordEvent.Finalize){
                if(!((VideoRecordEvent.Finalize)videoRecordEvent).hasError()){

                   //Updating Preview After Adding A New Video
                    getAllVideo();
                    setVideoPreview();
                    String msg = "Video Capture Succeeded : "+((VideoRecordEvent.Finalize)videoRecordEvent).getOutputResults().getOutputUri();
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }else{
                    recording.close();
                    recording = null;
                    String msg = "Error: " + ((VideoRecordEvent.Finalize)videoRecordEvent).getError();
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
                resetTimer();
                videoViewBinding.videoDuration.setVisibility(View.GONE);
                videoViewBinding.recordButton.setImageResource(R.drawable.record_icon);
            }
        });
    }



    private void setVideoPreview(){
        videos = new ArrayList<>();
        videos = getAllVideo();

        Uri art = null;
        if(videos.size()!=0) {
            art = videos.get(videos.size() - 1).getArtUri();
        }
        if(art!=null) {
            videoViewBinding.videoViewPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(VideoViewActivity.this)
                    .load(art)
                    .apply(RequestOptions.placeholderOf(R.color.white))
                    .into(videoViewBinding.videoViewPreview);
        }
    }

    @SuppressLint("Range")
    private ArrayList<Video> getAllVideo() {
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
            cursor =getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,projection,null,null );
        }

        if (cursor!=null){
            if(cursor.moveToNext()){
                do{


                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                    String size = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                    String id = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                    String data = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                    Long duration = Long.parseLong(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)));


                    try{
                        File file = new File(data);

                        artUri = Uri.fromFile(file);

                        Video video = new Video(id,title,duration,file.getParentFile().getName(),size,data,artUri);

                        if(file.exists()){
                            if(file.getParentFile().getName().contentEquals("CameraX-Video")){
                                tempList.add(video);
                            }
                        }
                    }catch (Exception e){

                    }

                }while (cursor.moveToNext());
                cursor.close();
            }
        }

        return tempList;
    }

    public void startCamera(int cameraFacing){
        ListenableFuture<ProcessCameraProvider> processCameraProvider = ProcessCameraProvider.getInstance(VideoViewActivity.this);

        processCameraProvider.addListener(()->{
            try{
                ProcessCameraProvider cameraProvider = processCameraProvider.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(videoViewBinding.videoPreview.getSurfaceProvider());

                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                        .build();
                videoCapture = VideoCapture.withOutput(recorder);

                cameraProvider.unbindAll();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing).build();

                Camera camera = cameraProvider.bindToLifecycle(this,cameraSelector,preview,videoCapture);

                videoViewBinding.flashButton.setOnClickListener(view -> toggleFlash(camera));
            }catch (ExecutionException | InterruptedException e){
                e.printStackTrace();
            }
        },ContextCompat.getMainExecutor(VideoViewActivity.this));
    }

    private void toggleFlash(Camera camera){
        if(camera.getCameraInfo().hasFlashUnit()){
            if(camera.getCameraInfo().getTorchState().getValue() == 0){
                camera.getCameraControl().enableTorch(true);
                videoViewBinding.flashButton.setImageResource(R.drawable.no_flash_icon);
            }else{
                camera.getCameraControl().enableTorch(false);
                videoViewBinding.flashButton.setImageResource(R.drawable.flash_on_icon);
            }
        }else{
            runOnUiThread(() ->  Toast.makeText(this,"Flash Not Available !",Toast.LENGTH_SHORT).show());
        }
    }

    private void runTimer()
    {
        handler.post(new Runnable() {
            @Override

            public void run()
            {
                long hours = seconds / 3600;
                long minutes = (seconds % 3600) / 60;
                long secs = seconds % 60;

                // Format the seconds into hours, minutes,
                // and seconds.
                String time
                        = String
                        .format(Locale.getDefault(),
                                "%d:%02d:%02d", hours,
                                minutes, secs);

                // Set the text view text.
                videoViewBinding.videoDuration.setText(time);

                // If running is true, increment the
                // seconds variable.
                if (running) {
                    seconds++;
                }

                // Post the code again
                // with a delay of 1 second.
                handler.postDelayed(this, 1000);
            }
        });
    }
    public void resetTimer()
    {
        running = false;
        seconds = 0;
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        service.shutdown();
    }
}
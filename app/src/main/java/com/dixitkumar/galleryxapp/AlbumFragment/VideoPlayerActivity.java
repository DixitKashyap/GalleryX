package com.dixitkumar.galleryxapp.AlbumFragment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.media.audiofx.LoudnessEnhancer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.dixitkumar.galleryxapp.PhotosFragment.Photos_Fragment;
import com.dixitkumar.galleryxapp.R;
import com.dixitkumar.galleryxapp.databinding.ActivityVideoPlayerBinding;
import com.dixitkumar.galleryxapp.databinding.AudioBoosterLayoutBinding;
import com.dixitkumar.galleryxapp.databinding.MoreVideoFeaturesLayoutBinding;
import com.dixitkumar.galleryxapp.databinding.SpeedDialogBinding;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import kotlin.Unit;

public class VideoPlayerActivity extends AppCompatActivity {

    private static int videoPosition = -1;
    private Runnable runnable;
    protected static SimpleExoPlayer player;
    private ActivityVideoPlayerBinding playerBinding;
    private boolean isRepeat = false;
    private static boolean isLocked = false;
    private boolean isFullScreen = false;
    private boolean isSubtitle = true;
    private LoudnessEnhancer enhancer ;
    private static Float video_speed= 1.0f;
    private Timer timer ;
    public static int pipStatus = 1;
    public static int pos = 0;

    public static boolean fromVideoView = false;
    Intent i ;
    private static DefaultTrackSelector defaultTrackSelector ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //For Viewing Video In Phone Notch Area
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        playerBinding = ActivityVideoPlayerBinding.inflate(getLayoutInflater());

        //Setting Up The Theme
        setTheme(R.style.playerActivityTheme);
        setContentView(playerBinding.getRoot());

        //For Immersive Mode
        WindowCompat.setDecorFitsSystemWindows(getWindow(),false);
        new WindowInsetsControllerCompat(getWindow(),playerBinding.getRoot()).setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        new WindowInsetsControllerCompat(getWindow(),playerBinding.getRoot()).hide(WindowInsetsCompat.Type.systemBars());

         i  = getIntent();
        videoPosition =i.getIntExtra("POS",0);

        //Getting Up The Tracks From Video Files
        defaultTrackSelector = new DefaultTrackSelector(this);
        player =  new SimpleExoPlayer.Builder(this).setTrackSelector(defaultTrackSelector).build();
        playerBinding.playerView.setPlayer(player);
       //Starting Video On Click of Button
        createPlayerView();
        //Setting Up The Back Button
        playerBinding.backBtn.setOnClickListener(view -> finish());

        //Setting Up the Play and pause button
        playerBinding.playPauseBtn.setOnClickListener(view -> {
            if(player.isPlaying()){
                pauseVideo();
            }else{
                playVideo();
            }
        });

        //Setting Up The Next Button
        playerBinding.nextBtn.setOnClickListener(view -> {
            nextPrevVideo(true);
        });

        //Setting Up The Previous Button
        playerBinding.prevBtn.setOnClickListener(view -> {
            nextPrevVideo(false);
        });

        //Setting Up Listener on Repeat Button
        playerBinding.repeatBtn.setOnClickListener(view -> {
            if(isRepeat){
                isRepeat = false;
                player.setRepeatMode(Player.REPEAT_MODE_OFF);
                playerBinding.repeatBtn.setImageResource(R.drawable.repeat_off_icon);
            }else{

                isRepeat = true;
                player.setRepeatMode(Player.REPEAT_MODE_ONE);
                playerBinding.repeatBtn.setImageResource(R.drawable.repeat_one_icon);
            }
        });

        //Setting Up Full Screen Button
        playerBinding.fullScreenBtn.setOnClickListener(view -> {
            if(isFullScreen){
                isFullScreen = false;
                playInFullScreen(false);
            }else{
                isFullScreen = true;
                playInFullScreen(true);
            }
        });

        //Setting Up The Lock Button
        playerBinding.screenLockButton.setOnClickListener(view -> {
            if(!isLocked){
                isLocked = true;
                playerBinding.playerView.hideController();
                playerBinding.playerView.setUseController(false);
                playerBinding.screenLockButton.setImageResource(R.drawable.close_lock_icon);
            }else{
                isLocked = false;
                playerBinding.playerView.setUseController(true);
                playerBinding.playerView.showController();
                playerBinding.screenLockButton.setImageResource(R.drawable.lock_open_icon);
            }
        });

        //Setting Up More Feature Button
        playerBinding.moreFeaturesBtn.setOnClickListener(view -> {
            pauseVideo();
            View dialog = LayoutInflater.from(this).inflate(R.layout.more_video_features_layout,playerBinding.getRoot(),false);
            MoreVideoFeaturesLayoutBinding moreFeaturesBinding = MoreVideoFeaturesLayoutBinding.bind(dialog);
            AlertDialog dialogBuilder = new MaterialAlertDialogBuilder(this).setView(dialog)
                    .setOnCancelListener(dialogInterface -> playVideo())
                    .setBackground( new ColorDrawable(Integer.parseInt(String.valueOf(0x803700B3))))
                    .create();
            dialogBuilder.show();

            //Changing Audio On Click of A Button
            moreFeaturesBinding.audioTrackButton.setOnClickListener(view1 -> {
                dialogBuilder.dismiss();

                ArrayList<String> audioTracks = new ArrayList<>();

                int track = player.getCurrentTrackGroups().length;
                for(int a =0;a<track;a++){
                    if(player.getCurrentTrackGroups().get(a).getFormat(0).selectionFlags == C.SELECTION_FLAG_DEFAULT){
                        audioTracks.add(new Locale(player.getCurrentTrackGroups().get(a).getFormat(0).language.toString()).getDisplayLanguage());
                    }
                }
                ArrayList<CharSequence> tempTracks = new ArrayList<>(audioTracks.size());
                for (CharSequence tr : audioTracks) {
                    tempTracks.add(tr);
                }
                CharSequence[] tempArray = tempTracks.toArray(new CharSequence[0]);

                new MaterialAlertDialogBuilder(this,R.style.alterDialog)
                        .setTitle("Select Language")
                        .setOnCancelListener(dialogInterface -> playVideo())
                        .setBackground( new ColorDrawable(Integer.parseInt(String.valueOf(0x803700B3))))
                        .setItems(tempArray, (dialogInterface, position) -> {
                            Toast.makeText(this, audioTracks.get(position)+" Selected", Toast.LENGTH_SHORT).show();
                            defaultTrackSelector.
                                    setParameters(defaultTrackSelector
                                            .buildUponParameters()
                                            .setPreferredAudioLanguage(audioTracks.get(position)));
                        })
                        .create().show();
            });


            //Setting Up Subtitle On And off Button
            moreFeaturesBinding.subtitle.setOnClickListener(view12 -> {
                if(isSubtitle) {
                    defaultTrackSelector.setParameters
                            (new DefaultTrackSelector.ParametersBuilder(this).
                                    setRendererDisabled(C.TRACK_TYPE_VIDEO,true).
                                    build()
                            );
                    Toast.makeText(this, "Subtitles Off", Toast.LENGTH_SHORT).show();
                    isSubtitle = false;
                }
                else{
                    defaultTrackSelector.setParameters
                            (new DefaultTrackSelector.ParametersBuilder(this).
                                    setRendererDisabled(C.TRACK_TYPE_VIDEO,false).
                                    build()
                            );
                    Toast.makeText(this, "Subtitles On", Toast.LENGTH_SHORT).show();
                    isSubtitle = true;
                }
                dialogBuilder.dismiss();
                playVideo();
            });
            moreFeaturesBinding.audioBoosterButton.setOnClickListener(view13 -> {
                dialogBuilder.dismiss();

                View dialogB = LayoutInflater.from(this).inflate(R.layout.audio_booster_layout,playerBinding.getRoot(),false);
                AudioBoosterLayoutBinding boosterBinding = AudioBoosterLayoutBinding.bind(dialogB);
                AlertDialog alertDialog = new  MaterialAlertDialogBuilder(this)
                        .setView(dialogB)
                        .setPositiveButton("Ok", (dialogInterface, i1) -> {
                            enhancer.setTargetGain(boosterBinding.volumeBoosterBar.getProgress()*100);
                            playVideo();
                            dialogInterface.dismiss();
                        })
                        .setOnCancelListener(dialogInterface -> {playVideo();})
                        .setBackground( new ColorDrawable(Integer.parseInt(String.valueOf(0x803700B3))))
                        .create();
                alertDialog.show();
               boosterBinding.volumeBoosterBar.setProgress((int) (((enhancer.getTargetGain()))/100));
               boosterBinding.progressBarInfo.setText("Audio Boost\n\n"+(int)(enhancer.getTargetGain()/10)+" %");
              boosterBinding.volumeBoosterBar.setOnProgressChangeListener(integer -> {
                  boosterBinding.progressBarInfo.setText("Audio Boost \n\n"+integer*10+"%");
                     return Unit.INSTANCE;
              });
                playVideo();
            });



            //Setting Up The Video Speed Button
            moreFeaturesBinding.videoSpeedButton.setOnClickListener(view14 -> {
                dialogBuilder.dismiss();
                playVideo();
                View dialogS = LayoutInflater.from(this).inflate(R.layout.speed_dialog,playerBinding.getRoot(),false);
                SpeedDialogBinding speedDialogBinding = SpeedDialogBinding.bind(dialogS);
                AlertDialog alertDialog = new  MaterialAlertDialogBuilder(this)
                        .setView(dialogS)
                        .setCancelable(false)
                        .setPositiveButton("Ok", (dialogInterface, i1) -> {
                            dialogInterface.dismiss();
                        })
                        .setBackground( new ColorDrawable(Integer.parseInt(String.valueOf(0x803700B3))))
                        .create();
                alertDialog.show();

                speedDialogBinding.speedText.setText(video_speed+" X");
                //Decreasing Speed on Click of Button
                speedDialogBinding.decreaseSpeed.setOnClickListener(view15 -> {
                    changeSpeed(false);
                    speedDialogBinding.speedText.setText(new DecimalFormat("#.##").format(video_speed)+" X");
                });

                //Increase Speed On Click  of Button
                speedDialogBinding.increaseSpeed.setOnClickListener(view16 -> {
                    changeSpeed(true);
                    speedDialogBinding.speedText.setText(new DecimalFormat("#.##").format(video_speed)+" X");
                });

            });


            //Setting Up The Sleep Timer
            moreFeaturesBinding.sleepTimer.setOnClickListener(view17 -> {
                dialogBuilder.dismiss();
                if(timer!=null){
                    Toast.makeText(this, "Timer Already Running:\nClose App To Reset Timer", Toast.LENGTH_SHORT).show();
                }else{
                    playVideo();
                    final Integer[] sleepTime = {new Integer(15)};
                    View dialogS = LayoutInflater.from(this).inflate(R.layout.speed_dialog,playerBinding.getRoot(),false);
                    SpeedDialogBinding speedDialogBinding = SpeedDialogBinding.bind(dialogS);
                    MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this);
                    materialAlertDialogBuilder.setView(dialogS);
                    materialAlertDialogBuilder.setCancelable(false);
                    materialAlertDialogBuilder.setPositiveButton("Ok", (dialogInterface, i1) -> {
                        timer = new Timer();

                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                moveTaskToBack(true);
                                System.exit(1);
                            }
                        };

                        if (timer != null) {
                            timer.schedule(task, (long) (sleepTime[0] * 60 * 1000));
                        }
                        dialogInterface.dismiss();
                    });
                    materialAlertDialogBuilder.setBackground(new ColorDrawable(Integer.parseInt(String.valueOf(0x803700B3))));
                    AlertDialog alertDialog = materialAlertDialogBuilder
                            .create();
                    alertDialog.show();

                    speedDialogBinding.speedText.setText(sleepTime[0] +"Min");
                    //Decreasing Speed on Click of Button
                    speedDialogBinding.decreaseSpeed.setOnClickListener(view15 -> {
                        if(sleepTime[0] >15){
                            sleepTime[0] -=15;
                        }
                        speedDialogBinding.speedText.setText(sleepTime[0] +"Min");
                    });

                    //Increase Speed On Click  of Button
                    speedDialogBinding.increaseSpeed.setOnClickListener(view16 -> {
                        if(sleepTime[0] <120){
                            sleepTime[0]+=15;
                        }
                        speedDialogBinding.speedText.setText(sleepTime[0] +"Min");
                    });
                }
            });


            //Handling Picture in Picture Mode
            moreFeaturesBinding.pictureInPicture.setOnClickListener(view18 -> {
                AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
                boolean status =  appOpsManager.
                        checkOpNoThrow(AppOpsManager.OPSTR_PICTURE_IN_PICTURE,android.os.Process.myUid(),getPackageName())
                        == AppOpsManager.MODE_ALLOWED?true:false;

                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
                    if (status) {
                        this.enterPictureInPictureMode();
                        dialogBuilder.dismiss();
                        playerBinding.playerView.hideController();
                        playVideo();
                        pipStatus = 0;
                    } else {
                        Intent intent = new Intent("android.settings.PICTURE_IN_PICTURE_SETTINGS",
                                Uri.parse("package:" + getPackageName()));

                        startActivity(intent);
                    }
                }else{
                    Toast.makeText(this, "Feature Not Supported", Toast.LENGTH_SHORT).show();
                    dialogBuilder.dismiss();
                    playVideo();
                }
            });


        });

        //Setting Up The Video Player Screen Orientation Button
        playerBinding.orientationBtn.setOnClickListener(view -> {

            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }else{
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            }
        });
    }
    private void nextPrevVideo(boolean isNext){
     if(isNext){
         setPosition(true);
         createPlayerView();
     }else{
         setPosition(false);
         createPlayerView();
     }
    }

    private void setPosition(boolean isIncrement){
       if(!isRepeat){
           if(isIncrement){
               if(VideoRecyclerviewAdapter.videoArrayList.size()-1 == videoPosition){
                   videoPosition = 0;
               }else{
                   ++videoPosition;
               }
           }else{
               if(videoPosition == 0){
                   videoPosition = VideoRecyclerviewAdapter.videoArrayList.size()-1;
               }else{
                   --videoPosition;
               }
           }
       }
    }
    private void playVideo(){
        playerBinding.playPauseBtn.setImageResource(R.drawable.pause_icon);
        player.play();
    }

    private void pauseVideo(){
        playerBinding.playPauseBtn.setImageResource(R.drawable.play_icon);
        player.pause();
    }
    private void createPlayerView(){

        if (isRepeat){playerBinding.repeatBtn.setImageResource(R.drawable.repeat_one_icon);}
        else{playerBinding.repeatBtn.setImageResource(R.drawable.repeat_off_icon);}
        video_speed = 1.0f;
        playerBinding.videoTitle.setSelected(true);

        playerBinding.videoTitle.setText(VideoRecyclerviewAdapter.videoArrayList.get(videoPosition).getTitle());
        MediaItem mediaItem = MediaItem.fromUri(VideoRecyclerviewAdapter.videoArrayList.get(videoPosition).getArtUri());
        player.setMediaItems(Collections.singletonList(mediaItem));


        playInFullScreen(isFullScreen);
        //Changing Video On Completion of the Current Video
        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Player.Listener.super.onPlayerStateChanged(playWhenReady, playbackState);
                if(playbackState == Player.STATE_ENDED && fromVideoView == false){
                    nextPrevVideo(true);
                }
            }
        });

        //Setting Up The Player Visibility
        setVisibility();
        enhancer = new LoudnessEnhancer(player.getAudioSessionId());
        enhancer.setEnabled(true);
        player.prepare();
        playVideo();


    }


    //For Handling Full Screen Mode
    private void playInFullScreen(boolean enable){
        if(enable){
            playerBinding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            playerBinding.fullScreenBtn.setImageResource(R.drawable.fullscreen_exit_icon);
        }else{
            playerBinding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            playerBinding.fullScreenBtn.setImageResource(R.drawable.fullscreen_icon);
        }
    }

    private void setVisibility(){
        runnable = () -> {
            if(playerBinding.playerView.isControllerVisible()){
                changeVisibility(View.VISIBLE);
            }else{
                changeVisibility(View.GONE);
            }
            new Handler(Looper.getMainLooper()).postDelayed(runnable,100);
        };
        new Handler(Looper.getMainLooper()).postDelayed(runnable,0);
    }

    private void changeSpeed(boolean isIncrement){
      if(isIncrement){
          if(video_speed <=3.0f){
              video_speed +=0.10f;
          }
      }else{
          if(video_speed>0.20f){
              video_speed -= 0.10f;
          }
      }
      player.setPlaybackSpeed(video_speed);
    }

    private void changeVisibility(int visibility){
        playerBinding.topController.setVisibility(visibility);
        playerBinding.bottomController.setVisibility(visibility);
        playerBinding.playPauseBtn.setVisibility(visibility);
        if(isLocked){
            playerBinding.screenLockButton.setVisibility(View.VISIBLE);
        }else{
            playerBinding.screenLockButton.setVisibility(visibility);
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        if (pipStatus != 0) {
            finish();
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("POS",pos);
            startActivity(intent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isInPictureInPictureMode()) {
            pauseVideo();
        }
        pauseVideo();
    }


    @Override
    protected void onResume() {
        super.onResume();
        playVideo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }
}
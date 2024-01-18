package com.dixitkumar.galleryxapp.AlbumFragment;

import android.net.Uri;

import java.util.concurrent.TimeUnit;

public class Video {
    private String id;
    private String title;
    private  Long duration = 0l;
    private String folderName ;
    private String size;
    private String path;
    private Uri artUri ;


    public Video(String id, String title, Long duration, String folderName, String size, String path, Uri artUri) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.folderName = folderName;
        this.size = size;
        this.path = path;
        this.artUri = artUri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Uri getArtUri() {
        return artUri;
    }

    public void setArtUri(Uri artUri) {
        this.artUri = artUri;
    }

    public static String formatTime(Long duration){
        Long minutes = TimeUnit.MINUTES.convert(duration,TimeUnit.MILLISECONDS);
        Long seconds = (TimeUnit.SECONDS.convert(duration,TimeUnit.MILLISECONDS)-minutes*TimeUnit.SECONDS.convert(1,TimeUnit.MINUTES));

        return String.format("%02d : %02d",minutes,seconds);
    }
}


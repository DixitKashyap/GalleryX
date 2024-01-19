package com.dixitkumar.galleryxapp.PhotosFragment;

public class Images {
    private String path;
    private String folderName;
    private String dateTaken;

    public Images(String path, String folderName, String dateTaken) {
        this.path = path;
        this.folderName = folderName;
        this.dateTaken = dateTaken;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(String dateTaken) {
        this.dateTaken = dateTaken;
    }
}

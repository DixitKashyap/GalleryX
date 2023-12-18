package com.dixitkumar.galleryxapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.dixitkumar.galleryxapp.AlbumFragment.Album_Fragment;
import com.dixitkumar.galleryxapp.PhotosFragment.Photos_Fragment;

public class Fragment_Adapter extends FragmentStateAdapter {

    public Fragment_Adapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle){
        super(fragmentManager,lifecycle);
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new Photos_Fragment();
            case 1:
                return new Album_Fragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}

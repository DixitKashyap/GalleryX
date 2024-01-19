package com.dixitkumar.galleryxapp.PhotosFragment;

import android.content.Context;
import android.content.Intent;
import android.gesture.GestureOverlayView;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dixitkumar.galleryxapp.ImageViewerActivity;
import com.dixitkumar.galleryxapp.R;
import com.dixitkumar.galleryxapp.databinding.ItemviewImageBinding;
import com.google.android.material.imageview.ShapeableImageView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Images> imagesArrayList ;

    PhotosAdapter(Context context,ArrayList<Images>imagesArrayList){
        this.context =context;
        this.imagesArrayList = imagesArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemviewImageBinding.inflate(LayoutInflater.from(context),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context)
                .load(imagesArrayList.get(position).getPath())
                .into(holder.imageView);

        holder.imageView.setOnClickListener(view -> {
            Intent i = new Intent(context, ImageViewerActivity.class);
            Log.d("TAG",imagesArrayList.get(position).getPath());
            i.putExtra("IMAGE_URI",imagesArrayList.get(position).getPath());
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return imagesArrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;

        public ViewHolder(@NonNull ItemviewImageBinding itemView) {
            super(itemView.getRoot());
            this.imageView = itemView.images;
        }
    }
}

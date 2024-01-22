package com.dixitkumar.galleryxapp.AlbumFragment;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dixitkumar.galleryxapp.ImageViewerActivity;
import com.dixitkumar.galleryxapp.PhotosFragment.Images;
import com.dixitkumar.galleryxapp.databinding.ItemviewImageBinding;

import java.util.ArrayList;

public class ImageSubViewAdapter extends RecyclerView.Adapter<ImageSubViewAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Images> imagesArrayList = new ArrayList<>();



    ImageSubViewAdapter(Context context,ArrayList<Images> imagesArrayList){
        this.context = context;
        this.imagesArrayList= imagesArrayList;
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
        private ImageView imageView;
        public ViewHolder(@NonNull ItemviewImageBinding itemView) {
            super(itemView.getRoot());
            this.imageView = itemView.images;
        }
    }
}

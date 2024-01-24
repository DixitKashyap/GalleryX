package com.dixitkumar.galleryxapp.AlbumFragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dixitkumar.galleryxapp.PhotosFragment.Photos_Fragment;
import com.dixitkumar.galleryxapp.R;
import com.dixitkumar.galleryxapp.databinding.RecyclerviewItemVideosBinding;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.common.primitives.Bytes;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class VideoRecyclerviewAdapter extends RecyclerView.Adapter<VideoRecyclerviewAdapter.ViewHolder> {

    private Context context;
    public static ArrayList<Video> videoArrayList= Photos_Fragment.AllVideoList;

    VideoRecyclerviewAdapter(Context context){
        this.context = context;
    }

    protected void setFilteredList(ArrayList<Video> filteredList){
        this.videoArrayList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RecyclerviewItemVideosBinding.inflate(LayoutInflater.from(context),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.video_title.setSelected(true);
      holder.video_title.setText(videoArrayList.get(position).getTitle());
      holder.videoThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(context)
                .load(videoArrayList.get(position).getArtUri())
                .apply(RequestOptions.placeholderOf(R.color.black))
                .into(holder.videoThumbnail);


        String videoDuration = Video.formatTime(videoArrayList.get(position).getDuration());
        holder.video_duration.setText(videoDuration);

        long videoSizeInBytes = Long.parseLong(videoArrayList.get(position).getSize());
        double videoInKiloBytes = (double)videoSizeInBytes/1024;
        double videoInMegaBytes = (double)videoInKiloBytes/1024;
        String imageSize = String.valueOf(videoInMegaBytes).substring(0,4)+"MB";
        holder.video_size.setText(imageSize+"");

        //Setting Up Click Listener
        holder.videoView.setOnClickListener(view -> {
            Intent i = new Intent(context, VideoPlayerActivity.class);
            VideoPlayerActivity.pipStatus = 1;
            VideoPlayerActivity.pos = position;
            i.putExtra("POS",position);
            context.startActivity(i);
        });

        //Sharing Video On Long Click
        holder.videoView.setOnLongClickListener(view -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("video/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(videoArrayList.get(position).getPath()));
            context.startActivity(Intent.createChooser(shareIntent,"Share Music File!!"));
            return true;
        });

      //Setting Up The Folder Name
        holder.folder_name.setText(videoArrayList.get(position).getFolderName());
    }

    @Override
    public int getItemCount() {
        return videoArrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        LinearLayout videoView;
        ShapeableImageView videoThumbnail;
        TextView video_title;
        TextView video_size;
        TextView video_duration;

        TextView folder_name;

        public ViewHolder(@NonNull RecyclerviewItemVideosBinding itemVideosBinding) {
            super(itemVideosBinding.getRoot());
            this.videoView = itemVideosBinding.getRoot();

            this.videoThumbnail = itemVideosBinding.videoThumbnail;
            this.video_title = itemVideosBinding.videTitle;
            this.video_size = itemVideosBinding.videoSize;
            this.video_duration = itemVideosBinding.videoDuration;
            this.folder_name = itemVideosBinding.folderName;
        }
    }
}

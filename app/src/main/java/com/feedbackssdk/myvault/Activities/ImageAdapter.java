package com.feedbackssdk.myvault.Activities;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.feedbackssdk.myvault.R;
import com.feedbackssdk.myvault.glideExtention.EncryptedFileModel;

import java.io.File;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private final Context context;
    private final List<File> imageFiles;

    public ImageAdapter(Context context, List<File> imageFiles) {
        this.context = context;
        this.imageFiles = imageFiles;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate your item layout. Ensure that the root is a MaskableFrameLayout.
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        File file = imageFiles.get(position);

        // Use Glide with your custom model to load the image.
        Glide.with(context)
                .asBitmap()
                .load(new EncryptedFileModel(file))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imageView);

        // Zoom functionality: Launch full-screen activity.
        holder.btnZoom.setOnClickListener(v -> {
            Intent fullScreenIntent = new Intent(context, FullScreenImageActivity.class);
            fullScreenIntent.putExtra(FullScreenImageActivity.EXTRA_FILE_PATH, file.getAbsolutePath());
            context.startActivity(fullScreenIntent);
        });

        // Delete functionality with confirmation dialog.
        holder.btnDelete.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this image? This action cannot be undone.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        if (file.delete()) {
                            imageFiles.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, imageFiles.size());
                            Toast.makeText(context, "Image deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Error deleting image", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
        });
        holder.btnShare.setOnClickListener(v -> {
            Intent shareImageIntent = new Intent(context, ShareImageActivity.class);
            shareImageIntent.putExtra(FullScreenImageActivity.EXTRA_FILE_PATH, file.getAbsolutePath());
            context.startActivity(shareImageIntent);
        });
    }

    @Override
    public int getItemCount() {
        return imageFiles.size();
    }

    // Make the ViewHolder public so that it is accessible outside the adapter's scope.
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton btnDelete, btnZoom, btnShare;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnZoom = itemView.findViewById(R.id.btnZoom);
            btnShare = itemView.findViewById(R.id.btnShare);
        }
    }
}

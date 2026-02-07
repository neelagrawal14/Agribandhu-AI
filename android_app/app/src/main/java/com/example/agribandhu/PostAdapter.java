package com.example.agribandhu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private Context context;
    private List<PostModel> postList;

    public PostAdapter(Context context, List<PostModel> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PostModel post = postList.get(position);
        holder.farmerName.setText(post.getFarmerName());
        holder.description.setText(post.getDescription());

        String imageData = post.getImageUri();

        if (imageData != null && !imageData.isEmpty()) {
            try {
                // ✅ Try to detect Base64 or URI
                if (imageData.startsWith("content://") || imageData.startsWith("file://") || imageData.startsWith("http")) {
                    holder.imageView.setImageURI(Uri.parse(imageData));
                } else {
                    // ✅ Decode Base64
                    byte[] decodedBytes = Base64.decode(imageData, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    holder.imageView.setImageBitmap(decodedBitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
                holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView farmerName, description;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            farmerName = itemView.findViewById(R.id.farmerName);
            description = itemView.findViewById(R.id.postDescription);
            imageView = itemView.findViewById(R.id.postImage);
        }
    }
}

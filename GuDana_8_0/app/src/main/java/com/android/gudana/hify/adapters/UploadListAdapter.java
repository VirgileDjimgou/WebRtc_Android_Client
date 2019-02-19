package com.android.gudana.hify.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.gudana.hify.ui.activities.notification.ImagePreview;
import com.android.gudana.hify.ui.activities.post.PostImage;
import com.android.gudana.R;

import java.util.ArrayList;
import java.util.List;

public class UploadListAdapter extends RecyclerView.Adapter<UploadListAdapter.ViewHolder>{

    private List<String> fileNameList;
    private List<String> fileUriList;
    private List<String> fileDoneList;
    public static List<String> uploadedImagesUrl;
    private Activity activity;
    private Context context;

    public UploadListAdapter(List<String> fileUriList, List<String> fileNameList, List<String> fileDoneList){

        this.fileDoneList = fileDoneList;
        this.fileNameList = fileNameList;
        this.fileUriList = fileUriList;
        uploadedImagesUrl=new ArrayList<>();

    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.hi_item_image, parent, false);
        context=parent.getContext();
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(context,ImagePreview.class)
                        .putExtra("uri",fileUriList.get(holder.getAdapterPosition()));
                context.startActivity(intent);
            }
        });

        String fileName = fileNameList.get(position);
        holder.fileNameView.setText(fileName);

        String fileDone = fileDoneList.get(position);

        if(fileDone.equals("uploading")){

            holder.progressBar.setVisibility(View.VISIBLE);
            holder.fileDoneView.setVisibility(View.GONE);

        } else {

            holder.fileDoneView.setImageResource(R.mipmap.checked);
            holder.fileDoneView.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);

            if(uploadedImagesUrl.size()==getItemCount()){
                PostImage.canUpload=true;
            }
        }



    }

    @Override
    public int getItemCount() {
        return fileNameList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public TextView fileNameView;
        public ImageView fileDoneView;
        public ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            fileNameView = (TextView) mView.findViewById(R.id.upload_filename);
            fileDoneView = (ImageView) mView.findViewById(R.id.upload_loading);
            progressBar = (ProgressBar) mView.findViewById(R.id.upload_progress);


        }

    }

}

package com.cw.picker.adapter;


import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cw.picker.PickerConfig;
import com.cw.picker.R;
import com.cw.picker.entity.Media;
import com.cw.picker.utils.FileUtils;
import com.cw.picker.utils.ScreenUtils;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


public class MediaGridAdapter extends RecyclerView.Adapter<MediaGridAdapter.MyViewHolder> {

    private ArrayList<Media> medias = new ArrayList<>();
    private ArrayList<Media> selectMedias = new ArrayList<>();
    private ArrayList<String> selectPaths = new ArrayList<>();
    private FileUtils fileUtils = new FileUtils();
    private long maxSelect, maxSize;
    private Context context;

    public MediaGridAdapter(Context context, ArrayList<String> select, int max, long maxSize) {
        if (select != null) {
            selectPaths = select;
        }
        this.maxSelect = max;
        this.maxSize = maxSize;
        this.context = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView media_image, check_image;
        public View mask_view;
        public TextView textView_size;
        public RelativeLayout gif_info;
        public RelativeLayout video_info;

        MyViewHolder(View view) {
            super(view);
            media_image = view.findViewById(R.id.media_image);
            check_image = view.findViewById(R.id.check_image);
            mask_view = view.findViewById(R.id.mask_view);
            video_info = view.findViewById(R.id.video_info);
            gif_info = view.findViewById(R.id.gif_info);
            textView_size = view.findViewById(R.id.textView_size);
            itemView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getItemWidth())); //让图片是个正方形
        }
    }

    private int getItemWidth() {
        return (ScreenUtils.getScreenWidth(context) / PickerConfig.GRID_SPAN_COUNT) - PickerConfig.GRID_SPAN_COUNT;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.media_view_item, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        final Media media = medias.get(position);
        Uri mediaUri = Uri.parse("file://" + media.path);

        Glide.with(context)
                .load(mediaUri)
                .into(holder.media_image);

        if (media.mediaType == 3) { //3 视频
            holder.gif_info.setVisibility(View.INVISIBLE);
            holder.video_info.setVisibility(View.VISIBLE);
            holder.textView_size.setText(fileUtils.getSizeByUnit(media.size));
        } else {
            holder.video_info.setVisibility(View.INVISIBLE);
            holder.gif_info.setVisibility(".gif".equalsIgnoreCase(media.extension) ? View.VISIBLE : View.INVISIBLE);
        }

        int isSelect = isSelect(media);
        holder.mask_view.setVisibility(isSelect >= 0 ? View.VISIBLE : View.INVISIBLE);
        holder.check_image.setImageDrawable(isSelect >= 0 ? ContextCompat.getDrawable(context, R.drawable.btn_selected) : ContextCompat.getDrawable(context, R.drawable.btn_unselected));


        holder.media_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int isSelect = isSelect(media);
                if (selectMedias.size() >= maxSelect && isSelect < 0) {
                    Toast.makeText(context, context.getString(R.string.msg_amount_limit), Toast.LENGTH_SHORT).show();
                } else {
                    if (media.size > maxSize) {
                        Toast.makeText(context, context.getString(R.string.msg_size_limit) + (FileUtils.fileSize(maxSize)), Toast.LENGTH_LONG).show();
                    } else {
                        holder.mask_view.setVisibility(isSelect >= 0 ? View.INVISIBLE : View.VISIBLE);
                        holder.check_image.setImageDrawable(isSelect >= 0 ? ContextCompat.getDrawable(context, R.drawable.btn_unselected) : ContextCompat.getDrawable(context, R.drawable.btn_selected));
                        setSelectMedias(media);
                        mOnItemClickListener.onItemClick(v, media, selectMedias);
                    }
                }

            }
        });
    }

    private void setSelectMedias(Media media) {
        int index = isSelect(media);
        if (index == -1) {
            selectMedias.add(media);
        } else {
            selectMedias.remove(index);
        }
    }

    /**
     * isSelect
     *
     * @param media media
     * @return 大于等于0 就是表示以选择，返回的是在selectMedias中的下标
     */
    private int isSelect(Media media) {
        int is = -1;
        if (selectMedias.size() <= 0) {
            return is;
        }
        for (int i = 0; i < selectMedias.size(); i++) {
            Media m = selectMedias.get(i);
            if (m.path.equals(media.path)) {
                is = i;
                break;
            }
        }
        return is;
    }

    public void updateAdapter(ArrayList<Media> list) {
        for (String selectPath : selectPaths) {
            for (Media media : list) {
                if (TextUtils.equals(media.path, selectPath)
                        && !selectMedias.contains(media)) {
                    selectMedias.add(media);
                }
            }
        }
        this.medias = list;
        notifyDataSetChanged();
    }

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public ArrayList<Media> getSelectMedias() {
        return selectMedias;
    }

    @Override
    public int getItemCount() {
        return medias.size();
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, Media data, ArrayList<Media> selectMedias);
    }
}

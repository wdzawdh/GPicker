package com.cw.picker.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cw.picker.PreviewActivity;
import com.cw.picker.R;
import com.cw.picker.entity.Media;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;


public class PreviewFragment extends Fragment {

    private ImageView play_view;

    public static PreviewFragment newInstance(Media media) {
        PreviewFragment f = new PreviewFragment();
        Bundle b = new Bundle();
        b.putParcelable("media", media);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.preview_fragment_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Media media = getArguments().getParcelable("media");
        play_view = view.findViewById(R.id.play_view);
        PhotoView photoView = view.findViewById(R.id.photoview);
        photoView.setMaximumScale(5);
        photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                PreviewActivity previewActivity = (PreviewActivity) getActivity();
                previewActivity.setBarStatus();
            }
        });
        setPlayView(media);
        Glide.with(getActivity()).load(media.path).into(photoView);
    }

    void setPlayView(final Media media) {
        if (media.mediaType == 3) { //3 视频
            play_view.setVisibility(View.VISIBLE);
            play_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(getUri(media.path), "video/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    if (isIntentAvailable(getActivity(), intent)) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.cant_play_video), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private Uri getUri(String path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".gplayer", new File(path));
        } else {
            return Uri.fromFile(new File(path));
        }
    }

    /**
     * 检查是否有可以处理的程序
     *
     * @param context context
     * @param intent  intent
     * @return
     */
    private boolean isIntentAvailable(Context context, Intent intent) {
        List resolves = context.getPackageManager().queryIntentActivities(intent, 0);
        return resolves.size() > 0;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
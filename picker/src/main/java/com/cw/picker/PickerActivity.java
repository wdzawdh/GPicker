package com.cw.picker;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cw.picker.adapter.FolderAdapter;
import com.cw.picker.adapter.MediaGridAdapter;
import com.cw.picker.adapter.SpacingDecoration;
import com.cw.picker.data.DataCallback;
import com.cw.picker.data.ImageLoader;
import com.cw.picker.data.MediaLoader;
import com.cw.picker.data.VideoLoader;
import com.cw.picker.entity.Folder;
import com.cw.picker.entity.Media;
import com.cw.picker.utils.ScreenUtils;
import com.cw.picker.utils.SystemBarHelper;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class PickerActivity extends AppCompatActivity implements DataCallback, View.OnClickListener {

    private Intent argsIntent;
    private RecyclerView recyclerView;
    private Button done, category_btn, preview;
    private MediaGridAdapter gridAdapter;
    private ListPopupWindow mFolderPopupWindow;
    private FolderAdapter mFolderAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        argsIntent = getIntent();
        setContentView(R.layout.main);

        recyclerView = findViewById(R.id.recycler_view);
        category_btn = findViewById(R.id.category_btn);
        done = findViewById(R.id.done);
        preview = findViewById(R.id.preview);

        findViewById(R.id.btn_back).setOnClickListener(this);
        done.setOnClickListener(this);
        category_btn.setOnClickListener(this);
        preview.setOnClickListener(this);

        setTitleBar();
        //get view end
        createAdapter();
        createFolderAdapter();
        getMediaData();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        SystemBarHelper.immersiveStatusBar(this);
        SystemBarHelper.setHeightAndPadding(this, findViewById(R.id.top));
    }

    public void setTitleBar() {
        int type = argsIntent.getIntExtra(PickerConfig.SELECT_MODE, PickerConfig.PICKER_IMAGE_VIDEO);
        if (type == PickerConfig.PICKER_IMAGE_VIDEO) {
            ((TextView) findViewById(R.id.bar_title)).setText(getString(R.string.select_title));
        } else if (type == PickerConfig.PICKER_IMAGE) {
            ((TextView) findViewById(R.id.bar_title)).setText(getString(R.string.select_image_title));
        } else if (type == PickerConfig.PICKER_VIDEO) {
            ((TextView) findViewById(R.id.bar_title)).setText(getString(R.string.select_video_title));
        }
    }

    void createAdapter() {
        //创建默认的线性LayoutManager
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, PickerConfig.GRID_SPAN_COUNT);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new SpacingDecoration(PickerConfig.GRID_SPAN_COUNT, PickerConfig.GRID_SPAN));
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        recyclerView.setHasFixedSize(true);
        //创建并设置Adapter
        ArrayList<String> select = argsIntent.getStringArrayListExtra(PickerConfig.DEFAULT_SELECTED_LIST);
        int maxSelect = argsIntent.getIntExtra(PickerConfig.MAX_SELECT_COUNT, PickerConfig.DEFAULT_SELECTED_MAX_COUNT);
        long maxSize = argsIntent.getLongExtra(PickerConfig.MAX_SELECT_SIZE, PickerConfig.DEFAULT_SELECTED_MAX_SIZE);
        gridAdapter = new MediaGridAdapter(this, select, maxSelect, maxSize);
        recyclerView.setAdapter(gridAdapter);
    }

    void createFolderAdapter() {
        ArrayList<Folder> folders = new ArrayList<>();
        mFolderAdapter = new FolderAdapter(folders, this);
        mFolderPopupWindow = new ListPopupWindow(this);
        mFolderPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        mFolderPopupWindow.setAdapter(mFolderAdapter);
        mFolderPopupWindow.setHeight((int) (ScreenUtils.getScreenHeight(this) * 0.6));
        mFolderPopupWindow.setAnchorView(findViewById(R.id.footer));
        mFolderPopupWindow.setModal(true);
        mFolderPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mFolderAdapter.setSelectIndex(position);
                category_btn.setText(mFolderAdapter.getItem(position).getName());
                gridAdapter.updateAdapter(mFolderAdapter.getSelectMedias());
                mFolderPopupWindow.dismiss();
            }
        });
    }

    void getMediaData() {
        int type = argsIntent.getIntExtra(PickerConfig.SELECT_MODE, PickerConfig.PICKER_IMAGE_VIDEO);
        String[] mimeTypes = argsIntent.getStringArrayExtra(PickerConfig.SELECT_MIME_TYPE);
        if (type == PickerConfig.PICKER_IMAGE_VIDEO) {
            getLoaderManager().initLoader(type, null, new MediaLoader(this,mimeTypes, this));
        } else if (type == PickerConfig.PICKER_IMAGE) {
            getLoaderManager().initLoader(type, null, new ImageLoader(this,mimeTypes, this));
        } else if (type == PickerConfig.PICKER_VIDEO) {
            getLoaderManager().initLoader(type, null, new VideoLoader(this,mimeTypes, this));
        }
    }

    @Override
    public void onData(ArrayList<Folder> list) {
        if (list.isEmpty()) {
            return;
        }
        setView(list);
        category_btn.setText(list.get(0).getName());
        mFolderAdapter.updateAdapter(list);
    }

    void setView(ArrayList<Folder> list) {
        gridAdapter.updateAdapter(list.get(0).getMedias());
        setButtonText();
        gridAdapter.setOnItemClickListener(new MediaGridAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, Media data, ArrayList<Media> selectMedias) {
                setButtonText();
            }
        });
    }

    void setButtonText() {
        int max = argsIntent.getIntExtra(PickerConfig.MAX_SELECT_COUNT, PickerConfig.DEFAULT_SELECTED_MAX_COUNT);
        done.setText(getString(R.string.done) + "(" + gridAdapter.getSelectMedias().size() + "/" + max + ")");
        preview.setText(getString(R.string.preview) + "(" + gridAdapter.getSelectMedias().size() + ")");
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_back) {
            ArrayList<Media> selectMedias = new ArrayList<>();
            done(selectMedias);
        } else if (id == R.id.category_btn) {
            if (mFolderPopupWindow.isShowing()) {
                mFolderPopupWindow.dismiss();
            } else {
                mFolderPopupWindow.show();
            }
        } else if (id == R.id.done) {
            done(gridAdapter.getSelectMedias());
        } else if (id == R.id.preview) {
            if (gridAdapter.getSelectMedias().size() <= 0) {
                Toast.makeText(this, getString(R.string.select_null), Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, PreviewActivity.class);
            intent.putExtra(PickerConfig.MAX_SELECT_COUNT, argsIntent.getIntExtra(PickerConfig.MAX_SELECT_COUNT, PickerConfig.DEFAULT_SELECTED_MAX_COUNT));
            intent.putExtra(PickerConfig.PRE_RAW_LIST, gridAdapter.getSelectMedias());
            this.startActivityForResult(intent, 200);
        }
    }

    public void done(ArrayList<Media> selects) {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(PickerConfig.EXTRA_RESULT, selects);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        Glide.get(this).clearMemory();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        ArrayList<Media> selectMedias = new ArrayList<>();
        done(selectMedias);
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200) {
            ArrayList<Media> selects = data.getParcelableArrayListExtra(PickerConfig.EXTRA_RESULT);
            if (resultCode == PickerConfig.RESULT_UPDATE_CODE) {
                setButtonText();
            } else if (resultCode == PickerConfig.RESULT_CODE) {
                done(selects);
            }
        }
    }
}

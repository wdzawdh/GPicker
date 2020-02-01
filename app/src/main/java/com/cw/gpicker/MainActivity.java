package com.cw.gpicker;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.cw.gpicker.permission.PermissionUtil;
import com.cw.gpicker.permission.ResultCallBack;
import com.cw.gpicker.takephoto.TakePhotoProxy;
import com.cw.gpicker.takephoto.TakePhotoUtils;
import com.cw.picker.PickerConfig;
import com.cw.picker.data.DataCallback;
import com.cw.picker.data.VideoLoader;
import com.cw.picker.entity.Folder;
import com.cw.picker.entity.Media;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> select = new ArrayList<>();
    private TakePhotoProxy takePhotoProxy = new TakePhotoProxy();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getLoaderManager().initLoader(PickerConfig.PICKER_VIDEO, null, new VideoLoader(this, null, new DataCallback() {
            @Override
            public void onData(ArrayList<Folder> list) {
                for (Folder folder : list) {
                    Log.d("folder", folder.name);
                    for (Media media : folder.getMedias()) {
                        Log.d("media", media.path + "---" + media.duration);
                    }
                }
            }
        }));

        findViewById(R.id.go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionUtil.with(MainActivity.this)
                        .add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .request(new ResultCallBack() {
                            @Override
                            public void onGrantedAll() {
                                openPicker();
                            }
                        });
            }
        });
    }

    private void openPicker() {
        takePhotoProxy.setCropNeed(true);
        takePhotoProxy.setPickCondition(20971520L, 2, select, "image/jpeg", "image/gif"); //20 MB
        takePhotoProxy.pickPhotoByPicker(this);
        takePhotoProxy.setOnTakePhotoListener(new TakePhotoUtils.OnTakePhotoListener() {
            @Override
            public void onTakePhoto(int index, String basePath, String treatedPath) {
                select.add(basePath);
                Log.d("cw", "index = " + index + "--->" + basePath);
            }
        });
    }

    /*
    1.正常使用方法

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == PickerConfig.RESULT_CODE) {
            select = data.getParcelableArrayListExtra(PickerConfig.EXTRA_RESULT);
            for (Media media : select) {
                Uri parse = Uri.parse(media.path);
                Log.d("cw", parse.toString());
            }
        }
    }

    private void openPicker() {
        Intent intent = new Intent(this, PickerActivity.class);
        intent.putExtra(PickerConfig.SELECT_MODE, PickerConfig.PICKER_IMAGE_VIDEO);//default image and video (Optional)
        long maxSize = 188743680L;//long long long
        intent.putExtra(PickerConfig.MAX_SELECT_SIZE, maxSize); //default 180MB (Optional)
        intent.putExtra(PickerConfig.MAX_SELECT_COUNT, 15);  //default 40 (Optional)
        intent.putExtra(PickerConfig.DEFAULT_SELECTED_LIST, select); // (Optional)
        intent.putExtra(PickerConfig.SELECT_MIME_TYPE, new String[]{"video/png", "image/gif"}); // (Optional)
        startActivityForResult(intent, 200);
    }


    2.使用 TakePhotoProxy

    private void openPicker() {
        takePhotoProxy.setCropNeed(false);
        takePhotoProxy.setPickCondition(20971520L, 2, select); //20 MB
        takePhotoProxy.pickVideoByPicker(this);
        takePhotoProxy.setOnTakePhotoListener(new TakePhotoUtils.OnTakePhotoListener() {
            @Override
            public void onTakePhoto(String basePath, String treatedPath) {
                select.add(basePath);
                Log.d("cw", basePath);
            }
        });
    }
    */
}

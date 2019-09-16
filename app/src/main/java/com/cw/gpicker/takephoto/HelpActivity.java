package com.cw.gpicker.takephoto;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cw
 * @date 2017/11/4
 */
public class HelpActivity extends Activity {

    public static final String HELP_INDEX = "index";
    public static final String HELP_MODE = "mode";
    public static final String HELP_PATH = "path";
    public static final String HELP_NEED_CROP = "needCrop";
    public static final String HELP_RATIO_X = "helpRatioY";
    public static final String HELP_RATIO_Y = "helpRatioX";
    public static final String HELP_PICK_MAX_SIZE = "helpPickMaxSize";
    public static final String HELP_PICK_MAX_COUNT = "helpPickMaxCount";
    public static final String HELP_PICK_MIME_TYPE = "helpPickMimeType";
    public static final String HELP_PICK_SELECT = "helpPickSelect";

    private static List<WeakReference<TakePhotoUtils>> sTakePhotoList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            handleIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    public static synchronized void setTakePhotoUtil(TakePhotoUtils takePhotoUtil) {
        if (takePhotoUtil == null) {
            return;
        }
        WeakReference<TakePhotoUtils> wTakePhoto = new WeakReference<>(takePhotoUtil);
        sTakePhotoList.add(wTakePhoto);
        takePhotoUtil.setIndex(sTakePhotoList.size() - 1);
    }

    private void handleIntent(Intent intent) {
        int index = intent.getIntExtra(HELP_INDEX, -1);
        if (index < 0 || index >= sTakePhotoList.size()) {
            finish();
            return;
        }
        TakePhotoUtils takePhotoUtils = sTakePhotoList.get(index).get();
        if (takePhotoUtils == null) {
            finish();
            return;
        }

        String mode = intent.getStringExtra(HELP_MODE);
        String path = intent.getStringExtra(HELP_PATH);
        int cropRatioX = intent.getIntExtra(HELP_RATIO_X, 1);
        int cropRatioY = intent.getIntExtra(HELP_RATIO_Y, 1);
        boolean cropNeed = intent.getBooleanExtra(HELP_NEED_CROP, false);
        long pickMaxSize = intent.getLongExtra(HelpActivity.HELP_PICK_MAX_SIZE, 188743680L);
        int pickMaxCount = intent.getIntExtra(HelpActivity.HELP_PICK_MAX_COUNT, 40);
        String[] mimeTypes = intent.getStringArrayExtra(HelpActivity.HELP_PICK_MIME_TYPE);
        ArrayList<String> pickSelect = intent.getStringArrayListExtra(HelpActivity.HELP_PICK_SELECT);

        takePhotoUtils.setCropNeed(cropNeed);
        takePhotoUtils.setCropRatio(cropRatioX, cropRatioY);
        takePhotoUtils.setPickCondition(pickMaxSize, pickMaxCount, pickSelect, mimeTypes);

        switch (mode) {
            case "takeCameraBySystem":
                takePhotoUtils.takeCameraBySystem(this);
                break;
            case "pickPhotoBySystem":
                takePhotoUtils.pickPhotoBySystem(this);
                break;
            case "pickPhotoByPicker":
                takePhotoUtils.pickPhotoByPicker(this);
                break;
            case "pickVideoBySystem":
                takePhotoUtils.pickVideoBySystem(this);
                break;
            case "pickVideoByPicker":
                takePhotoUtils.pickVideoByPicker(this);
                break;
            case "cropImage":
                takePhotoUtils.uCropImage(this, path);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (WeakReference<TakePhotoUtils> weakReferences : sTakePhotoList) {
            TakePhotoUtils utils = weakReferences.get();
            if (utils != null && utils.onActivityResult(this, requestCode, resultCode, data)) {
                finish();
            }
        }
    }
}

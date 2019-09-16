package com.cw.gpicker.takephoto;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;

import com.cw.gpicker.permission.PermissionUtil;
import com.cw.gpicker.permission.ResultCallBack;

import java.util.ArrayList;


/**
 * @author cw
 * @date 2017/11/4
 */
public class TakePhotoProxy {

    private TakePhotoUtils mTakePhotoUtils;

    public TakePhotoProxy() {
        mTakePhotoUtils = new TakePhotoUtils();
        HelpActivity.setTakePhotoUtil(mTakePhotoUtils);
    }

    public void setOnTakePhotoListener(TakePhotoUtils.OnTakePhotoListener listener) {
        mTakePhotoUtils.setOnTakePhotoListener(listener);
    }

    //crop
    private boolean mCropNeed = true;
    private int mCropRatioX = 1;
    private int mCropRatioY = 1;
    //pick
    private long mPickMaxSize = 188743680L; //default 180MB
    private int mPickMaxCount = 40;
    private ArrayList<String> mPickSelect;
    private String[] mMimeTypes;

    public void setCropNeed(boolean cropNeed) {
        mCropNeed = cropNeed;
    }

    public void setCropRatio(int ratioX, int ratioY) {
        mCropRatioX = ratioX;
        mCropRatioY = ratioY;
    }

    public void setPickCondition(long maxSize, int maxCount, ArrayList<String> select, String... mimeType) {
        mPickMaxSize = maxSize;
        mPickMaxCount = maxCount;
        mPickSelect = select;
        mMimeTypes = mimeType;
    }

    /**
     * 申请权限，启动系统相机,保存图片
     */
    public void takeCamera(final Activity act) {
        PermissionUtil.with(act)
                .add(Manifest.permission.CAMERA)
                .request(new ResultCallBack() {
                    @Override
                    public void onGrantedAll() {
                        takeCameraBySystem(act);
                    }
                });
    }

    /**
     * 启动系统相机,保存图片
     */
    private void takeCameraBySystem(Activity act) {
        Intent intent = new Intent(act, HelpActivity.class);
        intent.putExtra(HelpActivity.HELP_INDEX, mTakePhotoUtils.getIndex());
        intent.putExtra(HelpActivity.HELP_MODE, "takeCameraBySystem");
        intent.putExtra(HelpActivity.HELP_NEED_CROP, mCropNeed);
        intent.putExtra(HelpActivity.HELP_RATIO_X, mCropRatioX);
        intent.putExtra(HelpActivity.HELP_RATIO_Y, mCropRatioY);
        act.startActivity(intent);
    }

    /**
     * 打开系统图库
     */
    public void pickPhotoBySystem(Activity act) {
        Intent intent = new Intent(act, HelpActivity.class);
        intent.putExtra(HelpActivity.HELP_INDEX, mTakePhotoUtils.getIndex());
        intent.putExtra(HelpActivity.HELP_MODE, "pickPhotoBySystem");
        intent.putExtra(HelpActivity.HELP_NEED_CROP, mCropNeed);
        intent.putExtra(HelpActivity.HELP_RATIO_X, mCropRatioX);
        intent.putExtra(HelpActivity.HELP_RATIO_Y, mCropRatioY);
        act.startActivity(intent);
    }

    /**
     * 打开GPicker图库
     */
    public void pickPhotoByPicker(Activity act) {
        Intent intent = new Intent(act, HelpActivity.class);
        intent.putExtra(HelpActivity.HELP_INDEX, mTakePhotoUtils.getIndex());
        intent.putExtra(HelpActivity.HELP_MODE, "pickPhotoByPicker");
        intent.putExtra(HelpActivity.HELP_NEED_CROP, mCropNeed);
        intent.putExtra(HelpActivity.HELP_PICK_MAX_SIZE, mPickMaxSize);
        intent.putExtra(HelpActivity.HELP_PICK_MAX_COUNT, mPickMaxCount);
        intent.putExtra(HelpActivity.HELP_PICK_MIME_TYPE, mMimeTypes);
        intent.putStringArrayListExtra(HelpActivity.HELP_PICK_SELECT, mPickSelect);
        act.startActivity(intent);
    }

    /**
     * 打开系统视频
     */
    public void pickVideoBySystem(Activity act) {
        Intent intent = new Intent(act, HelpActivity.class);
        intent.putExtra(HelpActivity.HELP_INDEX, mTakePhotoUtils.getIndex());
        intent.putExtra(HelpActivity.HELP_MODE, "pickVideoBySystem");
        act.startActivity(intent);
    }

    /**
     * 打开GPicker视频
     */
    public void pickVideoByPicker(Activity act) {
        Intent intent = new Intent(act, HelpActivity.class);
        intent.putExtra(HelpActivity.HELP_INDEX, mTakePhotoUtils.getIndex());
        intent.putExtra(HelpActivity.HELP_MODE, "pickVideoByPicker");
        intent.putExtra(HelpActivity.HELP_PICK_MAX_SIZE, mPickMaxSize);
        intent.putExtra(HelpActivity.HELP_PICK_MAX_COUNT, mPickMaxCount);
        intent.putExtra(HelpActivity.HELP_PICK_MIME_TYPE, mMimeTypes);
        intent.putStringArrayListExtra(HelpActivity.HELP_PICK_SELECT, mPickSelect);
        act.startActivity(intent);
    }

    /**
     * 裁剪图片
     */
    public void cropImage(Activity act, String path) {
        Intent intent = new Intent(act, HelpActivity.class);
        intent.putExtra(HelpActivity.HELP_INDEX, mTakePhotoUtils.getIndex());
        intent.putExtra(HelpActivity.HELP_MODE, "cropImage");
        intent.putExtra(HelpActivity.HELP_PATH, path);
        intent.putExtra(HelpActivity.HELP_RATIO_X, mCropRatioX);
        intent.putExtra(HelpActivity.HELP_RATIO_Y, mCropRatioY);
        act.startActivity(intent);
    }

}

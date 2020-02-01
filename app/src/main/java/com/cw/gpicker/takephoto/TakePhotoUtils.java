package com.cw.gpicker.takephoto;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.widget.Toast;

import com.cw.gpicker.R;
import com.cw.picker.PickerActivity;
import com.cw.picker.PickerConfig;
import com.cw.picker.entity.Media;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import androidx.core.app.ActivityCompat;

/**
 * 调用系统拍照、图库、裁剪
 *
 * @author cw
 * @date 2017/9/11
 */
public class TakePhotoUtils implements Serializable {

    private static final String FOLDER_CACHE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/GPicker/";
    private static final String FOLDER_IMG_PATH = FOLDER_CACHE_PATH + "image/";

    private int RESULT_CAMERA_ONLY = 10000;//拍摄并保存完照片的结果码
    private int RESULT_PICK_IMAGE = 20000;//选择图片后的结果码
    private int RESULT_PICK_IMAGE_PICKER = 30000;//选择图片后的结果码
    private int RESULT_PICK_VIDEO = 40000;//选择视频后的结果码
    private int RESULT_PICK_VIDEO_PICKER = 50000;//选择视频后的结果码
    private int RESULT_CAMERA_CROP_RESULT = 60000;//裁剪并保存完照片的结果码

    private String mCameraPath;
    private String mCropPath;
    private String mBasePath;
    private int mIndex;

    //crop
    private boolean mCropNeed = true;
    private int mCropRatioX = 1;
    private int mCropRatioY = 1;
    //pick
    private long mPickMaxSize = 188743680L; //default 180MB
    private int mPickMaxCount = 40;
    private ArrayList<String> mPickSelect;
    private String[] mMimeTypes;

    public TakePhotoUtils() {
        String path = FOLDER_IMG_PATH;
        File parentFile = new File(path);
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
    }

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

    public void setIndex(int index) {
        mIndex = index;
        RESULT_CAMERA_ONLY += index;
        RESULT_PICK_IMAGE += index;
        RESULT_PICK_IMAGE_PICKER += index;
        RESULT_PICK_VIDEO += index;
        RESULT_PICK_VIDEO_PICKER += index;
        RESULT_CAMERA_CROP_RESULT += index;
    }

    public int getIndex() {
        return mIndex;
    }

    /**
     * 启动系统相机,保存图片
     */
    public void takeCameraBySystem(Activity act) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(act, "SD卡未挂载", Toast.LENGTH_SHORT).show();
            return;
        }
        mCameraPath = FOLDER_IMG_PATH + System.currentTimeMillis() + ".jpg";
        File file = new File(mCameraPath);
        Uri imageUri = Uri.fromFile(file);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        act.startActivityForResult(intent, RESULT_CAMERA_ONLY);
    }

    /**
     * 打开系统图库
     */
    public void pickPhotoBySystem(Activity act) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(act, "SD卡未挂载", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        act.startActivityForResult(i, RESULT_PICK_IMAGE);
    }

    /**
     * 打开GPicker图库
     */
    public void pickPhotoByPicker(Activity act) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(act, "SD卡未挂载", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(act, PickerActivity.class);
        intent.putExtra(PickerConfig.SELECT_MODE, PickerConfig.PICKER_IMAGE);
        intent.putExtra(PickerConfig.SELECT_MIME_TYPE, mMimeTypes);
        intent.putExtra(PickerConfig.MAX_SELECT_SIZE, mPickMaxSize);
        intent.putExtra(PickerConfig.MAX_SELECT_COUNT, mCropNeed ? 1 : mPickMaxCount);
        intent.putStringArrayListExtra(PickerConfig.DEFAULT_SELECTED_LIST, mPickSelect);
        act.startActivityForResult(intent, RESULT_PICK_IMAGE_PICKER);
    }

    /**
     * 打开系统视频
     */
    public void pickVideoBySystem(Activity act) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(act, "SD卡未挂载", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        act.startActivityForResult(i, RESULT_PICK_VIDEO);
    }

    /**
     * 打开GPicker视频
     */
    public void pickVideoByPicker(Activity act) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(act, "SD卡未挂载", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(act, PickerActivity.class);
        intent.putExtra(PickerConfig.SELECT_MODE, PickerConfig.PICKER_VIDEO);
        intent.putExtra(PickerConfig.SELECT_MIME_TYPE, mMimeTypes);
        intent.putExtra(PickerConfig.MAX_SELECT_SIZE, mPickMaxSize);
        intent.putExtra(PickerConfig.MAX_SELECT_COUNT, mPickMaxCount);
        intent.putStringArrayListExtra(PickerConfig.DEFAULT_SELECTED_LIST, mPickSelect);
        act.startActivityForResult(intent, RESULT_PICK_VIDEO_PICKER);
    }

    /**
     * 系统裁剪图片
     */
    public void cropImage(Activity act, String path) {
        if (path == null) {
            return;
        }
        File file = new File(path);
        Uri imageUri = getImageContentUri(act, file);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(imageUri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", mCropRatioX);
        intent.putExtra("aspectY", mCropRatioY);
        intent.putExtra("outputX", 450);
        intent.putExtra("outputY", 450);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);// 黑边
        intent.putExtra("return-data", false);
        mCropPath = FOLDER_IMG_PATH + System.currentTimeMillis() + ".jpg";
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCropPath)));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        act.startActivityForResult(intent, RESULT_CAMERA_CROP_RESULT);
    }

    /**
     * uCrop裁剪图片
     */
    public void uCropImage(Activity act, String path) {
        if (path == null) {
            return;
        }

        File file = new File(path);
        Uri imageUri = getImageContentUri(act, file);
        mCropPath = FOLDER_IMG_PATH + System.currentTimeMillis() + ".jpg";
        if (imageUri == null) {
            return;
        }
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ActivityCompat.getColor(act, R.color.colorPrimary));
        options.setStatusBarColor(ActivityCompat.getColor(act, R.color.colorPrimary));
        options.setFreeStyleCropEnabled(false);
        UCrop.of(imageUri, Uri.fromFile(new File(mCropPath)))
                .withOptions(options)
                .withAspectRatio(mCropRatioX, mCropRatioY)
                .withMaxResultSize(450, 450)
                .start(act);
    }

    /**
     * 处理onActivityResult结果
     *
     * @return 是否结束调用
     */
    boolean onActivityResult(Activity act, int requestCode, int resultCode, final Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return true;
        }
        //system camera
        if (requestCode == RESULT_CAMERA_ONLY) {
            mBasePath = mCameraPath;
            if (mCropNeed) {
                uCropImage(act, mCameraPath);
                return false;
            }
            if (listener != null) {
                listener.onTakePhoto(0, mBasePath, mCameraPath);
            }
            return true;
        }
        //system pick image
        if (requestCode == RESULT_PICK_IMAGE) {
            String pickPhotoPath = getPickMediaPath(act, data, MediaStore.Images.Media.DATA);
            mBasePath = pickPhotoPath;
            if (mCropNeed) {
                uCropImage(act, pickPhotoPath);
                return false;
            }
            if (listener != null) {
                listener.onTakePhoto(0, mBasePath, pickPhotoPath);
            }
            return true;
        }
        //GPicker image
        if (requestCode == RESULT_PICK_IMAGE_PICKER) {
            List<Parcelable> paths = data.getParcelableArrayListExtra(PickerConfig.EXTRA_RESULT);
            if (paths != null && !paths.isEmpty()) {
                for (int i = 0; i < paths.size(); i++) {
                    Media media = (Media) paths.get(i);
                    String pickPhotoPath = media.path;
                    mBasePath = pickPhotoPath;
                    if (mCropNeed) {
                        uCropImage(act, pickPhotoPath);
                        return false;
                    }
                    if (listener != null) {
                        listener.onTakePhoto(i, mBasePath, pickPhotoPath);
                    }
                }
            }
            return true;
        }
        //system pick video
        if (requestCode == RESULT_PICK_VIDEO) {
            String pickVideoPath = getPickMediaPath(act, data, MediaStore.Video.Media.DATA);
            mBasePath = pickVideoPath;
            if (listener != null) {
                listener.onTakePhoto(0, mBasePath, pickVideoPath);
            }
            return true;
        }
        //GPicker video
        if (requestCode == RESULT_PICK_VIDEO_PICKER) {
            List<Parcelable> paths = data.getParcelableArrayListExtra(PickerConfig.EXTRA_RESULT);
            if (paths != null && !paths.isEmpty()) {
                for (int i = 0; i < paths.size(); i++) {
                    Media media = (Media) paths.get(i);
                    String pickVideoPath = media.path;
                    mBasePath = pickVideoPath;
                    if (listener != null) {
                        listener.onTakePhoto(i, mBasePath, pickVideoPath);
                    }
                }
            }
            return true;
        }
        //system crop image
        if (requestCode == RESULT_CAMERA_CROP_RESULT) {
            if (listener != null) {
                listener.onTakePhoto(0, mBasePath, mCropPath);
            }
            return true;
        }
        //UCrop crop image
        if (requestCode == UCrop.REQUEST_CROP) {
            // Uri resultUri = UCrop.getOutput(data);
            if (listener != null) {
                listener.onTakePhoto(0, mBasePath, mCropPath);
            }
            return true;
        }
        //到这里说明不是自己的返回码，不处理
        return false;
    }

    /**
     * 将File转换为Content Uri
     * 为适配 7.0 原来的File Uri需要更改为Content Uri
     */
    private static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            Uri uri = Uri.withAppendedPath(baseUri, "" + id);
            cursor.close();
            return uri;
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    /**
     * 获取图库中选择的图片的路径
     */
    private String getPickMediaPath(Activity act, Intent data, String type) {
        //系统图库
        Uri selectedImage = data.getData();
        String[] filePathColumn = {type};
        Cursor cursor = act.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            return cursor.getString(columnIndex);
        }
        return null;
    }

    public void setOnTakePhotoListener(OnTakePhotoListener listener) {
        this.listener = listener;
    }

    private OnTakePhotoListener listener;

    public interface OnTakePhotoListener {
        void onTakePhoto(int index, String basePath, String treatedPath);
    }
}

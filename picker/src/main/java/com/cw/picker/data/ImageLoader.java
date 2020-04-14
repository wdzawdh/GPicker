package com.cw.picker.data;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.cw.picker.R;
import com.cw.picker.entity.Folder;
import com.cw.picker.entity.Media;

import java.util.ArrayList;


public class ImageLoader extends LoaderM implements LoaderManager.LoaderCallbacks {

    private String[] IMAGE_PROJECTION = {
            MediaStore.Images.Media.DATA,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media._ID};

    private Context mContext;
    private String[] mMineTypes;
    private DataCallback mLoader;

    public ImageLoader(Context context, String[] mineTypes, DataCallback loader) {
        this.mContext = context;
        this.mMineTypes = mineTypes;
        this.mLoader = loader;
    }

    @Override
    public Loader onCreateLoader(int picker_type, Bundle bundle) {
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + mineFilter(mMineTypes);

        Uri queryUri = MediaStore.Files.getContentUri("external");
        //Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        return new CursorLoader(
                mContext,
                queryUri,
                IMAGE_PROJECTION,
                selection,
                null, // Selection args (none).
                MediaStore.Images.Media.DATE_MODIFIED + " DESC" // Sort order.
        );
    }

    @Override
    public void onLoadFinished(Loader loader, Object o) {
        ArrayList<Folder> folders = new ArrayList<>();
        Folder allFolder = new Folder(mContext.getResources().getString(R.string.all_image));
        folders.add(allFolder);
        Cursor cursor = (Cursor) o;
        if (cursor.isClosed()) {
            return;
        }
        while (cursor.moveToNext()) {
            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
            long dateTime = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED));
            int mediaType = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)); // 0 图片
            long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));

            if (size < 1) continue;
            String dirName = getParent(path);
            Media media = new Media(path, name, dateTime, mediaType, size, 0, id, dirName);
            allFolder.addMedias(media);

            int index = hasDir(folders, dirName);
            if (index != -1) {
                folders.get(index).addMedias(media);
            } else {
                Folder folder = new Folder(dirName);
                folder.addMedias(media);
                folders.add(folder);
            }
        }
        mLoader.onData(folders);
        cursor.close();
    }

    @Override
    public void onLoaderReset(Loader loader) {
    }

}
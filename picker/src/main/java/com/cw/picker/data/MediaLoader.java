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


public class MediaLoader extends LoaderM implements LoaderManager.LoaderCallbacks {

    private String[] MEDIA_PROJECTION = {
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.PARENT};

    private Context mContext;
    private String[] mMineTypes;
    private DataCallback mLoader;

    public MediaLoader(Context context, String[] mineTypes, DataCallback loader) {
        this.mContext = context;
        this.mMineTypes = mineTypes;
        this.mLoader = loader;
    }

    @Override
    public Loader onCreateLoader(int picker_type, Bundle bundle) {
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                + mineFilter(mMineTypes);

        Uri queryUri = MediaStore.Files.getContentUri("external");
        return new CursorLoader(
                mContext,
                queryUri,
                MEDIA_PROJECTION,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
        );
    }

    @Override
    public void onLoadFinished(Loader loader, Object o) {
        ArrayList<Folder> folders = new ArrayList<>();
        Folder allFolder = new Folder(mContext.getResources().getString(R.string.all_dir_name));
        folders.add(allFolder);
        Folder allVideoDir = new Folder(mContext.getResources().getString(R.string.video_dir_name));
        folders.add(allVideoDir);
        Cursor cursor = (Cursor) o;
        if (cursor.isClosed()) {
            return;
        }
        while (cursor.moveToNext()) {
            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME));
            long dateTime = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED));
            int mediaType = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE));
            long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE));
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));

            if (size < 1) continue;
            String dirName = getParent(path);
            Media media = new Media(path, name, dateTime, mediaType, size, id, dirName);
            allFolder.addMedias(media);
            if (mediaType == 3) { //3 视频
                allVideoDir.addMedias(media);
            }

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

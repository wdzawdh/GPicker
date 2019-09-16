package com.cw.picker.data;


import android.provider.MediaStore;
import android.text.TextUtils;

import com.cw.picker.entity.Folder;

import java.util.ArrayList;


public class LoaderM {

    protected String mineFilter(String[] mineTypes) {
        if (mineTypes == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mineTypes.length; i++) {
            if (i == 0) sb.append(" AND (");
            sb.append(MediaStore.Files.FileColumns.MIME_TYPE).append("='").append(mineTypes[i]).append("'");
            if (i != mineTypes.length - 1) {
                sb.append(" OR ");
            } else {
                sb.append(")");
            }
        }
        return sb.toString();
    }

    protected String getParent(String path) {
        String sp[] = path.split("/");
        return sp[sp.length - 2];
    }

    protected int hasDir(ArrayList<Folder> folders, String dirName) {
        for (int i = 0; i < folders.size(); i++) {
            Folder folder = folders.get(i);
            if (TextUtils.equals(folder.getName(), dirName)) {
                return i;
            }
        }
        return -1;
    }
}

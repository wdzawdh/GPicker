package com.cw.picker.data;


import com.cw.picker.entity.Folder;

import java.util.ArrayList;


public interface DataCallback {

    void onData(ArrayList<Folder> list);
}

package com.app.videocompression;

import android.content.Context;
import android.os.Environment;


public class FilePickerHelper {

    public static final String BASE_DIRECTORY_PATH = Environment.getExternalStorageDirectory() + "/Android/data/";

    public static String getBaseDirectoryPath(Context context){
        return BASE_DIRECTORY_PATH + context.getPackageName() + "/UFINITY";
    }

    public static String getCompressDirectoryPath(Context context){
        return getBaseDirectoryPath(context) + "/Temp";
    }
}

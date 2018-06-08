package com.app.videocompression.video;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.app.videocompression.utils.ProgressCalculator;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.io.File;


public class VideoCompressor {

    public static final int SUCCESS = 1;
    public static final int FAILED = 2;
    public static final int NONE = 3;
    public static final int RUNNING = 4;

    private final Context context;
    private final ProgressCalculator mProgressCalculator;
    private boolean isFinished;
    private int status = NONE;
    private String errorMessage = "Compression Failed!";

    public VideoCompressor(Context context) {
        this.context = context;
        mProgressCalculator = new ProgressCalculator();
    }

    public void startCompressing(String inputPath, CompressionListener listener) {
        if (inputPath == null || inputPath.isEmpty()) {
            status = NONE;
            if (listener != null) {
                listener.compressionFinished(NONE, false, null);
            }
            return;
        }

        String outputPath = "";
        outputPath = getAppDir() + "/video_compress.mp4";
        String[] commandParams = new String[26];
        commandParams[0] = "-y";
        commandParams[1] = "-i";
        commandParams[2] = inputPath;
        commandParams[3] = "-s";
        commandParams[4] = "240x320";
        commandParams[5] = "-r";
        commandParams[6] = "20";
        commandParams[7] = "-c:v";
        commandParams[8] = "libx264";
        commandParams[9] = "-preset";
        commandParams[10] = "ultrafast";
        commandParams[11] = "-c:a";
        commandParams[12] = "copy";
        commandParams[13] = "-me_method";
        commandParams[14] = "zero";
        commandParams[15] = "-tune";
        commandParams[16] = "fastdecode";
        commandParams[17] = "-tune";
        commandParams[18] = "zerolatency";
        commandParams[19] = "-strict";
        commandParams[20] = "-2";
        commandParams[21] = "-b:v";
        commandParams[22] = "1000k";
        commandParams[23] = "-pix_fmt";
        commandParams[24] = "yuv420p";
        commandParams[25] = outputPath;

        compressVideo(commandParams, outputPath, listener);

    }

    public String getAppDir() {
        String outputPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        outputPath += "/" + "vvvvv";
        File file = new File(outputPath);
        if (!file.exists()) {
            file.mkdir();
        }
        outputPath += "/" + "videocompress";
        file = new File(outputPath);
        if (!file.exists()) {
            file.mkdir();
        }
        return outputPath;
    }

    private void compressVideo(String[] command, final String outputFilePath, final CompressionListener listener) {
        try {

            FFmpeg.getInstance(context).execute(command, new FFmpegExecuteResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    status = SUCCESS;
                }

                @Override
                public void onProgress(String message) {
                    status = RUNNING;
                    Log.e("VideoCronProgress", message);
                    int progress = mProgressCalculator.calcProgress(message);
                    Log.e("VideoCronProgress == ", progress + "..");
                    if (progress != 0 && progress <= 100) {
                        if (progress >= 99) {
                            progress = 100;
                        }
                        listener.onProgress(progress);
                    }
                }

                @Override
                public void onFailure(String message) {
                    status = FAILED;
                    Log.e("VideoCompressor", message);
                    if (listener != null) {
                        listener.onFailure("Error : " + message);
                    }
                }

                @Override
                public void onStart() {

                }

                @Override
                public void onFinish() {
                    Log.e("VideoCronProgress", "finnished");
                    isFinished = true;
                    if (listener != null) {
                        listener.compressionFinished(status, true, outputFilePath);
                    }
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            status = FAILED;
            errorMessage = e.getMessage();
            if (listener != null) {
                listener.onFailure("Error : " + e.getMessage());
            }
        }
    }

    public interface CompressionListener {
        void compressionFinished(int status, boolean isVideo, String fileOutputPath);

        void onFailure(String message);

        void onProgress(int progress);
    }

    public boolean isDone() {
        return status == SUCCESS || status == NONE;
    }

}

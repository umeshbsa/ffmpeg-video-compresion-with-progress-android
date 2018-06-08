package com.app.videocompression.video;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.app.videocompression.utils.ProgressCalculator;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class VideoMerger {

    public static final int SUCCESS = 1;
    public static final int FAILED = 2;
    public static final int NONE = 3;
    public static final int RUNNING = 4;

    private final Context context;
    private final ProgressCalculator mProgressCalculator;
    private boolean isFinished;
    private int status = NONE;
    private String errorMessage = "Compression Failed!";

    public VideoMerger(Context context) {
        this.context = context;
        mProgressCalculator = new ProgressCalculator();
    }

    public void startMerging(List<String> videoFilenameList, MergingCallback listener) {

        String outputPath = "";
        outputPath = getAppDir() + "/video_merge.mp4";

        List<String> cmdList = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < videoFilenameList.size(); i++) {
            cmdList.add("-i");
            cmdList.add(videoFilenameList.get(i));

            sb.append("[").append(i).append(":0] [").append(i).append(":1]");
        }
        sb.append(" concat=n=").append(videoFilenameList.size()).append(":v=1:a=1 [v] [a]");
        cmdList.add("-filter_complex");
        cmdList.add(sb.toString());
        cmdList.add("-map");
        cmdList.add("[v]");
        cmdList.add("-map");
        cmdList.add("[a]");
        cmdList.add("-preset");
        cmdList.add("ultrafast");
        cmdList.add(outputPath);

        sb = new StringBuilder();
        for (String str : cmdList) {
            sb.append(str).append(" ");
        }

        String[] cmd = cmdList.toArray(new String[cmdList.size()]);
        compressVideo(cmd, outputPath, listener);

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

    private void compressVideo(String[] command, final String outputFilePath, final MergingCallback listener) {
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
                        listener.onMergeComplete(outputFilePath);
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

    public interface MergingCallback {
        void onMergeComplete(String fileOutputPath);

        void onFailure(String message);

        void onProgress(int progress);
    }

    public boolean isDone() {
        return status == SUCCESS || status == NONE;
    }

}

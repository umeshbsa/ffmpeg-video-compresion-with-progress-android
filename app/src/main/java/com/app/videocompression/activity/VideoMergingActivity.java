package com.app.videocompression.activity;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.videocompression.R;
import com.app.videocompression.video.VideoMerger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoMergingActivity extends AppCompatActivity {

    private VideoMerger mVideoMerger = new VideoMerger(this);
    private TextView tv_output_path;
    private String mInputPath1;
    private String mInputPath2;
    private TextView tv_source_path;
    private ProgressBar progress_bar;
    private TextView tv_progress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_merger);

        // This is your input path of video
        String sdcardDir = Environment.getExternalStorageDirectory().getAbsolutePath();

        mInputPath1 = sdcardDir + "/DCIM/Camera/video_70_mb.mp4"; // change this path according to your path
        mInputPath2 = sdcardDir + "/DCIM/Camera/video_70_mb.mp4";


        tv_source_path = (TextView) findViewById(R.id.tv_source_path);
        tv_output_path = (TextView) findViewById(R.id.tv_output_path);

        tv_progress = (TextView) findViewById(R.id.tv_progress);
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);

        tv_source_path.setText("Input path :\n1. " + mInputPath1 + "\n\n2. " + mInputPath2);

        findViewById(R.id.btn_compress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startMediaCompression();
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
        } else {
            startMediaCompression();
        }
    }

    private void startMediaCompression() {
        progress_bar.setVisibility(View.VISIBLE);

        List<String> inputVideoList = new ArrayList<>();
        inputVideoList.add(mInputPath1);
        inputVideoList.add(mInputPath2);

        mVideoMerger.startMerging(inputVideoList, new VideoMerger.MergingCallback() {
            @Override
            public void onMergeComplete(String fileOutputPath) {
                if (mVideoMerger.isDone()) {
                    File outputFile = new File(fileOutputPath);
                    long outputCompressVideosize = outputFile.length();
                    long fileSizeInKB = outputCompressVideosize / 1024;
                    long fileSizeInMB = fileSizeInKB / 1024;

                    String s = "Output video path : " + fileOutputPath + "\n" +
                            "Output video size : " + fileSizeInMB + "mb";
                    tv_output_path.setText(s);
                }
                progress_bar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(String message) {
                progress_bar.setVisibility(View.INVISIBLE);
                tv_output_path.setText("Some error find. please try again : " + message);
            }

            @Override
            public void onProgress(final int progress) {
                progress_bar.setProgress(progress);
                tv_progress.post(new Runnable() {
                    @Override
                    public void run() {
                        tv_progress.setText(progress + "%");
                    }
                });
            }
        });
    }
}

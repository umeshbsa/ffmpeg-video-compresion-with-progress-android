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
import com.app.videocompression.video.VideoCompressor;

import java.io.File;

public class VideoCompressionActivity extends AppCompatActivity {

    private VideoCompressor mVideoCompressor = new VideoCompressor(this);
    private TextView tv_output_path;
    private String mInputPath;
    private TextView tv_source_path;
    private ProgressBar progress_bar;
    private TextView tv_progress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_compression);

        // This is your input path of video
        String sdcardDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        mInputPath = sdcardDir + "/DCIM/Camera/video_70_mb.mp4"; // change this path according to your path

        tv_source_path = (TextView) findViewById(R.id.tv_source_path);
        tv_output_path = (TextView) findViewById(R.id.tv_output_path);

        tv_progress = (TextView) findViewById(R.id.tv_progress);
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);

        File inputFile = new File(mInputPath);
        long inputVideoSize = inputFile.length();

        long fileSizeInKB = inputVideoSize / 1024;
        long fileSizeInMB = fileSizeInKB / 1024;

        String s = "Input video path : " + mInputPath + "\n" +
                "Input video size : " + fileSizeInMB + "mb";

        tv_source_path.setText(s);

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
        mVideoCompressor.startCompressing(mInputPath, new VideoCompressor.CompressionListener() {
            @Override
            public void compressionFinished(int status, boolean isVideo, String fileOutputPath) {

                if (mVideoCompressor.isDone()) {
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

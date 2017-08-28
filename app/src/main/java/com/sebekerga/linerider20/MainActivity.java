package com.sebekerga.linerider20;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mIntermediateMat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.show_camera_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public void onCameraViewStarted(int width, int height) {
        mIntermediateMat = new Mat();
    }

    public void onCameraViewStopped() {
        if (mIntermediateMat != null)
            mIntermediateMat.release();
        mIntermediateMat = null;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat src = inputFrame.rgba();
        Mat input = new Mat(720, 1280, CvType.CV_32FC2);
        Mat output = new Mat(720, 1280, CvType.CV_32FC2);

        Point srcPts[] = new Point[4];
        srcPts[0] = new Point(713, 177);
        srcPts[1] = new Point(718, 543);
        srcPts[2] = new Point(1244, 51);
        srcPts[3] = new Point(1249, 653);

        Point targetPts[] = new Point[4];
        targetPts[0] = new Point(0, 0);
        targetPts[1] = new Point(0, 720);
        targetPts[2] = new Point(1280, 0);
        targetPts[3] = new Point(1280, 720);


        input.put(720, 1280, srcPts[0].x, srcPts[0].y, srcPts[1].x, srcPts[1].y, srcPts[2].x, srcPts[2].y, srcPts[3].x, srcPts[3].y);
        output.put(720, 1280, targetPts[0].x, targetPts[0].y, targetPts[1].x, targetPts[1].y, targetPts[2].x, targetPts[2].y, targetPts[3].x, targetPts[3].y);
        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(input, output);

        Mat dst = src.clone();
        Imgproc.warpPerspective(src, dst, perspectiveTransform, new Size(1280, 720));

        return dst;

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("TAG", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("TAG", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
}

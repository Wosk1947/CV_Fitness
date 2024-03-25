package org.weirdmotionslab.samples.pushupsgame;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.QRCodeDetector;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.SurfaceView;

import java.io.PrintWriter;

import java.net.Socket;

public class BattleActivity extends CameraActivity implements OnTouchListener, CvCameraViewListener2 {
    private static final String  TAG              = "OCVSample::Activity";

    private boolean              mIsColorSelected = false;
    private Mat                  mRgba;
    private Mat                  reduced;
    private Mat                  previousFrame;
    private Mat                  previousDiffFrame;
    private Mat                  combinedFrame;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;
    private QRCodeDetector       decoder;
    private Size                 frameSize;
    public static double                X;
    public static double                Y;
    public int frameCount = 0;
    public Socket socket;
    public PrintWriter pw;
    public static String message = "0=0";
    public int numMessages = 0;
    public Rect templateRect = new Rect();
    public Rect searchRect = new Rect();
    public Mat template;
    public boolean startTimer = false;
    public long timer;
    public long startTime;
    public boolean newBeat = true;
    Timer tmr = new Timer();
    public int score;
    int nextType = -1;
    boolean finished = false;

    List<Square> squares = new ArrayList<>();


    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(BattleActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public BattleActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.battle_activity);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(1);
        mOpenCvCameraView.enableView();
        frameSize = new Size(1280,720);
        mOpenCvCameraView.setMaxFrameSize((int)frameSize.width, (int)frameSize.height);

        //CraftSystem craftSystem = new CraftSystem();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        try {
            pw.close();
            socket.close();
        } catch (Exception e){

        }
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        reduced = new Mat();
        previousFrame = new Mat(height, width, CvType.CV_8UC1);
        previousDiffFrame = new Mat(height, width, CvType.CV_8UC1);
        combinedFrame = new Mat(height, width, CvType.CV_8UC1);
        decoder = new QRCodeDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,0,0,255);
        template = new Mat();
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        startTimer = true;

        return false;
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.gray();
        Core.flip(mRgba, mRgba, 1);
        try {
            processFrame(mRgba);
        } catch (Exception e) {

        }
        return mRgba;
    }

    private void processFrame(Mat src) {
        tmr.update();
        if (frameCount == 0) {
            src.copyTo(previousFrame);
            frameCount++;
            return;
        }
        int width = src.width();
        int height = src.height();

        Mat diffFrame = new Mat(height, width, CvType.CV_8UC1);;
        Core.subtract(src,previousFrame,diffFrame);
        src.copyTo(previousFrame);

        Imgproc.threshold(diffFrame, diffFrame, 20, 255, Imgproc.THRESH_BINARY);
        Imgproc.dilate(diffFrame, diffFrame, new Mat(), new Point(-1, -1), 5);
        Scalar sum = Core.sumElems(diffFrame);
        int s = (int)(sum.val[0] / (width * height));

        if (frameCount == 1) {
            diffFrame.copyTo(previousDiffFrame);
            frameCount++;
            return;
        }

        if (s > 10) {
            diffFrame.copyTo(src);
            diffFrame.copyTo(previousDiffFrame);
        } else {
            previousDiffFrame.copyTo(src);
        }

        //Mask top
        int b = 50;
        Imgproc.rectangle(src, new Point(200 - b, height / 2 - b), new Point(200 + b, 0), new Scalar(0, 0, 0), -1);
        Imgproc.rectangle(src, new Point(200 + b, height), new Point(200 - b, height / 2 + b), new Scalar(0, 0, 0), -1);

        if (!startTimer) {
            Imgproc.line(src, new Point(200, 0), new Point(200, 720), new Scalar(255, 255, 255), 10);
            Imgproc.line(src, new Point(0, 360), new Point(1280, 360), new Scalar(255, 255, 255), 5);
        }

        if (startTimer && tmr.check()) {
            Square square = new Square();
            if (nextType == 1) {
                nextType = -1;
                square.type = 1;
            } else {
                double rnd = Math.random();
                if (rnd > 0.4) {      //bad
                    square.type = 1;
                } else {    //good
                    double choose023 = Math.random();
                    if (choose023 < 0.5) {
                        square.type = 0;
                    }
                    if (squares.size() >= 1 && squares.get(squares.size()-1).type == 1) {
                        if (choose023 >= 0.5 && choose023 < 0.75) {
                            nextType = 1;
                            square.type = 2;
                        }
                    }
                    if (choose023 >= 0.75) {
                        square.type = 3;
                    }
                }
            }
            squares.add(square);
        }

        if (startTimer) {
            for (int i=0; i<squares.size(); i++) {
                Square square = squares.get(i);
                square.update(tmr.dt / 1000f,src);
                if (square.checkIfLeftScreen()) {
                    if (square.type == 0 && square.state == 1) {
                        score++;
                    } else if (square.type == 1 && square.state == 1){
                        score-=4;
                    } else if (square.type == 2 && square.state == 1) {
                        score+=2;
                    } else if (square.type == 3 && square.state == 1) {
                        score++;
                    } else if (square.type == 3 && square.state == 0) {
                        score-=2;
                    }

                    squares.remove(i);
                    i--;
                    continue;
                }
                square.render(src);
            }

            Imgproc.line(src, new Point(200, 0), new Point(200, 720), new Scalar(255, 255, 255), 10);
            Imgproc.line(src, new Point(0, 360), new Point(1280, 360), new Scalar(255, 255, 255), 5);
            if (score < CraftSystem.winScore && !finished) {
                drawText(score + "", 300, 150, src);
            } else {
                finished = true;
                drawText("WIN", 300, 150, src);
                CraftSystem.lastBattleStatus = 1;
            }
        }

        frameCount++;
    }

    class Square{

        public int type = 0; //0 - good, 1 - bad, 2 - between good, 3 - mandatory
        public int state = 0; //0 - move right, 1 - move up

        public int x = 200;
        public int y = 720;

        public int b = 50;

        public float speed = 300 / CraftSystem.speedMultiplier;
        public float leaveSpeed = 400;

        public void update(float dt, Mat src) {
            if (state == 0) {
                // Log.e("Square", dt + " " + (int)(speed * dt) + " ");
                y -= (int)(speed * dt);
            }
            if (state == 1) {
                x -= (int)(leaveSpeed * dt);
            }
            if (state == 0 && checkCollision(src)) {
                state = 1;
            }
        }

        public boolean checkCollision(Mat src) {
            int w = src.width();
            int h = src.height();
            int thres = 5;
            if (y > h/2 - b && y < h/2 + b) {
                Mat hitbox = src.submat(y - b, y + b, x - b, x + b);
                Scalar sum = Core.sumElems(hitbox);
                int s = (int)(sum.val[0] / (2*b * 2*b));
                if (s > 5) {
                    return true;
                }
            }
            return false;
        }

        public void render(Mat src) {
            if (type == 0) {
                Imgproc.line(src, new Point(x - b, y - b), new Point(x + b, y - b), new Scalar(255, 255, 255), 10);
                Imgproc.line(src, new Point(x + b, y - b), new Point(x + b, y + b), new Scalar(255, 255, 255), 10);
                Imgproc.line(src, new Point(x + b, y + b), new Point(x - b, y + b), new Scalar(255, 255, 255), 10);
                Imgproc.line(src, new Point(x - b, y + b), new Point(x - b, y - b), new Scalar(255, 255, 255), 10);
            }
            if (type == 1) {
                Imgproc.line(src, new Point(x - b, y - b), new Point(x + b, y - b), new Scalar(255, 0, 0), 10);
                Imgproc.line(src, new Point(x + b, y - b), new Point(x + b, y + b), new Scalar(255, 0, 0), 10);
                Imgproc.line(src, new Point(x + b, y + b), new Point(x - b, y + b), new Scalar(255, 0, 0), 10);
                Imgproc.line(src, new Point(x - b, y + b), new Point(x - b, y - b), new Scalar(255, 0, 0), 10);

                Imgproc.line(src, new Point(x - b, y - b), new Point(x + b, y + b), new Scalar(255, 0, 0), 10);
                Imgproc.line(src, new Point(x + b, y - b), new Point(x - b, y + b), new Scalar(255, 0, 0), 10);
            }
            if (type == 2) {
                Imgproc.line(src, new Point(x - b, y - b), new Point(x + b, y - b), new Scalar(255, 255, 255), 10);
                Imgproc.line(src, new Point(x + b, y - b), new Point(x + b, y + b), new Scalar(255, 255, 255), 10);
                Imgproc.line(src, new Point(x + b, y + b), new Point(x - b, y + b), new Scalar(255, 255, 255), 10);
                Imgproc.line(src, new Point(x - b, y + b), new Point(x - b, y - b), new Scalar(255, 255, 255), 10);
                b = 30;
                Imgproc.line(src, new Point(x - b, y - b), new Point(x + b, y - b), new Scalar(255, 255, 255), 10);
                Imgproc.line(src, new Point(x + b, y - b), new Point(x + b, y + b), new Scalar(255, 255, 255), 10);
                Imgproc.line(src, new Point(x + b, y + b), new Point(x - b, y + b), new Scalar(255, 255, 255), 10);
                Imgproc.line(src, new Point(x - b, y + b), new Point(x - b, y - b), new Scalar(255, 255, 255), 10);
                b = 50;
            }
            if (type == 3) {
                Imgproc.line(src, new Point(x - b, y - b), new Point(x + b, y - b), new Scalar(255, 255, 255), 10);
                Imgproc.line(src, new Point(x + b, y - b), new Point(x + b, y + b), new Scalar(255, 255, 255), 10);
                Imgproc.line(src, new Point(x + b, y + b), new Point(x - b, y + b), new Scalar(255, 255, 255), 10);
                Imgproc.line(src, new Point(x - b, y + b), new Point(x - b, y - b), new Scalar(255, 255, 255), 10);

                Imgproc.line(src, new Point(x - b, y - b / 2), new Point(x + b, y - b / 2), new Scalar(255, 255, 255), 10);
                Imgproc.line(src, new Point(x - b, y ), new Point(x + b, y ), new Scalar(255, 255, 255), 10);
                Imgproc.line(src, new Point(x - b, y + b / 2), new Point(x + b, y + b / 2), new Scalar(255, 255, 255), 10);
            }
        }

        public boolean checkIfLeftScreen() {
            if (x < -b || y < -b) {
                return true;
            }
            return false;
        }
    }

    public void drawText(String text, int x, int y, Mat src) {
        Point position = new Point(x, y);
        Scalar color = new Scalar(255, 255, 255);
        int font = Imgproc.FONT_HERSHEY_SIMPLEX;
        int scale = 5;
        int thickness = 15;
        Imgproc.putText(src, text, position, font, scale, color, thickness);
    }

    class Timer {
        public long time = 0;
        public long previousTime = 0;
        public long dt = 0;
        public long circle = (long) ( 800 * CraftSystem.speedMultiplier);
        public long markTime = 0;
        public void update() {
            if (previousTime == 0) {
                previousTime = System.currentTimeMillis();
                markTime = System.currentTimeMillis();
            }
            time = System.currentTimeMillis();
            dt = time - previousTime;
            previousTime = time;
        }
        public void mark(){
            markTime = System.currentTimeMillis();
        }
        public boolean check() {

            if (time - markTime > circle) {
                markTime = time;
                return true;
            }
            return false;
        }
    }
}





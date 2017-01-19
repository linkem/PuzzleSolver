package com.example.mat.puzzlesolver;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.*;
import org.opencv.android.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.approxPolyDP;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.contourArea;

public class ExtractingPiecesActivity extends Activity {

    ImageView iv0,iv1,iv2,iv3, iv4,iv5;
    Bitmap demoPuzzle, demoPhoto, previousPhoto, step0, step1, step2, step3,step4;
    int stepCounter =0;
    Context context;
    Mat mMat,mMat_Grey;
    Button btNext,btPrvious;
    boolean isDemo;
    private static final String TAG = "ExtractingPiecesAct";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extracting_pieces);
        iv0 = (ImageView) findViewById(R.id.ExtractingPiecesImageView);
        iv1 = (ImageView) findViewById(R.id.ExtractingPiecesImageView1);
        iv2 = (ImageView) findViewById(R.id.ExtractingPiecesImageView2);
        iv3 = (ImageView) findViewById(R.id.ExtractingPiecesImageView3);
        iv4 = (ImageView) findViewById(R.id.ExtractingPiecesImageView4);
        iv5 = (ImageView) findViewById(R.id.ExtractingPiecesImageView5);

        btNext = (Button) findViewById(R.id.btExtractingPiecesNext);
        btPrvious = (Button) findViewById(R.id.btExtractingPiecesPrevoius);

        context = getApplicationContext();
        isDemo = getIntent().getExtras().getBoolean("isDemo");
        if(isDemo){
            demoPuzzle = BitmapFactory.decodeResource(getResources(), R.drawable.puzzles);
            demoPhoto = BitmapFactory.decodeResource(getResources(), R.drawable.photo);
            iv0.setImageBitmap(demoPuzzle);
        }

        btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //stepCounter++;
                switch (stepCounter++){
                    case 0:
                    {
                        my_threshold();
                    }
                    case 1:{
                        Log.d("przycisk next", "case 1");
                    }
                    case 2:{
                        Log.d("przycisk next", "case 2");
                    }
                    default:{
                        stepCounter=0;
                        btNext.setEnabled(false);
                    }
                }

            }
        });
        btPrvious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previous();
                Log.d("previous bt", "klik");
            }
        });
    }
    public void my_threshold(){
        //***INIT
        demoPuzzle = BitmapFactory.decodeResource(getResources(), R.drawable.puzzles);
        step0 = BitmapFactory.decodeResource(getResources(), R.drawable.puzzles);
        step1 = BitmapFactory.decodeResource(getResources(), R.drawable.puzzles);
        step2 = BitmapFactory.decodeResource(getResources(), R.drawable.puzzles);
        step3 = BitmapFactory.decodeResource(getResources(), R.drawable.puzzles);
        step4 = BitmapFactory.decodeResource(getResources(), R.drawable.puzzles);

        Mat matDemoPuzzles, matDemoPuzzles_grey,matDemoPuzzles_mask,tempMat;
        matDemoPuzzles = new Mat();
        matDemoPuzzles_grey = new Mat();
        matDemoPuzzles_mask = new Mat();
        //****GREY SCALE****
        Utils.bitmapToMat(demoPuzzle,matDemoPuzzles);
        tempMat = matDemoPuzzles.clone();
        Imgproc.cvtColor(matDemoPuzzles,matDemoPuzzles_grey,Imgproc.COLOR_BGR2GRAY);
        Utils.matToBitmap(matDemoPuzzles_grey,step0);

        iv1.setImageBitmap(step0);
        //*****MASK****
        Imgproc.threshold(matDemoPuzzles_grey, matDemoPuzzles_mask, 230,3,4);
        Utils.matToBitmap(matDemoPuzzles_mask,step1);

        iv2.setImageBitmap(step1);
        //****CONTOURS ON MASK****
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy   = new Mat(matDemoPuzzles_mask.rows(), matDemoPuzzles_mask.cols(), CvType.CV_8UC1, new Scalar(0));
        Point point = new Point(0,0);

        Imgproc.findContours(matDemoPuzzles_mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, point);
        List<MatOfPoint> bigContours = new ArrayList<MatOfPoint>();
        for( int i = 0; i< contours.size(); i++ ) // iterate through each contour.
        {
            double a = contourArea(contours.get(i), false);  //  Find the area of contour
            if (a > 15)
                bigContours.add(contours.get(i));
        }
        Imgproc.drawContours(matDemoPuzzles_mask, bigContours, -1, new Scalar(250,0) ,-1);
        Utils.matToBitmap(matDemoPuzzles_mask, step2);

        iv3.setImageBitmap(step2);
        //****CONTOURS ON IMAGE****
        Imgproc.drawContours(matDemoPuzzles, bigContours, -1, new Scalar(250,0) ,5);
        Utils.matToBitmap(matDemoPuzzles, step3,false);
        iv4.setImageBitmap(step3);
        Log.d(TAG, " rozmiar contures "+bigContours.size());
        Toast.makeText(context, "Znaleziono "+bigContours.size()+" elementów", Toast.LENGTH_SHORT).show();
        //***** CROP PUZZLES************
        List<Mat> listOfPuzzles = new ArrayList<Mat>(6);
        for(int i=0;i<bigContours.size();i++){
            MatOfPoint2f mMOP2F =  new MatOfPoint2f(bigContours.get(i).toArray());
            bigContours.get(i).convertTo(mMOP2F, CvType.CV_32FC2);
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            Imgproc.approxPolyDP(mMOP2F, approxCurve, 3, true);
            listOfPuzzles.add(tempMat.submat(boundingRect(bigContours.get(i))));
        }
        step4 = Bitmap.createScaledBitmap(step4, listOfPuzzles.get(2).cols(), listOfPuzzles.get(2).rows(), false);
        Utils.matToBitmap(listOfPuzzles.get(2), step4);
        iv5.setImageBitmap(step4);
        //*******************************

    }
    public void previous(){
       // TODO: zaimplementowac przycisk
    }
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV Manager Connected");
                    break;
                case LoaderCallbackInterface.INIT_FAILED:
                    Log.i(TAG, "Init Failed");
                    break;
                case LoaderCallbackInterface.INSTALL_CANCELED:
                    Log.i(TAG, "Install Cancelled");
                    break;
                case LoaderCallbackInterface.INCOMPATIBLE_MANAGER_VERSION:
                    Log.i(TAG, "Incompatible Version");
                    break;
                case LoaderCallbackInterface.MARKET_ERROR:
                    Log.i(TAG, "Market Error");
                    break;
                default:
                    Log.i(TAG, "OpenCV Manager Install");
                    super.onManagerConnected(status);
                    break;
            }
        }
    };
    protected void onResume() {
        super.onResume();
        //initialize OpenCV manager
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_13, this, mLoaderCallback);
    }
}
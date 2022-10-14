package com.example.facedetect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    //Ui views
    private ImageView originalImageIv;
    private ImageView croppedImageIv;
    private Button detectButtonBtn;

    //Tag fot debugging
    private static final String TAG = "FACE_DETECT_TAG";

    //factor process to make image smaller & process image faster
    private static final int SCALING_FACTOR = 10;

    private FaceDetector detector;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init  ui Views
        originalImageIv = findViewById(R.id.originalImageIv);
        croppedImageIv = findViewById(R.id.croppedImageIv);
        detectButtonBtn = findViewById(R.id.detectFacesBtn);

        FaceDetectorOptions realTimeFdo =
                new FaceDetectorOptions.Builder()
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .build();



        //init FaceDetector
        detector = FaceDetection.getClient(realTimeFdo);



        //handling onclick activities
        detectButtonBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //obtaining bitmaps from All posible ways is here

                //Bitmap from drawwable
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.c03);

                //Bitmap from uri incase you want pick image picker in gallary
                /*
                Uri imageUri = null;
                try {
                    Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(),imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                 */

                //Bitmap  from ImageView ,incase your image is in from URL/web
                /* BitmapDrawable bitmapDrawable = (BitmapDrawable) originalImageIv.getDrawable();
                Bitmap bitmap1 = bitmapDrawable.getBitmap();

                 */

                analyzePhoto(bitmap);









            }
        });



    }

    private void analyzePhoto(Bitmap bitmap){
        Log.d(TAG, "analyzePhoto: ");

        Bitmap smallerBitmap = Bitmap.createScaledBitmap(
                bitmap,
                bitmap.getWidth() / SCALING_FACTOR,
                bitmap.getHeight() / SCALING_FACTOR,
                false);
        InputImage inputImage = InputImage.fromBitmap(smallerBitmap,0);

        //start detention process
        detector.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                    @Override
                    public void onSuccess(List<Face> faces) {
                        //possible to have multiple faces detected we managethem throuh loop
                        Log.d(TAG, "onSuccess: No of faces detected: "+faces.size());
                        for (Face face: faces){
                            //Get detected as rectangle
                            Rect rect = face.getBoundingBox();
                            rect.set(rect.left+SCALING_FACTOR,rect.top+(SCALING_FACTOR-1),
                                    rect.right*SCALING_FACTOR,
                                    rect.bottom+SCALING_FACTOR + 90
                            );

                        }
                        cropDetectedFaces(bitmap, faces);


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Detention failed
                        Log.e(TAG,"onFailure: ", e);
                        Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();

                    }
                });



    }

    private void cropDetectedFaces(Bitmap bitmap, List<Face> faces) {
        Log.d(TAG, "cropDetectedFaces: ");
        //Detected faces will be croppd
        //there can be multiple faces they will be handled using loop

        Rect rect = faces.get(0).getBoundingBox();
        int x = Math.max(rect.left, 0);
        int y = Math.max(rect.top, 0);
        int width = rect.width();
        int height = rect.height();

        Bitmap croppedBitmap =  Bitmap.createBitmap(
                bitmap,
                x,
                y,
                (x + width > bitmap.getWidth()) ? (bitmap.getWidth() - x) : width,
                (y + height > bitmap.getHeight()) ? bitmap.getHeight() -y : height
        );

        //set the cropped bitmap to mage view
        croppedImageIv.setImageBitmap(croppedBitmap);
    }
}
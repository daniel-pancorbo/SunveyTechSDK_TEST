package com.rearcam.aicontrol.ai;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import com.rearcam.receive.R;

import java.util.List;

public class FrameProcessor {
    private static final ObjectDetectorOptions options =
            new ObjectDetectorOptions.Builder()
                    .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
                    .enableMultipleObjects()
                    .enableClassification()  // Optional
                    .build();
    private static final ObjectDetector objectDetector = ObjectDetection.getClient(options);
    private volatile boolean busy = false;
    private List<DetectedObject> detectedObjectsList;
    private TextView frameElapsedTimeView;
    private TextView detectionObjectsView;
    CanvasHandler CanvasHandler = new CanvasHandler();
    public void detect(Bitmap bmp, Activity activity){
        if(busy){
            Log.d("ML-check", "frame skipped");
            return;
        }else{
            Log.d("ML-check", "processing frame");
            busy = true;

            frameElapsedTimeView = activity.findViewById(R.id.textViewFrame);
            detectionObjectsView = activity.findViewById(R.id.textViewDetectionNumber);
            long startMs = SystemClock.elapsedRealtime();
            InputImage image = InputImage.fromBitmap(bmp, 0);

            objectDetector.process(image)
                    .addOnSuccessListener(
                            new OnSuccessListener<List<DetectedObject>>() {
                                @Override
                                public void onSuccess(List<DetectedObject> detectedObjects) {
                                    // The list of detected objects contains one item if multiple
                                    // object detection wasn't enabled.
                                    Log.d("ML-Success", "successful object detection");
                                    Log.d("ML-check", "frame processed");
                                    detectedObjectsList = detectedObjects;
                                    CanvasHandler.draw(bmp, activity, detectedObjectsList);
                                    if (detectedObjects.size() >= 1) {
                                        Log.d("ML-Detected", String.valueOf(detectedObjects));
                                    }

                                    busy = false;
                                    long endMs = SystemClock.elapsedRealtime();
                                    long frameElapsedTime = endMs - startMs;
                                    Log.d("ML-frameTime", String.valueOf(frameElapsedTime));
                                    frameElapsedTimeView.setText("Frame elapsed time in ms: "+ frameElapsedTime);
                                    detectionObjectsView.setText("Number of detected objects: " + detectedObjects.size() );
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("ML-Error", "Failed object detection");
                                }
                            });
        }
    }
}

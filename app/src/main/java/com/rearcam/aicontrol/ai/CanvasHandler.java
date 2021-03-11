package com.rearcam.aicontrol.ai;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.widget.ImageView;

import com.google.mlkit.vision.objects.DetectedObject;
import com.rearcam.receive.R;

import java.util.List;

public class CanvasHandler {
    private ImageView canvasView;

    public void draw(Bitmap bmp, Activity activity, List<DetectedObject> detectedObjectsList){
        canvasView = activity.findViewById(R.id.canvasView);
        // Initialize a new Bitmap object
        Bitmap bitmap = Bitmap.createBitmap(
                bmp.getWidth(), // Width
                bmp.getHeight(), // Height
                Bitmap.Config.ARGB_8888 // Config
        );
        // Initialize a new Canvas instance
        Canvas canvas = new Canvas(bitmap);

        // Draw a solid color to the canvas background
        canvas.drawColor(Color.TRANSPARENT);

        for (DetectedObject detectedObject : detectedObjectsList) {
            Rect boundingBox = detectedObject.getBoundingBox();
            // Initialize a new Paint instance to draw the Rectangle
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2); // set stroke width
            paint.setColor(Color.RED);
            paint.setAntiAlias(true);
            //draw the rectangle on the canvas
            canvas.drawRect(boundingBox,paint);
            Log.d("ML-Box", String.valueOf(boundingBox));
            Integer trackingId = detectedObject.getTrackingId();
            Log.d("ML-TrackingId", String.valueOf(trackingId));
            for (DetectedObject.Label label : detectedObject.getLabels()) {
                String text = label.getText();
                Log.d("ML-Label", text);
                float confidence = label.getConfidence();
                Log.d("ML-Confidence", String.valueOf(confidence));
                Paint paint2= new Paint();
                paint2.setColor(Color.GREEN);
                paint2.setTextSize(20);  //set text size
                canvas.drawText(text, boundingBox.left, boundingBox.top ,paint2);
            }
        }
        // Display the newly created bitmap on app interface
        canvasView.setImageBitmap(bitmap);
    }
}
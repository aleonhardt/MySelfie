package com.example.myselfie;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.view.SurfaceHolder;
import android.util.AttributeSet;
import android.util.Log;

import android.view.View;

public class FaceRectView extends View {

    private static final String TAG = "facedetection";
    Paint paint = new Paint();
    List<Camera.Face> faces = new ArrayList<Camera.Face>();
    Matrix matrix = new Matrix();
    RectF rect = new RectF();
    private int mDisplayOrientation;
    private int mOrientation;
    private int coord =20;
    private int mCameraId;
    /**
     * @param context
     */
    public FaceRectView(Context context) {
        super(context);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);

    }

    private void dumpRect(RectF rect, String msg) {
        Log.v(TAG, msg + "=(" + rect.left + "," + rect.top
                + "," + rect.right + "," + rect.bottom + ")");
    }

    @Override
    public void onDraw(Canvas canvas) {
      // canvas.drawRect(coord, coord, 2*coord, 2*coord, paint);
      //coord = coord+10;
      //canvas.drawARGB(0, 0, 0, 0);
       

        prepareMatrix(matrix, mDisplayOrientation, getWidth(), getHeight(), mCameraId);
      
        Log.d(TAG, "Drawing Faces - " + faces.size());
        for (Face face : faces) {
        	
        	//POR QUE NAO FUNCIONA?
            rect.set(face.rect);
            dumpRect(rect, "before");
            matrix.mapRect(rect);
            dumpRect(rect, "after");
            canvas.drawRect(rect.left, rect.top, rect.right, rect.bottom, paint);
            
            
        }
        //canvas.restore();
        
        super.onDraw(canvas);
    }

    public void setDisplayOrientation(int orientation) {
        mDisplayOrientation = orientation;
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
        invalidate();
    }

    /**
     * @param asList
     */
    public void setFaces(List<Camera.Face> faces) {
        this.faces = faces;
        invalidate();
    }

    public FaceRectView(Context context, AttributeSet attr) {
        super(context, attr);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(2f);
        paint.setStyle(Paint.Style.STROKE);
       // paint.setAntiAlias(true);
    }

    public static void prepareMatrix(Matrix matrix, int displayOrientation,
            int viewWidth, int viewHeight, int cameraId) {
    	CameraInfo info = new CameraInfo();;
    	Camera.getCameraInfo(cameraId, info);
    	 // Need mirror for front camera.
    	 boolean mirror = (info.facing == CameraInfo.CAMERA_FACING_FRONT);
    	//boolean mirror = false;
    	 matrix.setScale(mirror ? -1 : 1, 1);
        matrix.postRotate(displayOrientation);
        // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
        // UI coordinates range from (0, 0) to (width, height).
        matrix.postScale(viewWidth / 2000f, viewHeight / 2000f);
        matrix.postTranslate(viewWidth / 2f, viewHeight / 2f);

    }
    
    public void setCameraId(int camera){
    	mCameraId = camera;
    }

}
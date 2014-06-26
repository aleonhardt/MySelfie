package com.example.myselfie;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, FaceDetectionListener {
	
		public static final int K_STATE_PREVIEW = 0;
		public static final int K_STATE_FROZEN = 1;
		
		private boolean isRunningFaceDetection = false;
		public int minFacesToDetect = 0;
		
		public int ROTATION = 0;
	
		private Camera mCamera = null;
		private Face[] mFaces = null;

		//Matrix matrix = new Matrix();
	    RectF rectF = new RectF();
	    public static int mDisplayOrientation;
	    
	    public static int MAX_FACES;
	    
	    
	    SurfaceHolder mHolder;
	    int mPreviewState = K_STATE_PREVIEW;
	    
	    boolean isPreviewRunning = false;
		private FaceRectView mFaceRect;
		private Context mContext;
		
		private int mCameraId;
		
		Date lastRingCheckDate = new Date();

	    //Size mPreviewSize;
	    //List<Size> mSupportedPreviewSizes;
	    

		CameraPreview(Context context, Camera camera) {
	        super(context);
	
	        mCamera = camera;
	       
	        // Install a SurfaceHolder.Callback so we get notified when the
	        // underlying surface is created and destroyed.
	        mHolder = getHolder();
	        mHolder.addCallback(this);
	        
	        mContext = context;
	      
	    }
	    

	    public CameraPreview(Context context, AttributeSet attr) {
	        super(context, attr);
	       
	        mContext = context;
	        // Install a SurfaceHolder.Callback so we get notified when the
	        // underlying surface is created and destroyed.
	        mHolder =getHolder();
	        mHolder.addCallback(this);
	        
	    }

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			
			
			if (isPreviewRunning)
	        {
	            mCamera.stopPreview();
	            stopFaceDetection(mCamera);
	            
	        }
			
			previewCamera();
			
		
			
		}
		
		public void previewCamera()
		{        
		    try 
		    {           
		        mCamera.setPreviewDisplay(mHolder);          
		        mCamera.startPreview();
		        startFaceDetection(mCamera);
		        isPreviewRunning = true;
		       
		    }
		    catch(Exception e)
		    {
		        Log.d("CAMERA PREVIEW", "Cannot start preview", e);    
		    }
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// The Surface has been created, acquire the camera and tell it where
	        // to draw.
	        try {
	            if (mCamera != null) {
	                mCamera.setPreviewDisplay(holder);
	                Camera.Parameters parameters = mCamera.getParameters();
	    	        parameters.setPreviewSize(getWidth(), getHeight());
	    	        parameters.setSceneMode(Camera.Parameters.SCENE_MODE_PORTRAIT);
	    	        
	    	        Activity myActivity = (Activity)getContext();
	    	        
	    	        SeekBar sb = (SeekBar)myActivity.findViewById(R.id.seekbar_faces);
	    	        
	    	        switch(parameters.getMaxNumDetectedFaces()){
	    	        case 0: Toast.makeText(getContext(), R.string.no_facedetection_support, Toast.LENGTH_LONG).show();
	    	        		CameraPreview.MAX_FACES = 0;
	    	        		break;
	    	        case 1:	sb.setVisibility(View.GONE);
	    	        		CameraPreview.MAX_FACES = 1;
	    	        		break;
	    	        default: sb.setMax(parameters.getMaxNumDetectedFaces());
	    	        		CameraPreview.MAX_FACES = parameters.getMaxNumDetectedFaces();
	    	        }
	    	        
	    	        startFaceDetection(mCamera);
	                //mCamera.setFaceDetectionListener(this);
	            }
	        } catch (IOException exception) {
	            Log.e("CameraPreview", "IOException caused by setPreviewDisplay()", exception);
	        }
			
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder arg0) {
			// Surface will be destroyed when we return, so stop the preview.
		    if (mCamera != null) {
		        // Call stopPreview() to stop updating the preview surface.
		    	mCamera.stopPreview();
		    	stopFaceDetection(mCamera);
		    }
			
		}
	
		public void setCamera(Camera camera) {
		    if (mCamera == camera) { return; }
		    
		    stopPreviewAndFreeCamera();
		    
		    mCamera = camera;
		    
		    if (mCamera != null) {
		        //List<Size> localSizes = mCamera.getParameters().getSupportedPreviewSizes();
		        //mSupportedPreviewSizes = localSizes;
		        requestLayout();
		        
		        try {
		        	mCamera.setPreviewDisplay(mHolder);
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		        mCamera.setFaceDetectionListener(this);
		        // Important: Call startPreview() to start updating the preview
		        // surface. Preview must be started before you can take a picture.
		        mCamera.startPreview();
		    }
		}
		
		private void stopPreviewAndFreeCamera() {

		    if (mCamera != null) {
		        // Call stopPreview() to stop updating the preview surface.
		    	mCamera.stopPreview();
		    	stopFaceDetection(mCamera);
		    
		        // Important: Call release() to release the camera for use by other
		        // applications. Applications should release the camera immediately
		        // during onPause() and re-open() it during onResume()).
		    	mCamera.release();
		    
		    	mCamera = null;
		    }
		}
		
		public void onClick(View v) {
		    switch(mPreviewState) {
		    case K_STATE_FROZEN:
		        mCamera.startPreview();
		        mPreviewState = K_STATE_PREVIEW;
		        break;

		    default:
		        mCamera.takePicture( null, null, null);
		        /**TODO: IMPLEMENT THIS STATE. When callback from one of the picture types has returned, than we use K_STATE_FROZEN**/
		        //mPreviewState = K_STATE_BUSY;
		        mPreviewState = K_STATE_FROZEN;
		    } // switch
		    //shutterBtnConfig();
		}
		
		public void switchCamera(Camera camera) {
	        setCamera(camera);
	        try {
	            camera.setPreviewDisplay(mHolder);
	        } catch (IOException exception) {
	            Log.e("CameraPreview", "IOException caused by setPreviewDisplay()", exception);
	        }
	        mCamera.stopPreview();
	        stopFaceDetection(mCamera);
	        Camera.Parameters parameters = camera.getParameters();
	        parameters.setPreviewSize(getWidth(), getHeight());
	        parameters.setSceneMode(Camera.Parameters.SCENE_MODE_PORTRAIT);
	        requestLayout();

	        camera.setParameters(parameters);
	        mCamera.startPreview();
	    }

		
		 public static void setCameraDisplayOrientation(Activity activity,
		         int cameraId, android.hardware.Camera camera) {
		     android.hardware.Camera.CameraInfo info =
		             new android.hardware.Camera.CameraInfo();
		     android.hardware.Camera.getCameraInfo(cameraId, info);
		     int rotation = activity.getWindowManager().getDefaultDisplay()
		             .getRotation();
		     int degrees = 0;
		     switch (rotation) {
		         case Surface.ROTATION_0: degrees = 0; break;
		         case Surface.ROTATION_90: degrees = 90; break;
		         case Surface.ROTATION_180: degrees = 180; break;
		         case Surface.ROTATION_270: degrees = 270; break;
		     }

		     int result;
		     if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
		         result = (info.orientation + degrees) % 360;
		         result = (360 - result) % 360;  // compensate the mirror
		     } else {  // back-facing
		         result = (info.orientation - degrees + 360) % 360;
		     }
		     camera.setDisplayOrientation(result);
		     mDisplayOrientation = result;
		     FaceRectView view = ((FaceRectView)activity.findViewById(R.id.face_view));
			 view.setDisplayOrientation(result);
		 }

		 
		 public void setCameraID(int id){
			 mCameraId = id;
		 }

		@Override
		public void onFaceDetection(Face[] faces, Camera camera) {
			
				mFaces = faces;
				FaceRectView view = ((FaceRectView)(((Activity)getContext()).findViewById(R.id.face_view)));
		        view.setFaces(Arrays.asList(faces));
		        view.setCameraId(mCameraId);
		        
		        if(faces.length > 0){
		        	setFocusOnFaces();
		        }
			
			
		}
		
		private void setFocusOnFaces(){
			
			
			//Log.i("setFocusOnFaces", "Setting focus on " + mFaces.length + " faces.");
			stopFaceDetection(mCamera);
			Camera.Parameters params = mCamera.getParameters();

			int maxFocusAreas = params.getMaxNumFocusAreas();
			//Log.i("setFocusOnFaces", "Max focus areas " + maxFocusAreas);
			
			if (maxFocusAreas > 0){ // check that metering areas are supported
			    List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();

			    //FaceRectView.prepareMatrix(matrix, mDisplayOrientation, getWidth(), getHeight(), mCameraId);
			      
		        //Log.d(TAG, "Drawing Faces - " + faces.size());
		        for (Face face : mFaces) {
		        	if(maxFocusAreas == 0)
		        		continue;
		            rectF.set(face.rect);
		            
		            //Log.i("setFocusOnFaces",rectF.toShortString());
		            //matrix.mapRect(rectF);
		            
		            //Log.i("setFocusOnFaces",rectF.toShortString());
		            
		            Rect rect = new Rect();
		            rect.bottom = (int)rectF.bottom;
		            rect.left = (int)rectF.left;
		            rect.top = (int)rectF.top;
		            rect.right = (int)rectF.right;
		            
		            //Log.i("setFocusOnFaces",rect.toShortString());
		            
		            focusAreas.add(new Camera.Area(rect, 500)); // set weight to 60%
		            maxFocusAreas--;
		            
		            
		        }
		        params.setFocusAreas(focusAreas);
			}
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			mCamera.setParameters(params);
			mCamera.autoFocus(new Camera.AutoFocusCallback() {
				
				@Override
				public void onAutoFocus(boolean success, Camera camera) {
					if(success){
						
						Calendar last = Calendar.getInstance();       
		    	    	Calendar now = Calendar.getInstance();              
		    	    	last.setTime(lastRingCheckDate);       
		    	    	now.setTime(new Date());              
		    	    	long diff = now.getTimeInMillis() - last.getTimeInMillis();    
						
						Log.i("onAutoFocus", "Succesfully focused");
						if(mFaces.length >= minFacesToDetect){
							try {
								//Only rings again if the last ring was more than a second ago
								if(diff>1000){
								    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
								    Ringtone r = RingtoneManager.getRingtone(mContext, notification);
								    r.play();
								}
							} catch (Exception e) {
							    e.printStackTrace();
							}
						}
						lastRingCheckDate = now.getTime();
					}else{
						Log.i("onAutoFocus", "Focus failed");
					}
					startFaceDetection(camera);
				}
			});
			
			
		}
		
		private synchronized void startFaceDetection(Camera camera){
			if(!isRunningFaceDetection){
				camera.startFaceDetection();
				isRunningFaceDetection = true;
			}
		}
		
		private synchronized void stopFaceDetection(Camera camera){
			if(isRunningFaceDetection){
				camera.stopFaceDetection();
				isRunningFaceDetection = false;
			}
		}
		
		
		
}

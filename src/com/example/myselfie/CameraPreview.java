package com.example.myselfie;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, FaceDetectionListener {
	
		public static final int K_STATE_PREVIEW = 0;
		public static final int K_STATE_FROZEN = 1;
		
		public int ROTATION = 0;
	
		private Camera mCamera = null;


	    SurfaceHolder mHolder;
	    int mPreviewState = K_STATE_PREVIEW;
	    
	    boolean isPreviewRunning = false;
		private FaceRectView mFaceRect;

	    //Size mPreviewSize;
	    //List<Size> mSupportedPreviewSizes;
	    

		CameraPreview(Context context, Camera camera) {
	        super(context);
	
	        mCamera = camera;
	        // Install a SurfaceHolder.Callback so we get notified when the
	        // underlying surface is created and destroyed.
	        mHolder = getHolder();
	        mHolder.addCallback(this);
	        
	        
	      
	    }
	    

	    public CameraPreview(Context context, AttributeSet attr) {
	        super(context, attr);
	       

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
	        }
			previewCamera();
			
		
			
		}
		
		public void previewCamera()
		{        
		    try 
		    {           
		        mCamera.setPreviewDisplay(mHolder);          
		        mCamera.startPreview();
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
		        mCamera.startFaceDetection();
		    }
		}
		
		private void stopPreviewAndFreeCamera() {

		    if (mCamera != null) {
		        // Call stopPreview() to stop updating the preview surface.
		    	mCamera.stopPreview();
		    
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
		 }


		@Override
		public void onFaceDetection(Face[] faces, Camera arg1) {
			

				FaceRectView view = ((FaceRectView)(((Activity)getContext()).findViewById(R.id.face_view)));
		        view.setFaces(Arrays.asList(faces));
			
			
		}
}

package com.example.myselfie;


import android.hardware.Camera;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

	public Camera mCamera;
	private CameraPreview mPreview;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     // Hide the window title.
      //  requestWindowFeature(Window.FEATURE_NO_TITLE);
       // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //    WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Create a RelativeLayout container that will hold a SurfaceView,
        // and set it as the content of our activity.
        setContentView(R.layout.activity_main);
        openBackCamera();
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    
    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;
      
        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(id);
            CameraPreview.setCameraDisplayOrientation((Activity) this, id, mCamera);
            qOpened = (mCamera != null);
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

        return qOpened;    
    }

    private void releaseCameraAndPreview() {
        mPreview.setCamera(null);
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	//mPreview = new CameraPreview(this.getApplicationContext());
       
        openBackCamera();
        
        mPreview.setCamera(mCamera);
    }
    
    private void openBackCamera()
    {
    	 Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
    	 int nCameras = Camera.getNumberOfCameras();
    	 Toast.makeText(this.getApplicationContext(), "Number of cameras: "+nCameras, Toast.LENGTH_LONG).show();
    	 
    	 for(int id=0; id<nCameras; id++)
    	 {
    		 Camera.getCameraInfo(id, cameraInfo);
    	        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK)
    	        	safeCameraOpen(id);
    	 }
         
    }
    @Override
    protected void onPause() {
        super.onPause();

        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        if (mCamera != null) {
            mPreview.setCamera(null);
            mCamera.release();
            mCamera = null;
        }
    }
    
}

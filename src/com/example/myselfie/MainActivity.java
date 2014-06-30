package com.example.myselfie;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.ViewGroup.LayoutParams;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class MainActivity extends Activity implements MediaScannerConnectionClient {

	public Camera mCamera;
	private CameraPreview mPreview;

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;


	private static String appName = "MySelfie Pictures";
	private MediaScannerConnection scanner;
	private Context context = this;
	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			String TAG = "onPictureTaken";

			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			Bitmap pictureTaken = BitmapFactory.decodeByteArray(data, 0,data.length);

			//rotation
			Matrix matrix = new Matrix();
			matrix.postRotate(CameraPreview.mDisplayOrientation); // clockwise by 90 degrees

			// create a new bitmap from the original using the matrix to transform the result
			pictureTaken = Bitmap.createBitmap(pictureTaken , 0, 0, pictureTaken.getWidth(), pictureTaken.getHeight(), matrix, true);

			Uri contentUri = Uri.fromFile(pictureFile);

			OutputStream outputStream;
			try {
				outputStream = getContentResolver().openOutputStream(contentUri);
				boolean compressed = pictureTaken.compress(	Bitmap.CompressFormat.JPEG, 80, outputStream);
				Log.e(TAG, "picture successfully compressed at:" + pictureFile
						+ compressed);
				outputStream.close();
			} catch (FileNotFoundException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}

			if (pictureFile == null){
				Log.d(TAG, "Error creating media file, check storage permissions");
				return;
			}

			/*sends an intent so the media scanner adds the file to the library */
			Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

			mediaScanIntent.setData(contentUri);
			context.sendBroadcast(mediaScanIntent); 


			Log.i(TAG, "Picture taken! " + pictureFile.getPath());


			mCamera.startPreview();
			
		}
	};






	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type){
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File( Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), appName);
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (! mediaStorageDir.exists()){
			if (! mediaStorageDir.mkdirs()){
				Log.d(appName, "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.getDefault()).format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE){
			String filename = "IMG_"+ timeStamp;

			try {
				mediaFile = File.createTempFile( filename, ".jpg",mediaStorageDir);
			} catch (IOException e) {
				mediaFile=null;
				Log.d(appName, "failed to create the temp file");
			}

		} else {
			return null;
		}

		return mediaFile;
	}

	/* important to be on key down because this happens before. and thus before the volume keys are computed. */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			mCamera.stopFaceDetection();
			mCamera.takePicture(null, null, mPicture);
			Log.e("Main activity", "KEYCODE_VOLUME");
			return true;
		}
		else
			return super.onKeyDown(keyCode, event);
	}



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Hide the window title.
		//  requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		//    WindowManager.LayoutParams.FLAG_FULLSCREEN);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		// Create a RelativeLayout container that will hold a SurfaceView,
		// and set it as the content of our activity.
		setContentView(R.layout.activity_main);

		scanner = new MediaScannerConnection(getApplicationContext(),this);


		openBackCamera();
		mPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);
		
		setFacesBar();
	}
	
	private void setFacesBar()
	{
		SeekBar sb = (SeekBar)findViewById(R.id.seekbar_faces);
		
		sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			Toast toast = null;
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if(toast!=null){
					toast.cancel();
				}
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.faces)+" "+seekBar.getProgress(), Toast.LENGTH_SHORT);
				toast.show();
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(toast!=null){
					toast.cancel();
				}
				toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.faces)+" "+progress, Toast.LENGTH_SHORT);
				toast.show();
				mPreview.minFacesToDetect = progress;
				
			}
		});
		
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
			mPreview.setCameraID(id);
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
		//Toast.makeText(this.getApplicationContext(), "Number of cameras: "+nCameras, Toast.LENGTH_SHORT).show();

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

	@Override
	public void onMediaScannerConnected() {
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), appName);
		scanner.scanFile(mediaStorageDir.getAbsolutePath(), null);
		Log.i("Scanner", "Path scanned " + mediaStorageDir.getAbsolutePath());

	}

	@Override
	public void onScanCompleted(String path, Uri uri) {
		scanner.disconnect();

	}



}

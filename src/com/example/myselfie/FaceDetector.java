package com.example.myselfie;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.util.Log;
import android.widget.Toast;

public class FaceDetector implements FaceDetectionListener{

	
	@Override
	public void onFaceDetection(Face[] faces, Camera arg1) {
		if(faces.length>0)
		{
			
			System.out.println("FACESSSS");
			

		}

	}

}

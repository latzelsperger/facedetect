package com.exercise.facedetection;

import java.io.IOException;
import java.util.Observer;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Camera Preview class that displays a live image feed on the screen
 * 
 * @author ltz
 * 
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private SurfaceHolder	mHolder;
	private String			TAG	= getClass().getSimpleName();

	public CameraPreview(Context context) {
		super(context);
		CameraProvider.setContext(context);
		if (!CameraProvider.isFaceDetectSupported())
			Log.w(TAG, "Face detection not supported on this device!");

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);

		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, now tell the camera where to draw the
		// preview.
		Camera Camera = CameraProvider.getCamera();

		try {
			if (Camera == null)
				return;
			Camera.setPreviewDisplay(holder);
			Camera.startPreview();

			if (CameraProvider.isFaceDetectSupported())
				Camera.startFaceDetection();

		} catch (IOException e) {
			Log.d(TAG, "Error setting camera preview: " + e.getMessage());
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// empty. Take care of releasing the Camera preview in the activity.
		// CameraProvider.release();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		if (mHolder.getSurface() == null) {
			return;
		}
		Camera camera = CameraProvider.getCamera();

		// stop preview before making changes
		try {
			camera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}

		// react to e.g. orientation changes
		Camera.Parameters parameters = camera.getParameters();
		Size size;
		if (CameraProvider.getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
			size = getBestPreviewSize(w, h, parameters);
		} else {
			size = getBestPreviewSize(h, w, parameters);
		}

		if (size != null) {
			parameters.setPreviewSize(size.width, size.height);
			camera.setParameters(parameters);
			camera.startPreview();
		}

		// start preview with new parameters
		try {
			camera.setPreviewDisplay(mHolder);
			camera.startPreview();

			if (CameraProvider.isFaceDetectSupported()) {
				camera.startFaceDetection();
			}

		} catch (Exception e) {
			Log.d(TAG, "Error starting camera preview: " + e.getMessage());
		}
	}

	private Size getBestPreviewSize(int w, int h, Parameters parameters) {
		Camera.Size result = null;

		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
			if (size.width <= w && size.height <= h) {
				if (result == null) {
					result = size;
				} else {
					int resultArea = result.width * result.height;
					int newArea = size.width * size.height;

					if (newArea > resultArea) {
						result = size;
					}
				}
			}
		}

		return (result);
	}

	public void addObserver(Observer dot) {
		CameraProvider.setDetectionTarget(dot);
	}

}
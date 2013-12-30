package com.exercise.facedetection;

import java.util.Observer;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.Surface;

/**
 * Utility class the instantiates and releases the camera (front or back),
 * registers face detection listeners and provides information about the camera
 * api
 * 
 * @author ltz
 * 
 */
public class CameraProvider {

	private static Context				mContext;
	private static Camera				mCamera;
	private static LegacyFaceDetector	mCbk					= new LegacyFaceDetector();
	public static final int				CAM_FRONT				= 1;
	public static final int				CAM_BACK				= 0;
	public static int					CAM_TYPE				= CAM_FRONT;
	private static FaceListener			mFaceDetectionListener	= new FaceListener();
	private static boolean				USE_LEGACY_DETECTION	= true;
	private static int					mOrientation;

	public CameraProvider(Context _context) {
		mContext = _context;
	}

	public static int getNumCameras() {
		return Camera.getNumberOfCameras();
	}

	public static void release() {
		getCamera().release();
		mCamera = null;
	}

	public static boolean checkCamera() {
		return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}

	public static boolean isFaceDetectSupported() {
		return getCamera().getParameters().getMaxNumDetectedFaces() > 0;
	}

	@SuppressWarnings("unused")
	public static Camera getCamera() {

		try {
			int cams = getNumCameras();
			if (mCamera == null && cams > 0) {
				mCamera = Camera.open(CAM_TYPE);

				if (mContext instanceof Activity)
					setCameraDisplayOrientation((Activity) mContext, CAM_TYPE, mCamera);

				USE_LEGACY_DETECTION = !isFaceDetectSupported();
				if (USE_LEGACY_DETECTION) {
					mCamera.setPreviewCallback(mCbk);
				} else
					mCamera.setFaceDetectionListener(mFaceDetectionListener);

				int a = 0;
				mCamera.getParameters().setPreviewFormat(ImageFormat.RGB_565);

			}

			return mCamera;
		} catch (Exception e) {
			Log.e(CameraProvider.class.getSimpleName(), e.getMessage());
			return null;
		}
	}

	/**
	 * @param _addObserver
	 */
	public static void setDetectionTarget(Observer _addObserver) {
		if (USE_LEGACY_DETECTION)
			mCbk.addObserver(_addObserver);
		else
			mFaceDetectionListener.addObserver(_addObserver);
	}

	/**
	 * @param _context
	 */
	public static void setContext(Context _context) {
		mContext = _context;
	}

	public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		Display getOrient = activity.getWindowManager().getDefaultDisplay();
		setOrientation(Configuration.ORIENTATION_UNDEFINED);
		if (getOrient.getWidth() == getOrient.getHeight()) {
			setOrientation(Configuration.ORIENTATION_SQUARE);
		} else {
			if (getOrient.getWidth() < getOrient.getHeight()) {
				setOrientation(Configuration.ORIENTATION_PORTRAIT);
			} else {
				setOrientation(Configuration.ORIENTATION_LANDSCAPE);
			}
		}
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}

	/**
	 * @return the mOrientation
	 */
	public static int getOrientation() {
		return mOrientation;
	}

	/**
	 * @param mOrientation
	 *            the mOrientation to set
	 */
	public static void setOrientation(int mOrientation) {
		CameraProvider.mOrientation = mOrientation;
	}
}

package com.exercise.facedetection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Observer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.widget.Toast;

/**
 * Utility class the instantiates and releases the camera (front or back),
 * registers face detection listeners and provides information about the camera
 * api
 * 
 * @author ltz
 * 
 */
public class CameraProvider {

	public static final String	TAG	= CameraProvider.class.getSimpleName();
	public static String		mCurrentPhotoPath;

	/**
	 * @author ltz
	 * 
	 */
	private static class MyPictureCallback implements PictureCallback {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.hardware.Camera.PictureCallback#onPictureTaken(byte[],
		 * android.hardware.Camera)
		 */
		@Override
		public void onPictureTaken(byte[] _data, Camera _camera) {
			File pictureFile = null;
			try {
				pictureFile = createImageFile();
			} catch (IOException e1) {
				Log.d(TAG, "Error creating media file, check storage permissions: " + e1.getMessage());
			}
			if (pictureFile == null) {
				Log.d(TAG, "Error creating media file, check storage permissions!");
				return;
			}
			if (_data == null) {
				Log.w(TAG, "NULL data was received - not writing anything!");
				return;
			}
			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(_data);
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.d(TAG, "Error accessing file: " + e.getMessage());
			}

			galleryAddPic();
		}

		private File createImageFile() throws IOException {
			// Create an image file name
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
			String imageFileName = "facedetect_" + timeStamp + "_";
			File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			File image = File.createTempFile(imageFileName, /* prefix */
					".jpg", /* suffix */
					storageDir /* directory */
			);

			mCurrentPhotoPath = /* "file:" + */image.getAbsolutePath();
			return image;
		}

	}

	private static Context				mContext;
	private static Camera				mCamera;
	private static LegacyFaceDetector	mCbk					= new LegacyFaceDetector();
	public static final int				CAM_FRONT				= 1;
	public static final int				CAM_BACK				= 0;
	public static int					CAM_TYPE				= CAM_FRONT;
	private static FaceListener			mFaceDetectionListener	= new FaceListener();
	private static boolean				USE_LEGACY_DETECTION	= true;
	private static int					mOrientation;
	private static PictureCallback		mPictureCallback;

	public CameraProvider(Context _context) {
		mContext = _context;
		mPictureCallback = new MyPictureCallback();
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

	/**
	 * 
	 */
	public static void takePicture() {
		if (mPictureCallback == null)
			mPictureCallback = new MyPictureCallback();
		getCamera().takePicture(null, mPictureCallback, mPictureCallback);

	}

	private static void galleryAddPic() {
		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		File f = new File(mCurrentPhotoPath);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);

		mContext.sendBroadcast(mediaScanIntent);
		Toast.makeText(mContext, "Picture saved!", Toast.LENGTH_SHORT).show();
		getCamera().startPreview();
	}
}

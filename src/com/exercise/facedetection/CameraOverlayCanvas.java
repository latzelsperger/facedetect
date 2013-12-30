package com.exercise.facedetection;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.view.View;

/**
 * Transparent view that "floats" on top of the camera preview to draw a
 * bounding box around a detected face. Updates about detected faces happen
 * through the standard java observer mechanism.
 * 
 * @author ltz
 * 
 */
class CameraOverlayCanvas extends View {

	private Paint		mPaint;
	private Observer	mObserver;
	protected RectF		mRect;


	public CameraOverlayCanvas(Context context) {
		super(context);
		// prepare the bbox paint
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(Color.YELLOW);
		mPaint.setStrokeWidth(10);

	}

	public Observer getObserver() {
		if (mObserver == null) {
			mObserver = new Observer() {

				@Override
				public void update(Observable observable, Object data) {
					if (data instanceof Rect) {
						mRect = correctRotation((Rect) data);
						// Log.d(getClass().getSimpleName(), "face: " +
						// " Location X: " + mRect.centerX() + " Y: " +
						// mRect.centerY() + " W: "
						// + (mRect.right - mRect.left) + " H: " + (mRect.bottom
						// - mRect.top));
						invalidate();

					}

				}
			};
		}
		return mObserver;
	}

	/**
	 * @param _data
	 * @return
	 */
	protected RectF correctRotation(final Rect _data) {
		Matrix matrix = new Matrix();
		CameraInfo info = new CameraInfo();
		Camera.getCameraInfo(CameraProvider.CAM_TYPE, info);
		// Need mirror for front camera.
		boolean mirror = (info.facing == CameraInfo.CAMERA_FACING_FRONT);
		matrix.setScale(mirror ? -1 : 1, 1);
		// TODO: 1) react to orientation changes 2) camera axis of samsung
		// devices is rotated about 90 degrees...
		matrix.postRotate(90);
		// map coordinate range (-1000, -1000) to (1000, 1000) to screen size
		matrix.postScale(getWidth() / 2000f, getHeight() / 2000f);
		matrix.postTranslate(getWidth() / 2f, getHeight() / 2f);

		RectF dst = new RectF();
		RectF src = new RectF(_data);
		matrix.mapRect(dst, src);
		return dst;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		if (mRect != null && mPaint != null)
			canvas.drawRect(mRect, mPaint);

		super.onDraw(canvas);
	}

}
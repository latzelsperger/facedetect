package com.exercise.facedetection;

import java.util.Observable;

import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;

/**
 * Listener to detect faces on api-14-level devices, if they support face
 * detection
 */
public class FaceListener extends Observable implements FaceDetectionListener {

	@Override
	public void onFaceDetection(Face[] faces, Camera camera) {
		if (faces.length > 0 && faces[0].score > 50) {
			Rect r= faces[0].rect;


			setChanged();
			notifyObservers(r);
			
			// Log.d(getClass().getSimpleName(), "face: " + faces.length
			// + " Face 1 Location X: " + faces[0].rect.centerX() + " Y: "
			// + faces[0].rect.centerY() + " W: "
			// + (faces[0].rect.right - faces[0].rect.left) + " H: "
			// + (faces[0].rect.bottom - faces[0].rect.top));

		}

	}

}

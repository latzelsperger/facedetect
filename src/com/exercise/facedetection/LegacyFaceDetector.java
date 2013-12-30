package com.exercise.facedetection;

import java.io.ByteArrayOutputStream;
import java.util.Observable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;

/**
 * class that can be used for face detection if no api-level-14 face detection
 * is supported. CAUTION: this is SLOW due to image conversion from YUV to
 * Bitmap!!!
 * 
 */
public class LegacyFaceDetector extends Observable implements PreviewCallback {

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {

		BitmapFactory.Options bfo = new BitmapFactory.Options();
		bfo.inPreferredConfig = Bitmap.Config.RGB_565;
		bfo.inScaled = false;
		bfo.inDither = false;

		// Convert YUV to JPEG, decode as Bitmap
		Size previewSize = camera.getParameters().getPreviewSize();
		YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 80, baos);
		byte[] jdata = baos.toByteArray();

		Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length, bfo);
		if (bmp != null) {
			Rect r = findFace(bmp);
			if (r != null) {
				setChanged();
				notifyObservers(r);
			}
		}

	}

	private Rect findFace(Bitmap bmp) {
		// Ask for 1 face
		Face faces[] = new FaceDetector.Face[1];
		FaceDetector detector = new FaceDetector(bmp.getWidth(), bmp.getHeight(), 1);
		int count = detector.findFaces(bmp, faces);

		Face face = null;

		if (count > 0) {
			face = faces[0];

			PointF midEyes = new PointF();
			face.getMidPoint(midEyes);

			float eyedist = face.eyesDistance();
			PointF lt = new PointF(midEyes.x - eyedist * 2.0f, midEyes.y - eyedist * 2.5f);
			// Create rectangle around face. Create a box based on the eyes and
			// add some padding.
			// The ratio of head height to width is generally 9/5 but that makes
			// the rect a bit to tall.
			return new Rect(Math.max((int) (lt.x), 0), Math.max((int) (lt.y), 0), Math.min((int) (lt.x + eyedist * 4.0f), bmp.getWidth()),
					Math.min((int) (lt.y + eyedist * 5.5f), bmp.getHeight()));
		}

		return null;
	}

}

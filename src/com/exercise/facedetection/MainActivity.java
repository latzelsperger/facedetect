package com.exercise.facedetection;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

	private CameraPreview	mPreview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this);

		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);

		CameraOverlayCanvas dot = new CameraOverlayCanvas(this);
		mPreview.addObserver(dot.getObserver());
		FrameLayout overlay = (FrameLayout) findViewById(R.id.overlay);
		overlay.addView(dot);
		
		Button capt= (Button)findViewById(R.id.button_capture);
		capt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View _v) {
				CameraProvider.takePicture();
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		CameraProvider.release();
	}

	@Override
	protected void onPause() {
		super.onPause();
		CameraProvider.release();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem _item) {
		// if (_item.getTitle().toString().contains("Switch")) {
		//
		// int ct = CameraProvider.CAM_TYPE;
		// CameraProvider.CAM_TYPE = (ct - 1) * (ct - 1);
		// mPreview.restartPreview();
		//
		// }
		return super.onOptionsItemSelected(_item);
	}

}

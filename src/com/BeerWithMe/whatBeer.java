package com.BeerWithMe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class whatBeer extends Activity implements SurfaceHolder.Callback{

	private final static int SHARE_WITH = 0;
	protected static final String FileUtilities = null;
	private android.hardware.Camera mCamera = null;
	private Boolean mPreviewRunning = false;
	private Context mCtx = this;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		try{
			getWindow().setFormat(PixelFormat.TRANSLUCENT);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);

			setContentView(R.layout.whatbeer);
			SurfaceView mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);


			SurfaceHolder mSurfaceHolder = mSurfaceView.getHolder();

			mSurfaceHolder.addCallback(this);

			mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		}catch (Exception e){
			e.printStackTrace();
		}

	}

	public void goClicked(View view) {
		
		Toast.makeText(mCtx, "Saving File", Toast.LENGTH_SHORT).show();
		//mCamera.takePicture( mShutterCallback, null, mPictureCallback);
		new Task().execute();
	//	Intent i = new Intent(mCtx, shareWith.class);
	//	startActivityForResult(i, SHARE_WITH);
		//	Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		//startActivityForResult(intent, 0);

	}
	
	private android.hardware.Camera.PictureCallback mPictureCallback = new android.hardware.Camera.PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			if (data != null) {

				FileOutputStream outStream = null;
				File sd = Environment.getExternalStorageDirectory();

				if (!sd.canWrite()){
					Toast.makeText(mCtx, "Cannot write file to SD card, picture not saved", Toast.LENGTH_LONG).show();
					return;
				}
				try {
					// write to sdcard
					File file2 = new File(sd, "tmpfile2.jpg");
					outStream = new FileOutputStream(String.format(
							file2.getAbsolutePath()));
					outStream.write(data);
					outStream.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} 
				catch (Exception e){
					e.printStackTrace();
				}finally {
				}
				Toast.makeText(mCtx, "Picture Saved", Toast.LENGTH_LONG).show();
			}	
		}
	};
	
	private ShutterCallback mShutterCallback = new ShutterCallback() {
		  public void onShutter() {
			  Intent i = new Intent(mCtx, shareWith.class);
			  startActivityForResult(i, SHARE_WITH);
		  }
		};

	
	private class Task extends AsyncTask<Void, Void, Void> { 
		protected void onPreExecute() { 

		} 
		protected Void doInBackground(Void... unused) { 
			try { 
				mCamera.takePicture( mShutterCallback, null, mPictureCallback); 
			} catch (Exception e) { 
				Log.v("Error: ", "Exception", e); 
			} 
			return null; 
		} 
		protected void onPostExecute(Void unused) { 
		} 
	} 
	

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if (mPreviewRunning)
			if (mCamera != null)
				mCamera.stopPreview();

		// grab default parameters
		android.hardware.Camera.Parameters params =  mCamera.getParameters();

		try{
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			mCamera.setDisplayOrientation(90);
			List <Size> sizes = params.getSupportedPictureSizes();
			params.setRotation(90);
			params.setPreviewSize(272, 272);
			mCamera.setParameters(params);
		} catch (Exception e){
			e.printStackTrace();
		}



		try {
			mCamera.setPreviewDisplay(holder);

		} catch (IOException e) {
			e.printStackTrace();
		}

		mCamera.startPreview();
		mPreviewRunning = true;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// create camera instance
		try{
			mCamera = android.hardware.Camera.open();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mCamera.stopPreview();
		mPreviewRunning = false;
		mCamera.release();
	}
}
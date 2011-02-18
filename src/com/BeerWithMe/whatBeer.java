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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class whatBeer extends Activity implements SurfaceHolder.Callback{

	// static variables
	private final static int SHARE_WITH = 0;						// code for launching share with activity
	public static final String EXTRAS_BEER_NAME = "extrasBeerName";	// static variable for BeerName key
	public static final String EXTRAS_PIC_BYTES = "extrasPicBytes";	// static variable for where camera bytes are stored
	
	// private variables
	private android.hardware.Camera mCamera = null;					// camera object
	private Boolean mPreviewRunning = false;						// boolean if the camera is currently running
	private Context mCtx = this;									// Context for this activity
	private byte[] mCamData=null;									// byte array for camera image data
	
	// pointers to layout objects
	private Button mKeepButton; 
	private Button mGoButton;
	private Button mRetakeButton;
	private LinearLayout mBottomLinearLayout;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setup surface for holding camera
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
		
		// grab pointers to objects
		mKeepButton = (Button) findViewById(R.id.keepButton);
		mGoButton = (Button) findViewById(R.id.goButton);
		mRetakeButton = (Button) findViewById(R.id.retakeButton);
		mBottomLinearLayout = (LinearLayout) findViewById(R.id.linearLayoutBottom);
	}

	public void goClicked(View view) {
		
		mCamera.takePicture( null, null, mPictureCallback); 
		mPreviewRunning = false;
		//Toast.makeText(mCtx, "Saving File", Toast.LENGTH_SHORT).show();
		//mCamera.takePicture( mShutterCallback, null, mPictureCallback);
		//new Task().execute();
		//Intent i = new Intent(mCtx, shareWith.class);
		//startActivityForResult(i, SHARE_WITH);

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void launchShareWith(){
		
		// create intent to start next string and send data
		// create intent
		Intent i = new Intent(mCtx, shareWith.class);
        i.putExtra(whatBeer.EXTRAS_BEER_NAME, "tmpBeerName");
        i.putExtra(whatBeer.EXTRAS_PIC_BYTES, mCamData);
        int kyle = mCamData.length;
        startActivityForResult(i, SHARE_WITH);
	}
	
	public void keepClicked(View view){
		// switch views for buttons
        mBottomLinearLayout.setVisibility(LinearLayout.GONE);
        mGoButton.setVisibility(Button.VISIBLE);
		Intent i = new Intent(mCtx, shareWith.class);
		startActivityForResult(i, SHARE_WITH);
	}
	
	public void retakeClicked(View view){
		// switch views for buttons
        mBottomLinearLayout.setVisibility(LinearLayout.GONE);
        mGoButton.setVisibility(Button.VISIBLE);
        mCamera.startPreview();
        mPreviewRunning = true;
	}
	
	private android.hardware.Camera.PictureCallback mPictureCallback = new android.hardware.Camera.PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, android.hardware.Camera camera) {

			if (data != null) {
				
				mCamData = data;
				//launchShareWith();
				//Intent i = new Intent(mCtx, shareWith.class);
		        //i.putExtra(whatBeer.extrasBeerName, "tmpBeerName");
		        //i.putExtra(whatBeer.extrasPicBytes, mCamData);
		        BeerWithMeApp appState = (BeerWithMeApp) getApplicationContext();
		        appState.camBytes = data;
		        
		        // switch views for buttons
		        mGoButton.setVisibility(Button.GONE);
		        mBottomLinearLayout.setVisibility(LinearLayout.VISIBLE);
		        
		        
		        //mCamera.startPreview();
		        //startActivityForResult(i, SHARE_WITH);

		        /*
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
				*/
			}	
		}
	};	

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
			params.setPictureSize(640, 480);
			params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
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
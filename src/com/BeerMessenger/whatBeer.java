package com.BeerMessenger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.tools.*;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
	private Boolean mTryingToTakePicture = false;					// boolean used by autofocus callback
	protected static final WidthHeight mMaxWidthHeight = new WidthHeight(3000, 3000);
	protected static final WidthHeight mOptimalWidthHeight = new WidthHeight(400, 400);

	// pointers to layout objects
	private Button mKeepButton; 
	private ImageButton mGoButton;
	private Button mRetakeButton;
	private LinearLayout mBottomLinearLayout;
	private AutoCompleteTextView mBeerNameView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setup surface for holding camera
		try{
			getWindow().setFormat(PixelFormat.TRANSLUCENT);
			
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
		mGoButton = (ImageButton) findViewById(R.id.goButton);
		mRetakeButton = (Button) findViewById(R.id.retakeButton);
		mBottomLinearLayout = (LinearLayout) findViewById(R.id.linearLayoutBottom);
		mBeerNameView = (AutoCompleteTextView) findViewById(R.id.beerName);
	}

	/** Called when the camera viewing surface is clicked and auto focuses */
	public void surfaceClicked(View view){
		//TODO: auto focus on the correct spot instead of just the center
		
		// hide the keyboard if it's up
		com.tools.Tools.hideKeyboard(this, mBeerNameView);
		
		// autofocus
	//	mGoButton.setEnabled(false);
	//	mCamera.cancelAutoFocus();
	//	mCamera.autoFocus(myAutoFocusCallback);
	}

	/** Simple autofocus callback that re-enables 
	 * the go button and takes a picture if necessary */
	private AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback(){

		@Override
		public void onAutoFocus(boolean arg0, Camera arg1) {

			// if we are trying to take a picture then make 
			// that callback and tell this class that we are no longer previewing
			if (mTryingToTakePicture){
				mCamera.takePicture( null, null, mPictureCallback); 
				mPreviewRunning = false;
			}else{

				// enable the go button again if we are not taking a picture
				mGoButton.setEnabled(true);
			}
		}
	};

	/** Go button clicked. Start autofocus, disable button and then take picture */
	public void goClicked(View view) {

		mGoButton.setEnabled(false);
		mCamera.cancelAutoFocus();
		mTryingToTakePicture = true;
		mCamera.autoFocus(myAutoFocusCallback);
	}

	/** When returned from an activity */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	/** create and launch intent to start next window and store beer name */
	private void launchShareWith(){
		// store beer message in app data
		((BeerWithMeApp)getApplicationContext()).setBeerMessage(mBeerNameView.getText().toString());
		
		// launch next window
		Intent i = new Intent(mCtx, shareWith.class);
		startActivityForResult(i, SHARE_WITH);
	}

	/** Reset standard buttons and launch next window */
	public void keepClicked(View view){
		// switch views for buttons
		mBottomLinearLayout.setVisibility(LinearLayout.GONE);
		mGoButton.setVisibility(Button.VISIBLE);

		// launch next window
		launchShareWith();
	}

	/** Switch back to standard buttons */
	public void retakeClicked(View view){

		// switch views for buttons
		mBottomLinearLayout.setVisibility(LinearLayout.GONE);
		mGoButton.setVisibility(Button.VISIBLE);

		// start preview again
		mCamera.startPreview();
		mPreviewRunning = true;
	}

	/** callback for taking a picture that saves important camera byte data*/
	private android.hardware.Camera.PictureCallback mPictureCallback = new android.hardware.Camera.PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, android.hardware.Camera camera) {

			// only save data if data is not null
			if (data != null) {

				// store camera data in global app data
				//BeerWithMeApp appState = (BeerWithMeApp) getApplicationContext();
				//appState.camBytes = data;
				BeerWithMeApp.camBytes = data;

				// switch views for buttons
				mGoButton.setVisibility(Button.GONE);
				mBottomLinearLayout.setVisibility(LinearLayout.VISIBLE);

				// keep track that we are trying to take a picture
				mTryingToTakePicture = false;
				
				// done taking picture, so we can re-enable go button
				mGoButton.setEnabled(true);
				
			// camera not generating any data	
			}else{
				Toast.makeText(mCtx, "**Problem ** Camera is not generating any jpeg data", Toast.LENGTH_LONG).show();
			}
		}
	};	

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
		// turn off go button, will turn back on when done
		mGoButton.setVisibility(Button.INVISIBLE);
		
		// stop the preview
		if (mPreviewRunning)
			if (mCamera != null)
				mCamera.stopPreview();

		// grab default parameters
		android.hardware.Camera.Parameters params =  mCamera.getParameters();

		// set parameters
		try{
			// set orientation
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			mCamera.setDisplayOrientation(90);
			params.setRotation(90);
			
			// turn on flash
			params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
			
			// get possible preview sizes and image sizes
			List <Size> sizes = params.getSupportedPictureSizes();
			List<Size> previewSizes = params.getSupportedPreviewSizes();
			
			// get the image size that is closest to our optimal and set it
			WidthHeight bestWidthHeight = 
				getBestWidthHeight(sizes, mMaxWidthHeight, mOptimalWidthHeight);
			if (bestWidthHeight == null){
				Toast.makeText(this, "Could not find a good camera size...Quitting", Toast.LENGTH_SHORT).show();
				finish();
			}else{
				params.setPictureSize(bestWidthHeight.width, bestWidthHeight.height);
			}
			
			// get the preview size that is closest to the image size
			WidthHeight bestWidthHeightPreivew = 
				getBestWidthHeight(previewSizes, mMaxWidthHeight, bestWidthHeight);
			
			// determine how best to fit camera preview into surface
			if (bestWidthHeightPreivew != null){
				params.setPreviewSize(bestWidthHeightPreivew.width, bestWidthHeightPreivew.height);
				WidthHeight fitWindowWidthHeight = Tools.fitNoCrop(bestWidthHeightPreivew, new WidthHeight(width, height));
				SurfaceView mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
				mSurfaceView.setLayoutParams(new LinearLayout.LayoutParams(fitWindowWidthHeight.width, fitWindowWidthHeight.height, (float) 0.0));
			}
			
			// actually set the  parameters
			mCamera.setParameters(params);
			
		} catch (Exception e){
			Log.d("whatBeer", e.toString());
			e.printStackTrace();
		}

		// set the previewdisplay holder
		try {
			mCamera.setPreviewDisplay(holder);

		} catch (IOException e) {
			Log.d("whatBeer", e.toString());
			e.printStackTrace();
		}

		// start the preview
		mCamera.startPreview();
		mPreviewRunning = true;
		
		// turn go button back on
		mGoButton.setVisibility(Button.VISIBLE);
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
	
	/** Determine the optimal width and height, based on max size and optimal choice */
	private WidthHeight getBestWidthHeight(List <Size> sizes, WidthHeight maxWH, WidthHeight optWH){

		// check if none
		if (sizes.isEmpty())
			return null;
		
		// loop through possible ones and find the ones that are below the max
		ArrayList <Size> belowMax = new ArrayList<Size>();
		for (Iterator<Size> it = sizes.iterator (); it.hasNext();) {
		    Size s = it.next ();
		    if (maxWH == null)
		    	belowMax.add(s);
		    else if (s.width <= maxWH.width && s.height <= maxWH.height)
		    	belowMax.add(s);
		}
		
		// check if none
		if (belowMax.isEmpty())
			return null;
		
		// function to check optimal is diff(width)^2 + diff(height)^2, and aspect ratio is 10x more important
		WidthHeight result = new WidthHeight(0, 0);
		double fitness = 1e12;
		double tmpFitness;
		for (Iterator<Size> it = belowMax.iterator (); it.hasNext();) {
		    Size s = it.next ();
		    tmpFitness = (double) Math.sqrt(Math.pow(s.width - optWH.width, 2) + 
		    			 Math.pow(s.height - optWH.height, 2))/(optWH.height*.5+optWH.width*.5)+
		    			 Math.abs((double)optWH.width/optWH.height - (double)s.width/s.height)*10;
		    if (tmpFitness < fitness){
		    	fitness = tmpFitness;
		    	result.width = s.width;
		    	result.height = s.height;
		    }
		}
		
		// check if nothing matched
		if (result.width == 0 && result.height == 0)
			result = null;
		
		// return result
		return result;
		
	}
}

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
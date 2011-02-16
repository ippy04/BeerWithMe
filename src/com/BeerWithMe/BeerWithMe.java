package com.BeerWithMe;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class BeerWithMe extends Activity {

	// pointers to layout objects
	ImageView mMainIconObj; 
	TextView mBeersReviewedObj;
	
	// preferences actual numbers and keywords
	long mUserId;
	Integer mTotalBeers;
	private static final String BEERS_REVIEWED = "beerReviewed";
	private static final String USER_ID = "userId";

	// settings
	SharedPreferences mSettings;
	
	// intent codes
	private static final int WHAT_BEER=0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);   

		// Restore preferences
		mSettings = getPreferences(0);

		// find object IDs
		mMainIconObj = (ImageView) findViewById(R.id.mainIcon);
		mBeersReviewedObj = (TextView) findViewById(R.id.beersReivewedNumber);

		// set main icon image
		mMainIconObj.setImageResource(R.drawable.beerwithmehomeicon);

		// grab number of beers reviewed and assign to text
		mTotalBeers = mSettings.getInt(BEERS_REVIEWED, 0);
		mBeersReviewedObj.setText(mTotalBeers.toString());

		// grab user ID
		mUserId = mSettings.getLong(USER_ID, -1);
		if (mUserId == -1)
			getIdFromServer();

		// set background color
		View mlayout = (View) findViewById(R.id.main);
		mlayout.setBackgroundColor(Color.BLUE);
	}

	// Open next page when share is clicked
	public void shareClicked(View view) {
		Intent i = new Intent(this, whatBeer.class);
		startActivityForResult(i, WHAT_BEER);
	}
	
	// when activity is finished we are returned back here
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        
        switch(requestCode) {
    	case WHAT_BEER:
    		if (resultCode == 1){
    			mTotalBeers++;
    			Editor ed = mSettings.edit();
    			ed.putInt(BEERS_REVIEWED, mTotalBeers);
    			ed.commit();
    		}
        }
    }

	// function to start thread to grab id from server if we need one
	void getIdFromServer(){
		new getIdFromServerTask().execute(0);
	}

	// function to take a picture
	public void takePicture(){

		// create camera instance
		android.hardware.Camera cam = android.hardware.Camera.open();

		// grab default parameters and adjust if desired
		android.hardware.Camera.Parameters params =  cam.getParameters();
		// adjust here
		cam.setParameters(params);


	}

	// AsyncTask that stars new thread to grab a new user id from server
	private class getIdFromServerTask extends AsyncTask<Integer, Integer, Long> {
		protected Long doInBackground(Integer...inputs) {
			//TODO: write get command to get user ID from server
			return (long) 1;
		}

		// do nothing
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
		}

		protected void onPostExecute(Long result) {
			mUserId = result;
		}
	}
}
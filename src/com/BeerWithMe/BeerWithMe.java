package com.BeerWithMe;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import com.tools.SuccessReason;
import com.tools.*;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
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
	private String mUrl = "beerwithme.heroku.com/auth.xml";

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
		//if (mUserId == -1)
			getIdFromServer();

		// set background color
		View mlayout = (View) findViewById(R.id.main);
		mlayout.setBackgroundColor(Color.BLUE);

		// test
		String number = Tools.getMyStrippedPhoneNumber(this);
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
			requestSecret();
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

	// grab user name and password
	protected SuccessReason requestSecret(){
		Boolean result = true;

		// get phone number
		String phone = com.tools.Tools.getMyStrippedPhoneNumber(this);
		if (phone==null)
			phone = requestPhoneFromUser();

		// xml String to post to server
		String xml = "GET -d <?xml version='1.0' encoding='UTF-8'?>" +
		"<user><phone_number>"+phone+"</phone_number></user> " +
		"-H \"Content-Type: text/xml\"  "+mUrl;

		// default response
		String response="No Response From Server";

		// initialize string to be sent to server
		StringEntity se = null;
		try {
			se = new StringEntity(xml,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// set the type of text and what server to send to
		se.setContentType("text/xml");
		HttpPost getRequest = new HttpPost("http://"+mUrl);
		getRequest.setEntity(se);

		// create http client and response handler
		HttpClient httpclient = new DefaultHttpClient();  
		ResponseHandler <String> res=new BasicResponseHandler();

		// Execute HTTP Post Request  
		try {
			response = httpclient.execute(getRequest, res);
		} catch (ClientProtocolException e) {
			response = e.toString();
			result = false;
		} catch (IOException e) {
			result = false;
			response = e.toString();
		} 

		// print toast if error
		if (response=="No Response From Server"){
			result = false;
		}

		// store data to log
		Log.d("tag", response);
		//Log.d("tag2", getRequest.getEntity().toString());

		// return result
		SuccessReason getResult = new SuccessReason(result, response);
		return getResult;
	}

	protected String requestPhoneFromUser(){
		//TODO: actually request user phone number instead of hard code
		return "2345678910";
	}
}
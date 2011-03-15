package com.BeerMessenger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import com.tools.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class BeerWithMe extends Activity {

	//TODO: picture is being stored with a random gps location
	// pointers to layout objects
	ImageView mMainIconObj; 
	TextView mBeersReviewedObj;
	TextView mBeerQuoteObj;
	TextView mAppNameObj;
	TextView mEnjoyTextObj;
	Button mShareButtonObj;
	TextView mBeersReviewedTextObj;
	int mGetSecretAttempts = 0;
	int mMaxGetSecretAttempts = 3;
	int mGetQuoteAttempts = 0;
	int mMaxGetQuoteAttempts = 3;

	// preferences actual numbers and keywords
	private String mUrl = "beermessenger.com/users.xml";
	private String mUrlQuote = "beermessenger.com/request_quote.xml";

	// intent codes
	private static final int WHAT_BEER=0;
	
	Context mCtx = this;
	Activity mAct = this;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);   

		// find object IDs
		mMainIconObj = (ImageView) findViewById(R.id.mainIcon);
		mBeersReviewedObj = (TextView) findViewById(R.id.beersReivewedNumber);
		mBeerQuoteObj = (TextView) findViewById(R.id.beerQuote);
		mAppNameObj = (TextView) findViewById(R.id.appName);
		mEnjoyTextObj = (TextView) findViewById(R.id.enjoyText);
		mShareButtonObj = (Button) findViewById(R.id.shareButton);
		mBeersReviewedTextObj = (TextView) findViewById(R.id.beersReivewedText);

		// set main icon image
		mMainIconObj.setImageResource(R.drawable.android_bartender);
		
		// set custom fonts
		Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Philosopher.ttf");
		mAppNameObj.setTypeface(typeface);
		mEnjoyTextObj.setTypeface(typeface);
		mBeersReviewedObj.setTypeface(typeface);
		mShareButtonObj.setTypeface(typeface);
		mBeersReviewedTextObj.setTypeface(typeface);
 
		// grab user ID, secret, and user name	
		getName();
		getId();
		getSecret();
	}

	protected void setRandomBeerQuote(){
		// grab quote from app data
		BeerWithMeApp app = (BeerWithMeApp)getApplicationContext();
		String quote = app.getBeerQuote();
		
		// if nothing there, then get a random locally
		if (quote == null)
			quote = getRandomBeerQuoteLocally();
		if (quote.length()==0)
			quote = getRandomBeerQuoteLocally();
		
		// set it to text
		mBeerQuoteObj.setText(quote);
		
		// grab a new one from server
		new getBeerQuoteFromServerTask().execute(0);
		//Random g = new Random(System.currentTimeMillis());
		//int index = (int)(g.nextDouble()*mBeerQuotes.length);
		//mBeerQuoteObj.setText(mBeerQuotes[index]);	
	}
	
	protected String getRandomBeerQuoteLocally(){
		Random g = new Random(System.currentTimeMillis());
		int index = (int)(g.nextDouble()*mBeerQuotes.length);
		return (mBeerQuotes[index]);	
	}
	
	protected String[] mBeerQuotes = {"Alcohol, the cause and solution to all of life's problems. -Homer Simpson-", 
			"All right, brain, I don't like you and you don't like me - so let's just do this and I'll get back to killing you with beer. -Homer Simpson-",
			"Beer is proof that God loves us and wants us to be happy.-Benjamin Franklin-"};

	@Override 
	protected void onResume()
	{
		// standard super class resume, MUST be here
		super.onResume();
		
		// set random beer quote
		setRandomBeerQuote();
		
		// grab number of beers reviewed and assign to text
		Integer totalBeers=null;
		totalBeers = ((BeerWithMeApp)getApplicationContext()).getNBeersReviewed();
		mBeersReviewedObj.setText(totalBeers.toString());
	}
	
	// get user name
	void getName(){

		// grab from app data, if not here, then request from user
		// we don't actually do anything with it now, but we make sure it is stored in the app data
		BeerWithMeApp app = (BeerWithMeApp)getApplicationContext();
		String tmpName = app.getUserName();
		if (tmpName==null)
			requestUserName();		
	}
	
	// get user id from phone or ask user
	void getId(){

		// grab from app data, if not here, grab from phone, if not there, then request from user
		// we don't actually do anything with it now, but we make sure it is stored in the app data
		BeerWithMeApp app = (BeerWithMeApp)getApplicationContext();
		String tmpId = app.getUserID();
		if (tmpId==null){
			String phone = com.tools.Tools.getMyStrippedPhoneNumber(this);
			if (phone==null)
				requestPhoneFromUser();
			else
				app.setUserID(phone);
		}
	}
	
	// get secret from server
	void getSecret(){
		
		// grab from app data, if not here, grab from server
		// we don't actually do anything with it now, but we make sure it is stored in the app data		
		BeerWithMeApp app = (BeerWithMeApp)getApplicationContext();
		
		// check user name first, if null, then don't bother
		if (app.getUserName()==null | app.getUserID()==null)
			return;
		String tmpSecret = app.getSecret();
		if (tmpSecret == null)
			getSecretFromServer();
	}
	
	// Open next page when share is clicked
	public void shareClicked(View view) {
		Intent i = new Intent(this, whatBeer.class);
		startActivityForResult(i, WHAT_BEER);
	}	

	// function to start thread to grab id from server if we need one
	void getSecretFromServer(){
		new getSecretFromServerTask().execute(0);
	}

	// AsyncTask that starts new thread to grab a new user id from server
	private class getSecretFromServerTask extends AsyncTask<Integer, Integer, String> {
		protected String doInBackground(Integer...inputs) {
			//TODO: write get command to get user ID from server
			//TODO: handle a failed password request from server
			SuccessReason getRequest = requestSecret();
			Boolean success = false;
			String result = null;
			if (getRequest.getSuccess())
				if (com.tools.Tools.getXmlValueAtTag(getRequest.getReason(), "status").equalsIgnoreCase("success")){
					result =  getSecretFromXml(getRequest.getReason());
					if (result !=null)
						success = true;
				}
			
			return result;
		}

		// do nothing
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
		}

		protected void onPostExecute(String result) {
			
			// iterate how many tries
			mGetSecretAttempts++;
			
			// failed get so try again, but only 3 times
			if (result==null & mGetSecretAttempts < mMaxGetSecretAttempts){
				getSecretFromServer();
			}else if (result ==null){
				Toast.makeText(mCtx, "Cannot gain access to server to acquire secrue connection. Quitting", 
						Toast.LENGTH_LONG).show();
				mAct.finish();
				return;
			}else{
				mGetSecretAttempts = 0;
				((BeerWithMeApp)getApplicationContext()).setSecret(result);
			}
		}
	}  

	// AsyncTask that grabs new quote from server
	private class getBeerQuoteFromServerTask extends AsyncTask<Integer, Integer, String> {
		protected String doInBackground(Integer...inputs) {
			
			// create http client and get a string returned from url
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet("http://"+mUrlQuote);
	    	String response=null;
	    	ResponseHandler <String> res=new BasicResponseHandler();
	    	try {
	    		response = httpclient.execute(httpget, res);
	    	} catch (ClientProtocolException e1) {
	    	} catch (IOException e1) {
	    	}
	    		
	    	// check for null response
	    	if (response==null)
	    		return response;

	    	// extract beer quote
	    	response = com.tools.Tools.getXmlValueAtTag(response, "beer-quote");
	    	
	    	// convert html to string
	    	return android.text.Html.fromHtml(response).toString();
		}

		// do nothing
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
		}

		protected void onPostExecute(String result) {
			
			// iterate how many tries
			mGetQuoteAttempts++;
			
			// failed get so try again, but only 3 times
			if (result==null & mGetQuoteAttempts < mMaxGetQuoteAttempts){
				new getBeerQuoteFromServerTask().execute(0);
				return;
				
			// get random quote locally	
			}else if (result ==null){
				result = getRandomBeerQuoteLocally();
				mGetQuoteAttempts=0;
			}else{
				mGetSecretAttempts = 0;
			}
			
			// set app data to new result
			BeerWithMeApp app = (BeerWithMeApp)getApplicationContext();
			app.setBeerQuote(result);
		}
	}

	// grab user name and password
	private SuccessReason requestSecret(){
		Boolean result = true;

		// grab phone number
		BeerWithMeApp app = (BeerWithMeApp)getApplicationContext();
		String phone = app.getUserID();
		String name = app.getUserName();

		// xml String to post to server
		String xml = "POST -d <?xml version='1.0' encoding='UTF-8'?>" +
		"<user><phone_number>"+phone+"</phone_number><name>"+
		name+"</name></user> " +
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

		// return result
		SuccessReason getResult = new SuccessReason(result, response);
		return getResult;
	}

	/** Launches dialog to request user's phone number and sends value to */
	private void requestPhoneFromUser(){
		
		// grab id from app info
		BeerWithMeApp app = (BeerWithMeApp)getApplicationContext();
		String tmpId = app.getUserID();
		if (tmpId==null){tmpId="";}
		
		AlertDialog builder = new AlertDialog.Builder(this).create();
		final EditText input = new EditText(this);
		input.setText(tmpId);
		
		// keyListener for phone number to demand numeric data
		DigitsKeyListener MyDigitKeyListener = new DigitsKeyListener(true, false); // first true : is signed, second one : is decimal
		input.setKeyListener( MyDigitKeyListener );
		
		input.setHint("Your phone number");
	   	builder.setView(input);
	   	builder.setCancelable(false);
	    builder.setButton("OK", new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String value = input.getText().toString();
				if (value.length()==0)
					requestPhoneFromUser();
				else{
					value = com.tools.Tools.keepOnlyNumerics(com.tools.Tools.formatPhoneNumber(value));
					((BeerWithMeApp)getApplicationContext()).setUserID(value);
					getSecret();
				}
				
			}
		});
	   	
	    builder.show();	   
	}	
	
	/** Launches dialog to request user's phone number and sends value to */
	private void requestUserName(){	
		
		// grab id from app info
		BeerWithMeApp app = (BeerWithMeApp)getApplicationContext();
		String tmpId = app.getUserName();
		if (tmpId==null){tmpId="";}
		
		AlertDialog builder = new AlertDialog.Builder(this).create();
		final EditText input = new EditText(this);
		input.setText(tmpId);
		input.setHint("Your Name");
	   	builder.setView(input);
	   	builder.setCancelable(false);
	    builder.setButton("OK", new OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String value = input.getText().toString();
				if (value.length()==0)
					requestUserName();
				else{
					((BeerWithMeApp)getApplicationContext()).setUserName(value);
					getSecret();
				}
				
			}
		});
	   	
	    builder.show();	    
	}
	
	private String getSecretFromXml(String xml){
		return com.tools.Tools.getXmlValueAtTag(xml, "password");
	}
}
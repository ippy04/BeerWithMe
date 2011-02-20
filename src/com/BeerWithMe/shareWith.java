package com.BeerWithMe;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

public class shareWith extends Activity {

	android.hardware.Camera mCamera = null;
	byte[] mPicData;
	private String mUserId="2";
	private String mTargetNumber = "3109800919";
	private String mAvatarFileName = "testing.png";
	private String mBeerName = null;
	private String mMessage = "Drink up Bitches!!";
	private String mUrl = "beerwithme.heroku.com/beer_pics.xml";
	private Context mCtx = this;
	private Boolean mImagePosted = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sharewith);

		// try to grab data from global variable
		BeerWithMeApp appState = (BeerWithMeApp) getApplicationContext();
		mPicData = appState.camBytes;

		// grab extras that were passed into this intent
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mBeerName = extras.getString(whatBeer.EXTRAS_BEER_NAME);

			// check null pointers
			if (mBeerName==null){
				mBeerName = "Null Beer Name - Should not happen";
				Log.d("shareWithDebug", "Passed beer name should not be null, please pass correctly from whatBeer");
			}
			if (mPicData==null)
				Log.d("shareWithDebug","mPicData should not be null, please pass correctly from whatBeer");
		}else{
			Log.d("shareWithDebug", "shareWith was not passed any extras with the intent... This should not happen");
		}
		
		// error checking
		if (mBeerName==null){
			mBeerName = "Null Beer Name - Should not happen";
			Log.d("shareWithDebug", "Passed beer name should not be null, please pass correctly from whatBeer");
		}
		if (mBeerName.length()==0)
			mBeerName = "No Beer Entered!!!";
	}

	public void goClicked1(View view){
		//TODO: managedQuery is @deprecated
		//TODO: should not be posting data with this button click

		// grab phone number
		AutoCompleteTextView whoText = (AutoCompleteTextView) findViewById(R.id.searchName);
		mTargetNumber = whoText.getText().toString();
		postDataUsingAsync();
		/*
		String name = "KYle";
		String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '" + ("1") + "'";
		selection += "AND UPPER(" + ContactsContract.Contacts.DISPLAY_NAME + ")= UPPER('" + name + "')";
		Uri uri = ContactsContract.Contacts.CONTENT_URI;        
		String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
		Cursor myCursor=null;
		try{
			myCursor = this.managedQuery(uri, null, selection, null, sortOrder);
		}catch (Exception e){
			e.printStackTrace();
		}
		myCursor.moveToFirst();
		String id = myCursor.getString(myCursor.getColumnIndex(ContactsContract.Contacts._ID));
		Cursor pCur = getContentResolver().query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", new String[]{id}, null);
		pCur.moveToFirst();
		String newString = pCur.getString(pCur.getColumnIndex(CommonDataKinds.Phone.NUMBER));
		int kyle = 6;

		//Contacts C = new Contacts;
		//C.
		//Uri lookupUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey);


		ArrayList<String> string = getPhoneNumbers("Kyle");
		int kyle2 = 6;
		 */
	}

	public void goClicked2(View view){

	}

	private ArrayList<String> getPhoneNumbers(String id) 
	{
		ArrayList<String> phones = new ArrayList<String>();

		Cursor cursor = this.getContentResolver().query(
				CommonDataKinds.Phone.CONTENT_URI, 
				null, 
				CommonDataKinds.Phone.CONTACT_ID +" = ?", 
				new String[]{id}, null);

		while (cursor.moveToNext()) 
		{
			phones.add(cursor.getString(cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER)));
		} 

		cursor.close();
		return(phones);
	}

	/** Asynctask for posting data to server */
	private class postDataTask extends AsyncTask<Void, Void, PostResult>{

		@Override
		protected void onPreExecute() {
			Toast.makeText(mCtx, "Sending Image", Toast.LENGTH_LONG).show();
		}

		@Override
		protected PostResult doInBackground(Void... arg0) {
			
			// the result if successful or not
			Boolean result = true;
			
			// xml String to post to server
			String xml = "POST -d <?xml version='1.0' encoding='UTF-8'?>" +
			"<beer_pic><user_id>"+mUserId+"</user_id><avatar type='String'>" +
			Base64.encodeToString(mPicData, Base64.DEFAULT) +
			"</avatar><avatar_file_name>"+mAvatarFileName+"</avatar_file_name>" +
			"<target_number>"+mTargetNumber+"</target_number>" +
			"<beer_name>"+mBeerName+"</beer_name>" +
			"<message>"+mBeerName+"</message></beer_pic> " +
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
			HttpPost postRequest = new HttpPost("http://"+mUrl);
			postRequest.setEntity(se);

			// create http client and response handler
			HttpClient httpclient = new DefaultHttpClient();  
			ResponseHandler <String> res=new BasicResponseHandler();
			
			// Execute HTTP Post Request  
			try {
				response = httpclient.execute(postRequest, res);
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
			Log.d("tag2", postRequest.getEntity().toString());
			
			// return result
			PostResult postResult = new PostResult(result, response);
			return postResult;
		}

		@Override
		protected void onPostExecute(PostResult result) {
			if (result.result){
				Toast.makeText(mCtx, "Image Sent Successfully", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(mCtx, "Image Could Not be Posted because "+result.response, Toast.LENGTH_LONG).show();
			}
			mImagePosted = result.result;
		}
	}
	
	private class PostResult{
		public Boolean result;
		public String response;
		
		PostResult(Boolean RESULT, String RESPONSE){
			result = RESULT;
			response = RESPONSE;
			
		}
	}
	
	// post data to server
	public void postDataUsingAsync(){
		new postDataTask().execute();
	}
}
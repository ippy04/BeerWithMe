package com.BeerWithMe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class BeerWithMe extends Activity {

	// pointers to layout objects
	ImageView mMainIconObj; 
	TextView mBeersReviewedObj;

	// settings
	SharedPreferences mSettings;

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
		Integer totalBeers = mSettings.getInt("beersReviewed", 0);
		mBeersReviewedObj.setText(totalBeers.toString());

		// set background color
		View mlayout = (View) findViewById(R.id.main);
		mlayout.setBackgroundColor(Color.BLUE);
	}

	public void shareClicked(View view) {
		//  Intent i = new Intent(this, NoteEdit.class);
		//  startActivityForResult(i, ACTIVITY_CREATE);
		setContentView(R.layout.whatbeer);
		
//		Intent i = new Intent(this, NoteEdit.class);
//        startActivityForResult(i, ACTIVITY_CREATE);
	}

	public void goClicked(View view) {
		//  Intent i = new Intent(this, NoteEdit.class);
		//  startActivityForResult(i, ACTIVITY_CREATE);
		setContentView(R.layout.sharewith);
	}

	public void goClicked1(View view){
		//TODO: managedQuery is depricated
		//TODO: terrible terrible code, but just wanted to get access to phonebook working

		postData2();
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
	public void goClicked2(View view){

	}

	public void postData() {  
		// Create a new HttpClient and Post Header  
		HttpClient httpclient = new DefaultHttpClient();  
		HttpPost httppost = new HttpPost("http://beerwithme.heroku.com/beer_pics");  
		String response=null;
		ResponseHandler <String> res=new BasicResponseHandler();

		try {  
			
			// grab strings
			EditText Text1 = (EditText)findViewById(R.id.firstName);
			String string1 = Text1.getText().toString();
			EditText Text2 = (EditText)findViewById(R.id.phoneNumber);
			String string2 = Text2.getText().toString();
			
			// Add your data  
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);    
			nameValuePairs.add(new BasicNameValuePair("beer_pic", "<pic>Test</pic>"));  
			//nameValuePairs.add(new BasicNameValuePair(string1, string2));  
			
			//httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			// Execute HTTP Post Request  
			response = httpclient.execute(httppost, res); 
			Log.d("tag", response);
			Log.d("tag2", httppost.getEntity().toString());
			
		} catch (ClientProtocolException e) {   
			e.printStackTrace();
		} catch (IOException e) {  
			e.printStackTrace();
		}
		Toast.makeText(this, "Your post was successfully uploaded", Toast.LENGTH_LONG).show();
	}   
	
	public void postData2(){
		String xml = "POST -d \"<beer_pic><pic>YEAHHHH2</pic></beer_pic>\" -H \"Content-Type: text/xml\"  http://beerwithme.heroku.com/beer_pics.xml";
		StringEntity se = null;
		try {
			se = new StringEntity(xml,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		se.setContentType("text/xml");
		HttpPost postRequest = new HttpPost("http://beerwithme.heroku.com/beer_pics.xml");
		postRequest.setEntity(se);
		
		HttpClient httpclient = new DefaultHttpClient();  
		String response=null;
		ResponseHandler <String> res=new BasicResponseHandler();
		// Execute HTTP Post Request  
		try {
			response = httpclient.execute(postRequest, res);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		Log.d("tag", response);
		Log.d("tag2", postRequest.getEntity().toString());
	}

	public void postFile(){
		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;
		DataInputStream inputStream = null;

		String pathToOurFile = "/data/data/com.BeerWithMe/Sunset.jpg";
		String urlServer = "http://beerwithme.heroku.com/";
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary =  "*****";

		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1*1024*1024;

		try
		{
			FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );

			URL url = new URL(urlServer);
			connection = (HttpURLConnection) url.openConnection();

			// Allow Inputs & Outputs
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);

			// Enable POST method
			connection.setRequestMethod("POST");

			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

			outputStream = new DataOutputStream( connection.getOutputStream() );
			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + pathToOurFile +"\"" + lineEnd);
			outputStream.writeBytes(lineEnd);

			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// Read file
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0)
			{
				outputStream.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			outputStream.writeBytes(lineEnd);
			outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// Responses from the server (code and message)
			int serverResponseCode = connection.getResponseCode();
			String serverResponseMessage = connection.getResponseMessage();

			fileInputStream.close();
			outputStream.flush();
			outputStream.close();
		}
		catch (Exception ex)
		{
			int kyle4 = 6;
			//Exception handling
		}
		int kyle5 = 6;
	}

	public void takePicture(){
		
		// create camera instance
		android.hardware.Camera cam = android.hardware.Camera.open();
		
		// grab default parameters and adjust if desired
		android.hardware.Camera.Parameters params =  cam.getParameters();
		// adjust here
		cam.setParameters(params);
		
		
	}
}
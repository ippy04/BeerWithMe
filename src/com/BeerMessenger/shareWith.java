package com.BeerMessenger;

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

import com.tools.*;

import android.app.Activity;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class shareWith extends ListActivity {

	android.hardware.Camera mCamera = null;
	byte[] mPicData;
	private String mUserId;
	private String mTargetNumber;
	private String mAvatarFileName = "testing.png";
	private String mMessage;
	private String mDefaultMessage = "Let's drink up!!!";
	private String mFriendsName;
	private String mUrl = "beermessenger.com/new_beer_pic.xml";
	private Context mCtx = this;
	private Activity mAct = this;
	private Boolean mImagePosted = false;
	private Cursor mNamesCursor = null;
	private int mTypeColumn;
	private Boolean mIsNameFormatted = false;
	private String mSecret = null;
	private String mUserName = null;
	private int mSelectedNameColor = Color.MAGENTA;
	private int mStandardColor = Color.WHITE;
	private static final int NEW_CONTACT_ID = Menu.FIRST;
	private static final int CREATE_CONTACT_REQUEST = 0;
	private int mPostAttempts = 0;
	private int mMaxPostAttempts = 3;
	private String mBaseUrl = "beermessenger.com";
	private String mPictureUrl;

	private EditText mSearchNameObj;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sharewith);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// grab pick data from global variable
		mPicData = BeerWithMeApp.camBytes;
		
		// grab user ID and beer message from global variable and password and name
		BeerWithMeApp app = (BeerWithMeApp) getApplicationContext();
		mMessage = app.getBeerMessage();
		mUserId = app.getUserID();
		mSecret = app.getSecret();
		mUserName = app.getUserName();

		// error checking
		if (mMessage==null){
			mMessage = "Null Beer Message - Should not happen";
			Log.d("shareWithDebug", "Passed beer message should not be null, please pass correctly from whatBeer");
		}
		
		if (mUserId==null){
			mUserId = "Null userId - Should not happen";
			Log.d("shareWithDebug", "Passed userId should not be null, please pass correctly from whatBeer");
		}

		// no beer name, so fill with no beer entered!!
		if (mMessage.length()==0)
			mMessage = mDefaultMessage;

		// grab handles to objects
		mSearchNameObj = (EditText) this.findViewById(R.id.searchName);
		
		// add touch listener
		mSearchNameObj.setOnTouchListener(new OnTouchListener()
	    {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (mSearchNameObj.getText().length() !=0 && mIsNameFormatted){
					
					// clear text
					mSearchNameObj.setText("");
					//mSearchNameObj.setBackgroundColor(mStandardColor);
					
					// once we clear it, then it is no longer formatted
					mIsNameFormatted = false;
				}
				return false;
			}
	    });

		// make search box searchable
		mSearchNameObj.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable arg0) {

				// once we start typing, then it is no longer formatted
				mIsNameFormatted = false;
				
				// find cursor to contacts and fill in list
				findNameCursor();
				fillNames();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}
		});
		
		// save file locally
		saveFileUsingAsync();
		
		// find cursor to contacts and fill in list
		findNameCursor();
		fillNames();
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		// move cursor to the correct position
		mNamesCursor.moveToPosition(position);
		
		// grab the display name
		String selectedName = mNamesCursor.getString(mNamesCursor.getColumnIndex(CommonDataKinds.Phone.DISPLAY_NAME));
		
		// try to grab user's first name
		long contactId = mNamesCursor.getLong(mNamesCursor.getColumnIndex(CommonDataKinds.Phone.RAW_CONTACT_ID));
		Uri uri = ContactsContract.Data.CONTENT_URI;
		String selection = ContactsContract.Data.RAW_CONTACT_ID+ " = " + contactId+
		" AND "+ ContactsContract.Data.MIMETYPE+ " = '"+ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE+"'";

		// grab cursor from search result
		Cursor cursor = mAct.managedQuery(uri, null, selection, null, null);
		if (cursor != null)
			if (cursor.moveToFirst())
				selectedName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
		
		// grab the phone number
		String phone = mNamesCursor.getString(mNamesCursor.getColumnIndex(CommonDataKinds.Phone.NUMBER));
		phone = com.tools.Tools.formatPhoneNumber(phone);

		//"Name" <123-456-7890>
		String formattedString = "To: \""+selectedName+"\" <"+phone+">";

		// send string to edit box
		mSearchNameObj.setText(formattedString);
		//mSearchNameObj.setBackgroundColor(mSelectedNameColor);
		mIsNameFormatted = true;
	}
	
	protected void onListItemClickStandard(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		mNamesCursor.moveToPosition(position);
		String selectedName = mNamesCursor.getString(mNamesCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		String selectedId = mNamesCursor.getString(mNamesCursor.getColumnIndex(ContactsContract.Contacts._ID));
		String primaryPhone = getPrimaryPhone(selectedId);
		primaryPhone = com.tools.Tools.formatPhoneNumber(primaryPhone);

		//"Name" <123-456-7890>
		String formattedString = "\""+selectedName+"\" <"+primaryPhone+">";

		// send string to edit box
		mSearchNameObj.setText(formattedString);
		//mSearchNameObj.setBackgroundColor(mSelectedNameColor);
		mIsNameFormatted = true;
		
	}

	public void goClicked1(View view){
		//TODO: managedQuery is @deprecated
		//TODO: should not be posting data with this button click

		// testing save to file
		/*
		SuccessReason success = Tools.saveByteDataToFile(this, mPicData, mMessage, false);
		if (success.getSuccess())
			Toast.makeText(this, "File Saved Locally", Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(this, "File NOT Saved Locally due to "+success.getReason(), Toast.LENGTH_LONG).show();
			*/

		// grab phone number and name
		String nameAndNumberText = mSearchNameObj.getText().toString();
		TwoObjects<String, String> nameAndNumberParsed = parseNumber(nameAndNumberText);
		mTargetNumber = com.tools.Tools.keepOnlyNumerics(nameAndNumberParsed.mObject2);
		mFriendsName = nameAndNumberParsed.mObject1;
		
		// check length of phone number
		if (mTargetNumber.length() == 0){
			Toast.makeText(this, "You must enter a phone number", Toast.LENGTH_SHORT).show();
			return;
		}
		
		// post data
		postDataUsingAsync();	 
	}
	
	private void saveFileUsingAsync(){
		new saveFileTask().execute();
	}

	/** Asynctask for saving image to file */
	private class saveFileTask extends AsyncTask<Void, Void, SuccessReason>{

		@Override
		protected void onPreExecute() {
			Toast.makeText(mCtx, "Saving Image", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected SuccessReason doInBackground(Void... arg0) {

			return Tools.saveByteDataToFile(mAct, mPicData, mMessage, false);
		}

		@Override
		protected void onPostExecute(SuccessReason result) {
			if (result.getSuccess()){
				Toast.makeText(mCtx, "Image Saved Locally", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(mCtx, "File NOT Saved Locally due to "+result.getReason(), Toast.LENGTH_LONG).show();
			}
		}
	}

	private ArrayList<String> getPhoneNumbers(String id) 
	{
		ArrayList<String> phones = new ArrayList<String>();

		Cursor cursor = this.getContentResolver().query(
				CommonDataKinds.Phone.CONTENT_URI, 
				null, 
				CommonDataKinds.Phone.CONTACT_ID +" LIKE ?", 
				new String[]{id}, null);

		while (cursor.moveToNext()) 
		{
			phones.add(cursor.getString(cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER)));
		} 

		cursor.close();
		return(phones);
	}

	private String getPrimaryPhone(String id) 
	{
		String primaryPhone = null;

		Cursor cursor = this.getContentResolver().query(
				CommonDataKinds.Phone.CONTENT_URI, 
				null, 
				CommonDataKinds.Phone.CONTACT_ID +" LIKE ?", 
				new String[]{id}, null);

		while (cursor.moveToNext()) 
		{
			if (cursor.getInt(cursor.getColumnIndex(CommonDataKinds.Phone.IS_SUPER_PRIMARY)) != 0)
				primaryPhone = cursor.getString(cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER));
		} 

		cursor.close();
		return(primaryPhone);
	}

	/** Asynctask for posting data to server */
	private class postDataTask extends AsyncTask<Void, Void, SuccessReason>{

		@Override
		protected void onPreExecute() {
			if (mPostAttempts == 0)
				Toast.makeText(mCtx, "Sending Image", Toast.LENGTH_LONG).show();
			else
				Toast.makeText(mCtx, "Sending Image Attempt #"+(mPostAttempts+1)+" of "+mMaxPostAttempts, Toast.LENGTH_LONG).show();
		}

		@Override
		protected SuccessReason doInBackground(Void... arg0) {

			// the result if successful or not
			Boolean result = true;
			
			// xml String to post to server
			String xml = "POST -d <?xml version='1.0' encoding='UTF-8'?>" +
			"<beer_pic><user><phone_number>"+mUserId+"</phone_number><password>"+
			mSecret+"</password></user><avatar type='String'>" +
			Base64.encodeToString(mPicData, Base64.DEFAULT) +
			"</avatar><avatar_file_name>"+mAvatarFileName+"</avatar_file_name>" +
			"<target_number>"+mTargetNumber+"</target_number>" +
			"<message>"+mMessage+"</message></beer_pic> " +
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

			// grab url from response
			String hash = com.tools.Tools.getXmlValueAtTag(response, "hash");
			if (hash==null){
				result = false;
			}else{
				String url = com.tools.Tools.getXmlValueAtTag(hash, "url");
				if (url==null){
					result = false;
				}else
					mPictureUrl = mBaseUrl + url;
			}

			// return result
			SuccessReason postResult = new SuccessReason(result, response);
			return postResult;
		}

		@Override
		protected void onPostExecute(SuccessReason result) {
			if (result.getSuccess()){
				Toast.makeText(mCtx, "Image Sent Successfully", Toast.LENGTH_SHORT).show();
				((BeerWithMeApp)mCtx.getApplicationContext()).addBeer(1);
			}else{
				Toast.makeText(mCtx, "Image Could Not be Posted because "+result.getReason(), Toast.LENGTH_LONG).show();
			}
			mImagePosted = result.getSuccess();
			mPostAttempts++;
			
			// failed post, so try again, but only three times
			if (!mImagePosted & mPostAttempts < mMaxPostAttempts)
				postDataUsingAsync();
			else
				mPostAttempts = 0;
			
			//send text message if we successfully posted
			if (mImagePosted){
				String intro;
				if (mFriendsName.length()==0)
					intro = "";
				else
					intro = "Hey "+mFriendsName+", ";
				sendSMS(mTargetNumber, intro+mUserName+
						" sent you a BeerMessenger Pic. Share a beer and view the picture at "+
						mPictureUrl);
			}
				
		}
	}

	// post data to server
	public void postDataUsingAsync(){
		new postDataTask().execute();
	}

	// fill list of possible name
	private void fillNames() {
		startManagingCursor(mNamesCursor);

		// Create an array to specify the fields we want to display in the list
		String[] from = new String[]{CommonDataKinds.Phone.DISPLAY_NAME, 
				CommonDataKinds.Phone.NUMBER, 
				CommonDataKinds.Phone.TYPE};//, CommonDataKinds.Phone.NUMBER};
		
		// and an array of the fields we want to bind those fields to (in this case just text1)
		int[] to = new int[]{R.id.text1, R.id.text2, R.id.text3};

		// Now create a simple cursor adapter and set it to display
		mTypeColumn = mNamesCursor.getColumnIndex(CommonDataKinds.Phone.TYPE);
		SimpleCursorAdapter notes = 
			new SimpleCursorAdapter(mCtx, R.layout.notes_row, mNamesCursor, from, to);
		setListAdapter(notes);
		notes.setViewBinder(new CustomDataViewBinder());
	}

	public class CustomDataViewBinder implements 
	SimpleCursorAdapter.ViewBinder 
	{ 
		@Override 
		public boolean setViewValue(View view, Cursor cursor, int 
				columnIndex) 
		{ 
			TextView textView = (TextView) view;
			if(columnIndex == mTypeColumn){
				switch (cursor.getInt(columnIndex)){
				case CommonDataKinds.Phone.TYPE_HOME: textView.setText("Home"); break;
				case CommonDataKinds.Phone.TYPE_MOBILE: textView.setText("Mobile"); break;
				case CommonDataKinds.Phone.TYPE_WORK_MOBILE: textView.setText("Work Mobile"); break;
				case CommonDataKinds.Phone.TYPE_WORK: textView.setText("Work"); break;
				default: textView.setText("Other"); break;
				}
				return true;
			}else
				return false;
		} 
	} 
	/** extract phone number from string that is *
	 * "name" <123-456-7890>, or if no <>, then just return the input
	 * @param input
	 * @return
	 */
	private TwoObjects<String, String> parseNumber(String input){

		// find the first < and return input if not found
		int first = input.indexOf("<");
		if (first==-1)
			return new TwoObjects<String, String>("", com.tools.Tools.formatPhoneNumber(input));

		// find the second > and return input if not found
		int second = input.indexOf(">", first+1);
		if (second == -1)
			return new TwoObjects<String, String>("", com.tools.Tools.formatPhoneNumber(input));

		// parse the name before < and number between <> and remove "" around name if present
		String name = input.substring(0, first);
		name = name.replace("\"", "");
		name = name.trim();
		return new TwoObjects<String, String>(name, 
				com.tools.Tools.formatPhoneNumber(input.substring(first+1, second)));
	}

	private void findNameCursorStandard(){
		// grab string from Box
		String srchName = mSearchNameObj.getText().toString();

		// create string for searching
		String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '1' "+
		"AND (UPPER(" + ContactsContract.Contacts.DISPLAY_NAME + ") LIKE UPPER('" + srchName + "%') OR UPPER(" 
		+ ContactsContract.Contacts.DISPLAY_NAME + ") LIKE UPPER('% " + srchName + "%')) AND "+
		ContactsContract.Contacts.HAS_PHONE_NUMBER + " = '1' ";

		// grab URI	
		Uri uri = ContactsContract.Contacts.CONTENT_URI;   

		// sort order alphabetical
		String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

		// grab cursor from search result
		mNamesCursor = mAct.managedQuery(uri, null, selection, null, sortOrder);
	}
	
	private void findNameCursor(){
		// grab string from Box
		String srchName = mSearchNameObj.getText().toString();

		// create string for searching
		String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '1' "+
		"AND (UPPER(" + CommonDataKinds.Phone.DISPLAY_NAME + ") LIKE UPPER('" + srchName + "%') OR UPPER(" 
		+ CommonDataKinds.Phone.DISPLAY_NAME + ") LIKE UPPER('% " + srchName + "%'))";

		// grab URI	  
		Uri uri = CommonDataKinds.Phone.CONTENT_URI;

		// sort order alphabetical
		String sortOrder = CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

		// grab cursor from search result
		mNamesCursor = mAct.managedQuery(uri, null, selection, null, sortOrder);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, NEW_CONTACT_ID, 0, "Create New Contact");
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case NEW_CONTACT_ID:
        		Intent intent = new Intent(Intent.ACTION_INSERT,ContactsContract.Contacts.CONTENT_URI); 
        		this.startActivityForResult(intent, CREATE_CONTACT_REQUEST); 
        		return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        
        switch(requestCode){
        case CREATE_CONTACT_REQUEST:
        	if (resultCode==RESULT_OK){
        		//TODO: add new contact to edit box
        		return;        	
        	}
        }
    }
    
    //---sends an SMS message to another device---
    private void sendSMS(String phoneNumber, String message)
    {        
    	//TODO: add check if text message is sent successfully
        PendingIntent pi = PendingIntent.getActivity(this, 0,
            new Intent(this, SmsSentClass.class), 0);                
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);        
    }
    
    private class SmsSentClass extends Activity {
    	
    	@Override
		protected void onCreate(Bundle savedInstanceState){
    		
    	}
    }
  //---sends an SMS message to another device---
    private void sendSMS2(String phoneNumber, String message)
    {        
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
 
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
            new Intent(SENT), 0);
 
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
            new Intent(DELIVERED), 0);
 
        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off", 
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));
 
        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered", 
                                Toast.LENGTH_SHORT).show();
                        break;                        
                }
            }
        }, new IntentFilter(DELIVERED));        
 
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);        
    }

}
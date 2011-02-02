package com.BeerWithMe;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
        // set the color 
        mlayout.setBackgroundColor(Color.BLUE);
    }
    
    public void shareClicked(View view) {
      //  Intent i = new Intent(this, NoteEdit.class);
      //  startActivityForResult(i, ACTIVITY_CREATE);
    	setContentView(R.layout.whatbeer);
    }
    
    public void goClicked(View view) {
        //  Intent i = new Intent(this, NoteEdit.class);
        //  startActivityForResult(i, ACTIVITY_CREATE);
      	setContentView(R.layout.sharewith);
      }
    
    public void goClicked1(View view){

    }

    public void goClicked2(View view){

    }
}
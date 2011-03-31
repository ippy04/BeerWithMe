package com.BeerMessenger;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class BeerWithMeApp extends Application {

	protected static byte[] camBytes = null;
	protected static final String PREF_FILE = "BeerWithMeApp.pref";
	protected static final String USER_ID = "userId";
	protected static final String N_BEERS_REVIEWED = "nBeersReviewed";
	protected static final String BEER_MESSAGE = "beerMessage";
	protected static final String SECRET = "secret";
	protected static final String USER_NAME = "userName";
	protected static final String ALLOW_POSTS = "allowPosts";
	protected static final String BEER_QUOTE = "beerQuote";
	protected static final String PICTURE_INFO = "pictureInfo";
	
	protected void setUserID(String user){
		SharedPreferences mSettings = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
		Editor ed = mSettings.edit();
		ed.putString(USER_ID, user);
		ed.commit();
	}
	
	protected String getUserID(){
		SharedPreferences mSettings = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
		return mSettings.getString(USER_ID, null);
	}
	
	protected void setNBeersReviewed(int beers){
		SharedPreferences mSettings = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
		Editor ed = mSettings.edit();
		ed.putInt(N_BEERS_REVIEWED, beers);
		ed.commit();
	}
	
	protected int getNBeersReviewed(){
		SharedPreferences mSettings = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
		return mSettings.getInt(N_BEERS_REVIEWED, 0);
	}
	
	protected void setBeerMessage(String message){
		SharedPreferences mSettings = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
		Editor ed = mSettings.edit();
		ed.putString(BEER_MESSAGE, message);
		ed.commit();
	}
	
	protected String getBeerMessage(){
		SharedPreferences mSettings = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
		return mSettings.getString(BEER_MESSAGE, null);
	}
	
	protected void setSecret(String secret){
		SharedPreferences mSettings = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
		Editor ed = mSettings.edit();
		ed.putString(SECRET, secret);
		ed.commit();
	}
	
	protected String getSecret(){
		SharedPreferences mSettings = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
		return mSettings.getString(SECRET, null);
	}
	
	protected void setUserName(String name){
		SharedPreferences mSettings = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
		Editor ed = mSettings.edit();
		ed.putString(USER_NAME, name);
		ed.commit();
	}
	
	protected String getUserName(){
		SharedPreferences mSettings = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
		return mSettings.getString(USER_NAME, null);
	}
	
	protected void addBeer(int inc){
		int n = getNBeersReviewed();
		setNBeersReviewed(n+inc);
	}
	
	protected void setBeerQuote(String name){
		SharedPreferences mSettings = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
		Editor ed = mSettings.edit();
		ed.putString(BEER_QUOTE, name);
		ed.commit();
	}
	
	protected String getBeerQuote(){
		SharedPreferences mSettings = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
		return mSettings.getString(BEER_QUOTE, null);
	}
}
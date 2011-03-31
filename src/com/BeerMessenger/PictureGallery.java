package com.BeerMessenger;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PictureGallery extends Activity{

	// static variables for passing intent data
	public static final String FILE_NAMES = "fileNames";
	public static final String CAPTIONS = "captions";

	// private variables
	private ArrayList<String> mFileNames; 		// list of all filesnames
	private ArrayList<String> mCaptions;		// list of captions to go with pictures
	private Activity mAct = this;
	private int mThumbNailSize = 120;			// pixel size of thumbnails

	// object views
	private ImageView mMainPictureObj = null;
	private FrameLayout mMainFrame = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) throws ExceptionInInitializerError{
		//TODO: make pixel sizes into dp
		super.onCreate(icicle);
		setContentView(R.layout.picturegallery);

		// grab extras
		Bundle extras = getIntent().getExtras();
		if (extras!=null){
			mFileNames = extras.getStringArrayList(FILE_NAMES);
			mCaptions = extras.getStringArrayList(CAPTIONS);	
			if (mFileNames == null)
				throw new ExceptionInInitializerError("must pass in File Names to view in gallery");
			if (mCaptions != null && mCaptions.size() != 0 && mCaptions.size() != mFileNames.size())
				throw new ExceptionInInitializerError("Captions and file names Arrays must be the same length");

		}else{
			throw new ExceptionInInitializerError("must pass in File Names to view in gallery");
		}

		// create a gallery, for some reason does not work in xml file, so we must do it here
		Gallery g = new Gallery(this);
		LinearLayout l = (LinearLayout) findViewById(R.id.linearPicture);
		l.addView(g, new LayoutParams(LayoutParams.FILL_PARENT, mThumbNailSize));
		g.setAdapter(new ImageAdapter(this));

		// make frame below gallery and add picture and caption to it
		initializeMainFrame();
		
		// set callback for which item is centered
		setGalleryCallback(g);
	}

	private void initializeMainFrame(){
		
		// find main linear layout
		LinearLayout l = (LinearLayout) findViewById(R.id.linearPicture);
		
		// create main frame
		mMainFrame = new FrameLayout(this);
		mMainFrame.setLayoutParams(new Gallery.LayoutParams(mThumbNailSize, LayoutParams.FILL_PARENT));
		mMainFrame.setBackgroundColor(Color.BLACK);
		l.addView(mMainFrame, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, 0, 1));
		
		// add picture frame to main frame
		mMainPictureObj = new ImageView(this);
		
		// put picture object into frame
		mMainFrame.addView(mMainPictureObj, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1));
	}
	
	private void setGalleryCallback(Gallery g){
		g.setCallbackDuringFling(true);
		g.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				// clear out any old children from frame
				mMainFrame.removeViews(0, mMainFrame.getChildCount());
				
				// grab bitmap of current object
				BitmapDrawable bitmap = new BitmapDrawable(mFileNames.get(arg2));
				Bitmap actualBitmap = bitmap.getBitmap();
				
				// if no bitmap then fill with a text object
				if (actualBitmap == null){
					TextView textUnknown = new TextView(mAct);
					textUnknown.setText("Picture Not Found. Probably Deleted");
					textUnknown.setPadding(12, 12, 12, 12);
					textUnknown.setBackgroundColor(Color.BLACK);
					textUnknown.getBackground().setAlpha(128);
					textUnknown.setTextColor(Color.WHITE);
					FrameLayout.LayoutParams params = 
						new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 
								LayoutParams.WRAP_CONTENT, 
								Gravity.CENTER);
					
					// add text to frame
					mMainFrame.addView(textUnknown, params);
				}else{
					
					// set bitmap to picture object
					mMainPictureObj.setImageBitmap(actualBitmap);
					mMainPictureObj.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
					mMainPictureObj.setBackgroundColor(Color.BLACK);
					
					// put picture object into frame
					mMainFrame.addView(mMainPictureObj);
				}      
				
				// read caption and assign to frame
				if (mCaptions != null){
					TextView t = new TextView(mAct);
					t.setText(mCaptions.get(arg2));
					t.setGravity(Gravity.CENTER_HORIZONTAL);
					t.setGravity(Gravity.BOTTOM);
					t.setPadding(12, 12, 12, 12);
					t.setBackgroundColor(Color.BLACK);
					t.getBackground().setAlpha(128);
					t.setTextColor(Color.WHITE);
					FrameLayout.LayoutParams params = 
						new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 
								LayoutParams.WRAP_CONTENT, 
								Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
					params.setMargins(0, 0, 0, 50);

					mMainFrame.addView(t, params);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// do nothing here           
			}
		});
	}
	
	public class ImageAdapter extends BaseAdapter {
		/** The parent context */
		private Context myContext;

		/** Simple Constructor saving the 'parent' context. */
		public ImageAdapter(Context c) { this.myContext = c; }

		/** Returns the amount of images we have defined. */
		public int getCount() { return mFileNames.size(); }

		/* Use the array-Positions as unique IDs */
		public Object getItem(int position) { return position; }
		public long getItemId(int position) { return position; }

		@Override
		/** Returns a new ImageView to
		 * be displayed, depending on
		 * the position passed. */
		public View getView(int position, View convertView, ViewGroup parent) {

			// create a frame layout
			FrameLayout lin = new FrameLayout(this.myContext);
			lin.setLayoutParams(new Gallery.LayoutParams(mThumbNailSize, LayoutParams.FILL_PARENT));
			lin.setPadding(5, 0, 5, 0);
			lin.setBackgroundColor(Color.BLACK);

			// read bitmap
			BitmapDrawable bitmap = new BitmapDrawable(mFileNames.get(position));
			Bitmap actualBitmap = bitmap.getBitmap();
			
			// fill with text if no bitmap
			if (actualBitmap == null){
				TextView textUnknown = new TextView(this.myContext);
				textUnknown.setText("Picture Not Found. Probably Deleted");
				textUnknown.setPadding(12, 12, 12, 12);
				textUnknown.setBackgroundColor(Color.BLACK);
				textUnknown.getBackground().setAlpha(128);
				textUnknown.setTextColor(Color.WHITE);
				FrameLayout.LayoutParams params = 
					new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 
							LayoutParams.WRAP_CONTENT, 
							Gravity.CENTER);
				lin.addView(textUnknown, params);
				
			// file with actual bitmap if we have it	
			}else{
				ImageView i = new ImageView(this.myContext);
				i.setImageBitmap(actualBitmap);
				i.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				i.setLayoutParams(new Gallery.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				i.setBackgroundColor(Color.BLACK);

				lin.addView(i);
			}

			return lin;
		}

		/** Returns the size (0.0f to 1.0f) of the views
		 * depending on the 'offset' to the center. */
		public float getScale(boolean focused, int offset) {
			/* Formula: 1 / (2 ^ offset) */
			return Math.max(0, 1.0f / (float)Math.pow(2, Math.abs(offset)));
		}
	}
}
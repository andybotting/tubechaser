/*  
 * Copyright 2010 Andy Botting <andy@andybotting.com>  
 *  
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * This file is distributed in the hope that it will be useful, but  
 * WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU  
 * General Public License for more details.  
 *  
 * You should have received a copy of the GNU General Public License  
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.  
 *  
 * This file incorporates work covered by the following copyright and  
 * permission notice:
 * 
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andybotting.tubechaser.activity;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.andybotting.tubechaser.R;
import com.andybotting.tubechaser.objects.DepartureBoard;
import com.andybotting.tubechaser.objects.NextDeparture;
import com.andybotting.tubechaser.objects.Line;
import com.andybotting.tubechaser.objects.Station;
import com.andybotting.tubechaser.provider.TfLTubeProvider;
import com.andybotting.tubechaser.provider.TubeChaserProvider;
import com.andybotting.tubechaser.provider.TubeChaserProviderException;
import com.andybotting.tubechaser.provider.TubeChaserContract.Stations;
import com.andybotting.tubechaser.utils.GenericUtil;
import com.andybotting.tubechaser.utils.PreferenceHelper;
import com.andybotting.tubechaser.utils.UIUtils;

import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class StationDetail extends ExpandableListActivity {
	private static final String TAG = "StationDetail";
    private static final boolean LOGV = Log.isLoggable(TAG, Log.DEBUG);
    
	public static final String EXTRA_LINE = "extra_line";
	
	// Menu items
	private static final int MENU_REFRESH = 0;
	private static final int MENU_STAR = 1;
	private static final int MENU_MAP = 2;

	private PreferenceHelper mPreferenceHelper;
    private TubeChaserProvider mProvider;
	private Station mStation;
	private Line mLine;
	private Context mContext;

	private ExpandableListAdapter mListAdapter;
	private ExpandableListView mListView;
	private CompoundButton mStarredButton;

	private DepartureBoard mDepartureBoard;
	private TfLTubeProvider mTfLTubeProvider;
	
	private String mErrorMessage;
	private int mErrorRetry = 0;
	private final int MAX_ERRORS = 3;
	private boolean mFirstDepartureReqest = true;
	private Uri mLineUri;
	private boolean mStarred;
	    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri stationUri = getIntent().getData();
        
        mContext = this.getBaseContext();
        mPreferenceHelper = new PreferenceHelper(this);
        mProvider = new TubeChaserProvider();
        
        mStation = mProvider.getStation(mContext, stationUri);
        mTfLTubeProvider = new TfLTubeProvider();
        mDepartureBoard = new DepartureBoard(); // just so it's not null

        // Get the line, if given in last activity
        mLineUri = getIntent().getParcelableExtra(EXTRA_LINE);
        
        // No line was passed in previous activity
        if (mLineUri == null) {
        	
        	// Get lines list
    		Uri linesUri = Stations.buildLinesUri(mStation.getUri());
    		final List<Line> lines = mProvider.getLines(mContext, linesUri);
        	
        	// If we only find one line, just use it
        	if (lines.size() == 1) {
        		mLine = lines.get(0);
        		displayStation();
        	}
        	else {
        		if (LOGV) Log.v(TAG, "Showing select dialog");
        		// Two or more lines, so show selecter
        		showLineSelect(lines);
        	}
        }
        else {
        	displayStation();
        }

    }
    
    /**
     * SHow line select dialog
     */
	public void showLineSelect(final List<Line> lines) {
		
		// Build alert dialog
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle("Select Line");

		String[] items = new String[lines.size()];
		for(int i=0; i<lines.size(); i++) {
			items[i] = lines.get(i).getName();
		}
		
		dialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	mLine = lines.get(which);
            	displayStation();
            }
        }).create();

		dialogBuilder.setCancelable(false);
		dialogBuilder.show();
	}

    /**
     * Display station
     */
    private void displayStation() {

        setContentView(R.layout.activity_station_detail);
    	
        // Set up our list
        mListAdapter = new DepartureListAdapter();
				
		mListView = getExpandableListView();
        Display display = getWindowManager().getDefaultDisplay(); 
        mListView.setIndicatorBounds(display.getWidth()-50, display.getWidth()-10);
		        
        // Get our line, if we haven't already
        if (mLine == null)
        	mLine = mProvider.getLine(mContext, mLineUri);

        
		// Set the title bar text
        ((TextView) findViewById(R.id.title_text)).setText(mLine.getLineName());
		
        // Set the title bar colour
		String lineColour = mLine.getColour();
		int colour = Color.parseColor("#" + lineColour);		
        UIUtils.setTitleBarColor(findViewById(R.id.title_container), colour);

		// Set the station text
        ((TextView) findViewById(R.id.station_name)).setText(mStation.getName());
        ((TextView) findViewById(R.id.station_lines)).setText(mLine.getLineName());

        // Get Favourite
        //mStarUri = Stations.buildStarUri(mStation.getId(), mLine.getId());
        //mStarred = mProvider.getStarred(mContext, mStarUri);
        mStarred = mPreferenceHelper.isStarred(mStation.getId(), mLine.getId());
        
        
        // Star button
        mStarredButton = (CompoundButton) findViewById(R.id.star_button);
        mStarredButton.setChecked(mStarred);

        // Star button OnClick
        mStarredButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				toggleStarred();
			}
		});        
   
        new GetDepartures().execute();
    }

    /**
     * Toggle the station 'starred' status 
     */
    public void toggleStarred() {
    	if (mStarred)
    		mPreferenceHelper.unstarStation(mStation.getId(), mLine.getId());
    	else
    		mPreferenceHelper.starStation(mStation.getId(), mLine.getId());
    	
    	mStarred = mStarred ? false : true;
    	mStarredButton.setChecked(mStarred);
    }
    
    /**
     * Create the options menu
     */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, MENU_REFRESH, 0, "Refresh");
		MenuItem menuItem1 = menu.findItem(MENU_REFRESH);
		menuItem1.setIcon(R.drawable.ic_menu_refresh);

		menu.add(0, MENU_STAR, 0, ""); // Title set in onMenuOpened()
		MenuItem menuItem2 = menu.findItem(MENU_STAR);
		menuItem2.setIcon(R.drawable.ic_menu_star);
		
		menu.add(0, MENU_MAP, 0, "Map");
		MenuItem menuItem3 = menu.findItem(MENU_MAP);
		menuItem3.setIcon(R.drawable.ic_menu_mapmode);
		
		return true;
	}

	/**
	 * Menu opened
	 */
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		// Ensure the 'Favourite' menu item has the correct text
		menu.getItem(MENU_STAR).setTitle((mStarred ? "Unfavourite" : "Favourite"));
		return super.onMenuOpened(featureId, menu);
	}

	/**
	 * Options item selected
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case MENU_REFRESH:
			// Refresh
			new GetDepartures().execute();
			return true;
		case MENU_STAR:
			// Star/Favourite
			toggleStarred();
			return true;
		case MENU_MAP:
			// Map view
	    	final Intent intent = new Intent(this, StationMap.class);
	    	intent.putExtra(StationMap.EXTRA_STATION, mStation.getUri());
	    	startActivityForResult(intent, 1);
			return true;
		}
		return false;
	}
    
	/**
	 * Update the departures list
	 */
    public void updateDeparturesList() {
		updateRefreshStatus(false);
    	setListAdapter(mListAdapter);
    	
		// Expand groups
        int size = mListAdapter.getGroupCount();
        for( int i=0; i<size; i++) {
        	mListView.expandGroup(i);
        }
    }
    
	/**
	 * On home icon click
	 */
    public void onHomeClick(View v) {
        UIUtils.goHome(this);
    }

    /**
     * On refresh icon click
     */
    public void onRefreshClick(View v) {
    	new GetDepartures().execute();
    }

    /**
     * On map icon click
     */
    public void onMapClick(View v) {
    	final Intent intent = new Intent(this, StationMap.class);
    	intent.putExtra(StationMap.EXTRA_STATION, mStation.getUri());
    	startActivityForResult(intent, 1);
    }

    /**
     * Update refresh status icon/views
     */
	private void updateRefreshStatus(boolean isRefreshing) {
		mListView.setVisibility(isRefreshing ? View.GONE : View.VISIBLE);
		findViewById(R.id.departures_loading).setVisibility(isRefreshing ? View.VISIBLE : View.GONE);
		
		findViewById(R.id.btn_title_refresh).setVisibility(isRefreshing ? View.GONE : View.VISIBLE);
		findViewById(R.id.title_refresh_progress).setVisibility(isRefreshing ? View.VISIBLE : View.GONE);
	}

	/**
	 * Get the departures as a background task 
	 */
	private class GetDepartures extends AsyncTask<DepartureBoard, Void, DepartureBoard> {
		
		@Override
		protected void onPreExecute() {
			// Show the loading spinners
			updateRefreshStatus(true);
		}

		@Override
		protected DepartureBoard doInBackground(final DepartureBoard... params) {
			try {
				mDepartureBoard = mTfLTubeProvider.getNextDepartures(mLine.getShortName(), mStation.getCode());
			} 
			catch (TubeChaserProviderException e) {
				// Retry a couple of times before error
				if (mErrorRetry < MAX_ERRORS) {
					mErrorRetry++;
					if (LOGV) Log.v(TAG, "Error " + mErrorRetry + " of " + MAX_ERRORS + ": " + e.getMessage());
					this.doInBackground(params);
				}
				else {
					// Save the error message for the toast
					mErrorMessage = e.getMessage();
				}
			}
			return mDepartureBoard;
		}

		@Override
		protected void onPostExecute(DepartureBoard mDepartureBoard) {
        	if (mErrorRetry == MAX_ERRORS) {
            	// Display a toast with the error
        		String error = "Error downloading departure information (" + mErrorMessage + ")";
        		UIUtils.popToast(mContext, error);
        		mErrorMessage = null;
        		mErrorRetry = 0;
        	}
        	else {
        		updateDeparturesList();

        		// Upload stats
        		if (mFirstDepartureReqest) {
   					if (mPreferenceHelper.isSendStatsEnabled()) {
   						new Thread() {
   							public void run() {
   								uploadStats();
   							}
   						}.start();
   					}
   					mFirstDepartureReqest = false;
        		}
        		
        	}
        	
        	// Hide the loading spinners
        	updateRefreshStatus(false);
		}
	}
	
	/**
	 * Departures list adapter
	 */
    public class DepartureListAdapter extends BaseExpandableListAdapter {
        
        public Object getChild(int groupPosition, int childPosition) {
            return mDepartureBoard.getPlatform(groupPosition).getDeparture(childPosition);
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            return mDepartureBoard.getPlatform(groupPosition).numberOfDepartures();
        }
       
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        	View pv;
            if(convertView == null) {
    			LayoutInflater inflater = getLayoutInflater();
    			pv = inflater.inflate(R.layout.departures_row, parent, false);
            }
            else {
                pv = convertView;
            }
            
            NextDeparture nextDeparture = (NextDeparture) mDepartureBoard.getPlatform(groupPosition).getDeparture(childPosition);
            
            ((TextView) pv.findViewById(R.id.departure_destination)).setText(nextDeparture.getDestination());
        	((TextView) pv.findViewById(R.id.departure_location)).setText(nextDeparture.getLocation());
            ((TextView) pv.findViewById(R.id.departure_time)).setText(nextDeparture.getTime());

			return pv;
        }

        public Object getGroup(int groupPosition) {
            return mDepartureBoard.getPlatform(groupPosition);
        }

        public int getGroupCount() {
            return mDepartureBoard.numberOfPlatforms();
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        	View pv;
            if(convertView == null) {
    			LayoutInflater inflater = getLayoutInflater();
    			pv = inflater.inflate(R.layout.departures_row_header, parent, false);
            }
            else {
                pv = convertView;
            }
            ((TextView)pv.findViewById(R.id.departure_platform)).setText(getGroup(groupPosition).toString());
            return pv;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }

    }
    
    /**
     * Upload stats
     */
    private void uploadStats() {
    	if (LOGV) Log.v(TAG, "Sending Station/Line statistics");
    	
		// gather all of the device info
    	try {
			TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			String device_uuid = tm.getDeviceId();
			String device_id = "00000000000000000000000000000000";
			if (device_uuid != null) {
				device_id = GenericUtil.MD5(device_uuid);
			}
			
			LocationManager mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
			Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	
			// post the data
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://tubechaser.andybotting.com/stats/depart/send");
			post.setHeader("Content-Type", "application/x-www-form-urlencoded");
	
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			
			pairs.add(new BasicNameValuePair("device_id", device_id));
			pairs.add(new BasicNameValuePair("station_id", String.valueOf(mStation.getId())));
			pairs.add(new BasicNameValuePair("line_id",  String.valueOf(mLine.getId())));
			
			if (location != null) {
				pairs.add(new BasicNameValuePair("latitude", String.valueOf(location.getLatitude())));
				pairs.add(new BasicNameValuePair("longitude", String.valueOf(location.getLongitude())));
				pairs.add(new BasicNameValuePair("accuracy", String.valueOf(location.getAccuracy())));
			}

			try {
				post.setEntity(new UrlEncodedFormEntity(pairs));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
	
			try {
				HttpResponse response = client.execute(post);
				response.getStatusLine().getStatusCode();
			} catch (Exception e) {
				e.printStackTrace();
			}
	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
    }
    
    
   
}

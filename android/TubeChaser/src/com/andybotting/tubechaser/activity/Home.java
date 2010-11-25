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
import com.andybotting.tubechaser.objects.Line;
import com.andybotting.tubechaser.provider.TfLTubeProvider;
import com.andybotting.tubechaser.provider.TubeChaserProvider;
import com.andybotting.tubechaser.provider.TubeChaserProviderException;
import com.andybotting.tubechaser.provider.TubeChaserContract.Stations;
import com.andybotting.tubechaser.utils.GenericUtil;
import com.andybotting.tubechaser.utils.InfoWindow;
import com.andybotting.tubechaser.utils.PreferenceHelper;
import com.andybotting.tubechaser.utils.UIUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.TextView;

public class Home extends Activity {
    
    private static final String TAG = "Home";
    private static final boolean LOGV = Log.isLoggable(TAG, Log.DEBUG);
    
	// Menu items
	private static final int MENU_ABOUT = 0;
	private static final int MENU_SEARCH = 1;
	private static final int MENU_SETTINGS = 2;
	
    private static final int UPDATE_MINS = 10;

	private PreferenceHelper mPreferenceHelper;
	private List<Line> mLines;
	private TubeChaserProvider mProvider;
	private Context mContext; 
	
    private View mInfoLoadingView;
    private View mInfoWindowView;
    private int mInfoWindowId = 0;
    
    // Seconds between Info Window changes
    private static final int INFO_CHANGE_TIME = 10;
    
    private volatile Thread mRefreshThread;
    
	private String mErrorMessage;    
	private int mErrorRetry = 0;
	private final int MAX_ERRORS = 3;

	
    // Handler for Info Window update
    Handler UpdateHandler = new Handler() {
    	public void handleMessage(Message msg) {
    		changeInfoWindow();
    	}
	};
	
	
    /**
     * Activity start
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        mInfoLoadingView = findViewById(R.id.info_window_loading);
        
        mContext = this.getBaseContext();
		mPreferenceHelper = new PreferenceHelper(this);	
        mProvider = new TubeChaserProvider();
        
        // Try to get lines to stop a FC
		try {
			mLines = mProvider.getLines(mContext);
		} catch (Exception e) {
			// Ignore
		}

        checkStats();
       
        // Show about dialog window on first launch
		if (mPreferenceHelper.isFirstLaunch())
			showAbout();
		
		long lastUpdate = mPreferenceHelper.getLastUpdateTimestamp();
        //Date lastUpdate = mProvider.getUpdateTimestamp(this);
        long timeDiff = UIUtils.dateDiff(lastUpdate);

        if (LOGV) Log.v(TAG, "Last tube status update " + timeDiff + "ms ago");
        if (timeDiff > UPDATE_MINS * 60000) {
            // 10 minutes ago
        	new GetTubeStatus().execute();
        }
        else {
        	// Start the thread for updating our info window
        	startRefreshThread();
        }

		// If we're started by the launcher, then try the default activity
		if (getIntent().hasCategory(Intent.CATEGORY_LAUNCHER))
        	goDefaultLaunchActivity();
				
    }
    
    
	public void onStop() {
		super.onStop();
		stopRefreshThread();
	}
	
	
	public void onResume() {
		super.onResume();
		startRefreshThread();
	}
    
    
    /**
     * Start the search activity
     */
    public void onSearchClick(View v) {
        UIUtils.goSearch(this);
    }

    /**
     * Start the 'Tube Overview' activity from the info window
     */
    public void onTubeStatusClick(View v) {
        // Launch overview
        startActivity(new Intent(this, LineOverview.class));
    }
    
    /**
     * Start the 'Tube Overview' activity
     */
    public void onOverviewClick(View v) {
        // Launch overview
        startActivity(new Intent(this, LineOverview.class));
    }

    /**
     * Start the 'Browse Stations' activity
     */
    public void onStationsClick(View v) {
        // Launch stations
        startActivity(new Intent(this, StationsList.class));
    }

    /**
     * Start the 'Nearby Stations' activity
     */
    public void onNearbyClick(View v) {
        // Launch stations
        startActivity(new Intent(this, StationsNearby.class));
    }

    /**
     * Start the 'Favourite' stations activity
     */
    public void onStarredClick(View v) {
        // List of favourite stations
        startActivity(new Intent(Intent.ACTION_VIEW, Stations.buildStarredUri()));
    }

    /**
     * Start the 'Settings' activity
     */
    public void onSettingsClick(View v) {
        // List of favourite stations
        startActivity(new Intent(this, Settings.class));
    }
    

    /**
     * Create the options menu
     */
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, MENU_ABOUT, 0, R.string.menu_item_about)
			.setIcon(android.R.drawable.ic_menu_help);

		menu.add(0, MENU_SETTINGS, 0, R.string.menu_item_settings)
			.setIcon(android.R.drawable.ic_menu_preferences);
		
        menu.add(0, MENU_SEARCH, 0, R.string.menu_item_search)
        	.setIcon(android.R.drawable.ic_search_category_default)
        	.setAlphabeticShortcut(SearchManager.MENU_KEY);

		return true;
	}
   
	
    /**
     * Handle a menu item being selected
     */
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ABOUT:
			showAbout();
			return true;
        case MENU_SEARCH:
        	UIUtils.goSearch(this);
            return true;
		case MENU_SETTINGS:
			Intent intent = new Intent(mContext, Settings.class);
			startActivityForResult(intent, 1);
			return true;
		}
		return false;

	}

    /**
     * Launch the default activity as set in preferences
     */
	private void goDefaultLaunchActivity(){
		String activityName = mPreferenceHelper.defaultLaunchActivity();
		Intent intent = null;
		
		// Find out chosen default activity
		if (activityName.equals("Home"))
			intent = null;
		else if (activityName.equals("Favourite"))
			intent = new Intent(Intent.ACTION_VIEW, Stations.buildStarredUri());
		else if (activityName.equals("Nearby"))
			intent = new Intent(this, StationsNearby.class);
		else if (activityName.equals("Status"))
			intent = new Intent(this, LineOverview.class);			
		if (intent != null)
			startActivity(intent);
	}

    /**
     * Show about dialog window
     */
	public void showAbout() {
		// Get the package name
		String heading = getResources().getText(R.string.app_name) + "\n";

		try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
			heading += "v" + pi.versionName + "\n\n";
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		// Build alert dialog
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle(heading);
		View aboutView = getLayoutInflater().inflate(R.layout.dialog_about, null);
		dialogBuilder.setView(aboutView);
		dialogBuilder.setPositiveButton("OK",
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// Set first launch flag in DB
					mPreferenceHelper.setFirstLaunch();
				}
			});
		dialogBuilder.setCancelable(false);
		dialogBuilder.setIcon(R.drawable.icon);
		dialogBuilder.show();
	}

    /**
     * Reload tube status data
     */
    public void onRefreshClick(View v) {
    	new GetTubeStatus().execute();
    }
    
    
    public synchronized void startRefreshThread(){
    	if(mRefreshThread == null){
    		if (LOGV) Log.v(TAG, "Starting refresh thread");
            mRefreshThread = new Thread(new InfoWindowTimer());
            mRefreshThread.setDaemon(true);
    		mRefreshThread.start();
    	}
    }

    public synchronized void stopRefreshThread(){
    	if(mRefreshThread != null){
    		if (LOGV) Log.v(TAG, "Stopping refresh thread");
    		Thread moribund = mRefreshThread;
    		mRefreshThread = null;
    		moribund.interrupt();
    	}
    }

    
    
    private class InfoWindowTimer implements Runnable {
        public void run() {
        	while(!Thread.currentThread().isInterrupted()){
        		Message m = new Message();
        		UpdateHandler.sendMessage(m);
        		try {
        			// 5 seconds
        			Thread.sleep(INFO_CHANGE_TIME * 1000);
        		} 
        		catch (InterruptedException e) {
        			Thread.currentThread().interrupt();
        		}
        	}
        }
	}
 
    public List<View> getInfoWindows() {
    	mLines = mProvider.getLines(mContext);
    	InfoWindow infoWindow = new InfoWindow(this); 
		return infoWindow.getInfoWindows(mLines);
    }
    
    
    public void changeInfoWindow() {
    	List<View> infoWindows = getInfoWindows();
    	if (infoWindows.size() > 0) {
    		if (mInfoWindowId == infoWindows.size()-1)
    			mInfoWindowId = 0;
    		else
    			mInfoWindowId++;
    		setInfoWindow(infoWindows.get(mInfoWindowId));
    	}
    }
    
    public View makeErrorInfoWindow() {
		View infoView = getLayoutInflater().inflate(R.layout.info_window, null);
		((TextView) infoView.findViewById(R.id.info_window_title)).setText("Tube Status");
		((TextView) infoView.findViewById(R.id.info_window_subtitle)).setText("Error updating Tube Status. Hit refresh to update.");
		((ImageButton) infoView.findViewById(R.id.info_window_icon)).setImageResource(R.drawable.info_window_weekend);
		return infoView;
    }
    
    
    /**
     * Make a status window
     */
    public void setInfoWindow(View infoView) {
		
        if (mInfoLoadingView == null) // Landscape orientation
        	return;
        
        ViewGroup homeRoot = (ViewGroup) findViewById(R.id.home_root);
        
        mInfoWindowView = findViewById(R.id.info_window);
        if (mInfoWindowView != null) {
            homeRoot.removeView(mInfoWindowView);
            mInfoWindowView = null;
        }
        
        mInfoWindowView = infoView;

        homeRoot.addView(infoView, new LayoutParams(
        		LayoutParams.FILL_PARENT,
                (int) getResources().getDimension(R.dimen.info_window_height)));
      
        mInfoWindowView.setVisibility(View.VISIBLE);  
        mInfoLoadingView.setVisibility(View.GONE);
    }

    /**
     * Change UI widgets when updating status data
     */
    private void updateRefreshStatus(boolean isRefreshing) {
        if (mInfoWindowView != null) {
        	mInfoWindowView.setVisibility(isRefreshing ? View.GONE : View.VISIBLE);
        	mInfoLoadingView.setVisibility(isRefreshing ? View.VISIBLE : View.GONE);
        }

        findViewById(R.id.btn_title_refresh).setVisibility(isRefreshing ? View.GONE : View.VISIBLE);
        findViewById(R.id.title_refresh_progress).setVisibility(isRefreshing ? View.VISIBLE : View.GONE);
    }
    
    /**
     * Async task for updating tube status data
     */
    private class GetTubeStatus extends AsyncTask<Void, Void, Void> {

        protected void onPreExecute() {
        	updateRefreshStatus(true);
        }

        protected Void doInBackground(Void... unused) {
        	TfLTubeProvider tubeProvider = new TfLTubeProvider();
        	try {
                if (LOGV) Log.v(TAG, "Updating Tube Status");
				tubeProvider.updateTubeStatus(mContext);
			} catch (TubeChaserProviderException e) {
			    mErrorMessage = e.getMessage();
			}
        	return null;
        }

        protected void onPostExecute(Void unused) {
        	// Display a toast with the error
        	if (mErrorMessage != null) {
        		if (mErrorRetry == MAX_ERRORS) {
        			setInfoWindow(makeErrorInfoWindow());
        			mErrorMessage = null;
        			mErrorRetry = 0;
        		}
        		else {
        			new GetTubeStatus().execute();
        		}
        		// Increment error
        		mErrorMessage = null;
        		mErrorRetry++;
        	}
        	else {
        		// Start update status timer, if not already running
        		startRefreshThread();
        	}
        	
        	updateRefreshStatus(false);
        }
    }
    
    /**
     * Check last time stats were sent, and send again if time greater than a week
     */
	private void checkStats() {	
		if (mPreferenceHelper.isSendStatsEnabled()) {
			long statsTimestamp = mPreferenceHelper.getStatsTimestamp();
	        long timeDiff = UIUtils.dateDiff(statsTimestamp);
	        
	        if (LOGV) Log.v(TAG, "Lasts stats date was " + timeDiff + "ms ago" );
			
			// Only once a week
			if (timeDiff > 604800000) {
				new Thread() {
					public void run() {
						uploadStats();
					}
				}.start();
				mPreferenceHelper.setStatsTimestamp();
			}
		}
	}
    
    /**
     * Upload statistics to remote server
     */
	private void uploadStats() {
		if (LOGV) Log.v(TAG, "Sending statistics");
		
		// gather all of the device info
		String app_version = "";
		try {
			try {
				PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
				app_version = pi.versionName;
			} catch (NameNotFoundException e) {
				app_version = "N/A";
			}

			TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			String device_uuid = tm.getDeviceId();
			String device_id = "00000000000000000000000000000000";
			if (device_uuid != null) {
				device_id = GenericUtil.MD5(device_uuid);
			}
			
			String mobile_country_code = tm.getNetworkCountryIso();
			String mobile_network_number = tm.getNetworkOperator();
			int network_type = tm.getNetworkType();
	
			// get the network type string
			String mobile_network_type = "N/A";
			switch (network_type) {
			case 0:
				mobile_network_type = "TYPE_UNKNOWN";
				break;
			case 1:
				mobile_network_type = "GPRS";
				break;
			case 2:
				mobile_network_type = "EDGE";
				break;
			case 3:
				mobile_network_type = "UMTS";
				break;
			case 4:
				mobile_network_type = "CDMA";
				break;
			case 5:
				mobile_network_type = "EVDO_0";
				break;
			case 6:
				mobile_network_type = "EVDO_A";
				break;
			case 7:
				mobile_network_type = "1xRTT";
				break;
			case 8:
				mobile_network_type = "HSDPA";
				break;
			case 9:
				mobile_network_type = "HSUPA";
				break;
			case 10:
				mobile_network_type = "HSPA";
				break;
			}
	
			String device_version = android.os.Build.VERSION.RELEASE;
	
			if (device_version == null) {
				device_version = "N/A";
			}
			
			String device_model = android.os.Build.MODEL;
			
			if (device_model == null) {
				device_model = "N/A";
			}

			String device_language = getResources().getConfiguration().locale.getLanguage();
			String home_function = mPreferenceHelper.defaultLaunchActivity();
			
			// post the data
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://tubechaser.andybotting.com/stats/app/send");
			post.setHeader("Content-Type", "application/x-www-form-urlencoded");
	
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("device_id", device_id));
			pairs.add(new BasicNameValuePair("app_version", app_version));
			pairs.add(new BasicNameValuePair("home_function", home_function));
			pairs.add(new BasicNameValuePair("device_model", device_model));
			pairs.add(new BasicNameValuePair("device_version", device_version));
			pairs.add(new BasicNameValuePair("device_language", device_language));
			pairs.add(new BasicNameValuePair("mobile_country_code", mobile_country_code));
			pairs.add(new BasicNameValuePair("mobile_network_number", mobile_network_number));
			pairs.add(new BasicNameValuePair("mobile_network_type",	mobile_network_type));

			try {
				post.setEntity(new UrlEncodedFormEntity(pairs));
			} catch (UnsupportedEncodingException e) {
				// Do nothing
			}

			try {
				HttpResponse response = client.execute(post);
				response.getStatusLine().getStatusCode();
			} catch (Exception e) {
				if (LOGV) Log.v(TAG, "Error uploading stats: " + e.getMessage());
			}

		} catch (Exception e) {
			// Do nothing
		}

	}
    

}

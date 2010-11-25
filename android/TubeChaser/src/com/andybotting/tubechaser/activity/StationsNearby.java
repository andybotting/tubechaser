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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.andybotting.tubechaser.R;
import com.andybotting.tubechaser.objects.Line;
import com.andybotting.tubechaser.objects.Station;
import com.andybotting.tubechaser.provider.TubeChaserProvider;
import com.andybotting.tubechaser.provider.TubeChaserContract.Stations;
import com.andybotting.tubechaser.utils.UIUtils;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class StationsNearby extends ListActivity implements LocationListener {

    private static final String TAG = "StationsNearby";
    private static final boolean LOGV = Log.isLoggable(TAG, Log.DEBUG);

	private final int MAX_STATIONS = 10;
	
	private LocationManager mLocationManager;
	private Location mLastKnownLocation;
	
	private ListView mListView;
	private StationsListAdapter mAdapter;
    
	private List<Station> mStations;
	//private List<TubeStation> mNearStations;
	private TubeChaserProvider mProvider;
	
	private Context mContext;	
	
	// Only show loading dialog at first load
	private boolean mShowBusy = true;
	private boolean mIsListeningForNetworkLocation;
	private boolean mIsCalculatingStationDistances;
    
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);	  
		
		mIsListeningForNetworkLocation = true;
		mIsCalculatingStationDistances = false;

		// Set the nearby stops layout
		setContentView(R.layout.activity_stations_list);
		mListView = (ListView) findViewById(android.R.id.list);

		// Get stations
		mContext = getApplicationContext();
		
        mProvider = new TubeChaserProvider();
        mStations = mProvider.getStations(mContext);
        
       	((TextView) findViewById(R.id.title_text)).setText("Nearby Stations");
       
		// Get the location
		mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		
	  	Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	  	
	  	if (LOGV) Log.v(TAG, "Location Services Enabled: " + 
	  			" Network:" + mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) +
	  			" GPS:" + mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
	  	
	  	if (location != null) {		
	  		new StationDistanceCalculator(location).execute();			
    	}
	  	else {
	  		
	  	    if ( (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) && 
	  	    		(!mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) ) {
	  	    	buildAlertNoLocationServices();
	  	    }
	  	    
	  	}
	  	
	  	
//	  	else{
//    		// TODO: This should a nicer dialog explaining that no location services
//    		UIUtils.popToast(this, "Unable to determine location!");			
//			// Finish the activity, and go back to the main menu
//			this.finish();
//    	}
	
	}
	
    private void buildAlertNoLocationServices() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You do not have GPS or Wireless network location services enabled.\n\nWould you like to enable them now?")
        .setTitle("No Location Services")
        .setCancelable(false)
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                launchGPSOptions(); 
            }
        })
        .setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
            }
        });
        final AlertDialog alert = builder.create();
        alert.show();
    }
	
	
    private void launchGPSOptions() {
        final ComponentName toLaunch = new ComponentName("com.android.settings","com.android.settings.SecuritySettings");
        final Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(toLaunch);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, 0);
    }  
	
	
	
    @Override
    protected void onStop() {
        //mSensorManager.unregisterListener(mListener);
        super.onStop();
    }
	
    @Override
    protected void onPause() {
    	super.onPause();
        stopLocationListening();
	}
    
    @Override
    protected void onResume() {
        super.onResume();
        startLocationListening(true);
        //mSensorManager.registerListener(mListener, SensorManager.SENSOR_ORIENTATION, SensorManager.SENSOR_DELAY_GAME);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    	stopLocationListening();
    }
	
	
    /** Handle "home" title-bar action. */
    public void onHomeClick(View v) {
        UIUtils.goHome(this);
    }
	
    /** Handle "search" title-bar action. */
    public void onSearchClick(View v) {
        UIUtils.goSearch(this);
    }


    protected void onListItemClick(ListView l, View v, int position, long id) {
        final Uri stationUri = mAdapter.getStationItem(position).getUri();
        final Intent intent = new Intent(Intent.ACTION_VIEW, stationUri);
        startActivity(intent);
    }
    
    
	private class StationDistanceCalculator extends AsyncTask<Station, Void, ArrayList<Station>> {
		
		private final Location mLocation;
		private boolean mRefreshListOnly;
		
		public StationDistanceCalculator(Location location){
			mLocation = location;
		}
		
		// Can use UI thread here
		protected void onPreExecute() {
			mIsCalculatingStationDistances = true;
			mRefreshListOnly = !mShowBusy;			
			if (mShowBusy) {
				// Show the dialog window
				mListView.setVisibility(View.GONE);
				findViewById(R.id.loading).setVisibility(View.VISIBLE);
				mShowBusy = false;
			}
		}
		
		// Automatically done on worker thread (separate from UI thread)
		protected ArrayList<Station> doInBackground(final Station... params) {
			ArrayList<Station> sortedStations = new ArrayList<Station>();
			SortedMap<Double, Station> sortedStationList = new TreeMap<Double, Station>();
			
	    	for(Station station : mStations){
	    		double distance = mLocation.distanceTo(station.getLocation());
	    		sortedStationList.put(distance, station);
	    	}  
	
	    	// Build a sorted list, of MAX_STATIONS stations 
	    	for(Entry<Double, Station> item : sortedStationList.entrySet()) {
	    		Station station = item.getValue();

	    		sortedStations.add(station);

				if (sortedStations.size() >= MAX_STATIONS)
	    			break;
	    	}
	    	
			// Find the lines for our nearest stations
			for(Station station : sortedStations) {
				Uri linesUri = Stations.buildLinesUri(station.getUri());
				List<Line> lines = mProvider.getLines(mContext, linesUri);
				station.setLines(lines);
			}
			
			return sortedStations;
		}    
    
		// Can use UI thread here
		protected void onPostExecute(final ArrayList<Station> sortedStations) {
		
			if(mRefreshListOnly){
				// Just update the list
				StationsListAdapter stationsListAdapter = (StationsListAdapter)getListAdapter();
				stationsListAdapter.updateStationList(sortedStations, mLocation);
			}
			else{
				// Refresh the entire list
				mAdapter = new StationsListAdapter(sortedStations, mLocation);
				setListAdapter(mAdapter);	
			}
			
			// If we've just been showing the loading screen
			if (mListView.getVisibility() == View.GONE) {
				mListView.setVisibility(View.VISIBLE);
				findViewById(R.id.loading).setVisibility(View.GONE);
			}
			
			mIsCalculatingStationDistances = false;
		}
	}    
    
    
	private class StationsListAdapter extends BaseAdapter {

		private ArrayList<Station> mStations;
		private Location mLocation;
		
		public StationsListAdapter(ArrayList<Station> stations, Location location){
			mStations = stations;
			mLocation = location;
		}
		
		public void updateStationList(ArrayList<Station> stations, Location location){
			mStations = stations;
			mLocation = location;
			this.notifyDataSetChanged();			
		}
		
		public Station getStationItem(int position) {
			return mStations.get(position);
		}
		
		public int getCount() {
			return mStations.size();
		}

		public Object getItem(int position) {
			return mStations.get(position).getId();
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View pv;
            if(convertView == null) {
    			LayoutInflater inflater = getLayoutInflater();
    			pv = inflater.inflate(R.layout.stations_nearby_row, parent, false);
            }
            else {
                pv = convertView;
            }
            
            Station thisStation = (Station) mStations.get(position);
            
            ((TextView)pv.findViewById(R.id.station_name)).setText(thisStation.getName());
            ((TextView)pv.findViewById(R.id.station_distance)).setText(thisStation.formatDistanceTo(mLocation));
            ((TextView)pv.findViewById(R.id.lines_list)).setText(thisStation.getLinesString());
            
            return pv;
			

		}
	}
	
	
    public void onLocationChanged(Location location) {

    	if (location != null) { 
    		// If this is a GPS location then ignore and unsubscribe from network location updates.
        	if(location.getProvider().equals("gps") && mIsListeningForNetworkLocation){
        		stopLocationListening();
        		startLocationListening(false);
        	}
        	
        	if(shouldCalculateNewDistance(location)){       		
        		new StationDistanceCalculator(location).execute();
        		mLastKnownLocation = location;
        	}
        	
        	if(location.hasAccuracy()){
        		((TextView) findViewById(R.id.title_text)).setText("Nearby Stations" + " (±" + (int)location.getAccuracy() + "m)");
        	}
        	else {
        		((TextView) findViewById(R.id.title_text)).setText("Nearby Stations");
        	}
        	
    	}
    }
    
    
    private boolean shouldCalculateNewDistance(Location location){
		boolean result = false;
		
    	if(mLastKnownLocation != null && mLastKnownLocation.distanceTo(location)>1) {
    		result = true;	
    	}
    	else if(mLastKnownLocation == null){
    		result = true;
    	}
    	
    	return result && (!mIsCalculatingStationDistances);
    }
    
    
    private void stopLocationListening() {
    	if (mLocationManager != null) {
    		mLocationManager.removeUpdates(this);
    	}
	}

    
    private void startLocationListening(boolean subscribeToNetworkLocation) {
    	if (mLocationManager != null) {
    		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
        	mIsListeningForNetworkLocation = subscribeToNetworkLocation;
        	
        	if(subscribeToNetworkLocation)       		
        	mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 20, this);
    	}
	}

	@Override
	public void onProviderDisabled(String provider) {}

	@Override
	public void onProviderEnabled(String provider) {}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}
		
}

	
	
	
	
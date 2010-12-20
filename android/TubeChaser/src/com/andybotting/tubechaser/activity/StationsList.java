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

import java.util.List;

import com.andybotting.tubechaser.R;
import com.andybotting.tubechaser.objects.Station;
import com.andybotting.tubechaser.provider.TubeChaserProvider;
import com.andybotting.tubechaser.provider.TubeChaserContract.Stations;
import com.andybotting.tubechaser.utils.UIUtils;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class StationsList extends ListActivity {
	
    public static final String EXTRA_LINE = "extra_line";
 
    private Uri mLineUri;
    private Uri mStationsUri;
    
	List<Station> mStations;
	Context mContext;	
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);	  

        final Intent intent = getIntent();
        mContext = getApplicationContext();
        TubeChaserProvider provider = new TubeChaserProvider();

        // Get URI
        mStationsUri = intent.getData();
        if (mStationsUri == null)
        	mStationsUri = Stations.CONTENT_URI;

        // If we're in the tab, get stations for a given line
        if (intent.hasCategory(Intent.CATEGORY_TAB)) {
            mStations = provider.getStations(mContext, mStationsUri);
            mLineUri = getIntent().getParcelableExtra(EXTRA_LINE);       	
        	setContentView(R.layout.tab_stations_list);
        } 
        else {
        	// Show non-tab layout
        	setContentView(R.layout.activity_stations_list);
        	
    		// Home button
    		findViewById(R.id.btn_title_home).setOnClickListener(new View.OnClickListener() {
    		    public void onClick(View v) {       
    		    	UIUtils.goHome(StationsList.this);
    		    }
    		});	
            		
    		// Search button
    		findViewById(R.id.btn_title_search).setOnClickListener(new View.OnClickListener() {
    		    public void onClick(View v) {
    		    	UIUtils.goSearch(StationsList.this);
    		    }
    		});
    		
    		
        	// If starred
        	if (mStationsUri.equals(Stations.buildStarredUri())) {
        		((TextView) findViewById(R.id.title_text)).setText(R.string.title_favourite_stations);
        		mStations = provider.getStarredStations(mContext);
        		if (mStations.size() == 0) {
        			alertNoFavourites();
        		}
        	}
        	else {
        		// If browsing all stations
                mStations = provider.getStations(mContext, mStationsUri);
                mLineUri = getIntent().getParcelableExtra(EXTRA_LINE);  
        		((TextView) findViewById(R.id.title_text)).setText(R.string.title_all_stations);
        	}
        		
        }
                
        ListAdapter adapter = new StationsListAdapter();
        this.setListAdapter(adapter);
	
	}
	
	
	private void alertNoFavourites() {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setMessage(R.string.dialog_no_favourites)
			.setCancelable(false)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// Action for 'Yes' Button				
					UIUtils.goSearch(StationsList.this);
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					//  Action for 'NO' Button
					dialog.cancel();				
					// TODO: getParent() instead?
					finish();
				}
			});
		
		AlertDialog alert = dialogBuilder.create();
		alert.setTitle("No Favourite Stops");
		alert.setIcon(R.drawable.appicon);
		alert.show();
	}

	
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final Uri stationUri = mStations.get(position).getUri();	
    	final Intent intent = new Intent(Intent.ACTION_VIEW, stationUri);
    	
    	// If station is starred, then we should have 1 line
    	if (mStationsUri.equals(Stations.buildStarredUri()))
    		mLineUri = mStations.get(position).getLines().get(0).getUri();
    	
    	if (mLineUri != null) 
    		intent.putExtra(StationDetail.EXTRA_LINE, mLineUri);
    	    	    	
        startActivity(intent);
    }
    
    
	private class StationsListAdapter extends BaseAdapter {

		public int getCount() {
			return mStations.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View pv;
            if(convertView == null) {
    			LayoutInflater inflater = getLayoutInflater();
    			pv = inflater.inflate(R.layout.stations_list_row, parent, false);
            }
            else {
                pv = convertView;
            }
            
            Station thisStation = (Station) mStations.get(position);
            ((TextView)pv.findViewById(R.id.station_name)).setText(thisStation.toString());
            
            if (thisStation.getLines() != null) {
            	((TextView)pv.findViewById(R.id.lines_list)).setText(thisStation.getLinesListString());
            }
            else {
            	((TextView)pv.findViewById(R.id.lines_list)).setText(thisStation.getLinesString());
            }
            	
            return pv;
		}
	}
}

	
	
	
	
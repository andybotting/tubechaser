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

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
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

public class Search extends ListActivity {

	List<Station> mStations;
	Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mContext = getApplicationContext();
        onNewIntent(getIntent());
        
        Intent intent = getIntent();
        
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // Handle the normal search query case
            String query = intent.getStringExtra(SearchManager.QUERY);
            final CharSequence title = getString(R.string.title_search_query, query);
            ((TextView) findViewById(R.id.title_text)).setText(title);

            TubeChaserProvider provider = new TubeChaserProvider();
            final Uri stationsUri = Stations.buildSearchUri(query);
            mStations = provider.getStations(mContext, stationsUri);
            ListAdapter adapter = new StationsListAdapter();
            setListAdapter(adapter);
        } 
        else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // Handle a suggestions click (because the suggestions all use ACTION_VIEW)
            String stationId = intent.getDataString();
        	Uri uri = Stations.CONTENT_URI.buildUpon().appendPath(stationId).build();
            intent = new Intent(Intent.ACTION_VIEW, uri);	
            startActivity(intent);
            finish();
        }
        
        
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
        final Uri stationUri = mStations.get(position).getUri();	
    	final Intent intent = new Intent(Intent.ACTION_VIEW, stationUri);
        //intent.putExtra(StationDetail.EXTRA_LINE, mLineUri);
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
            ((TextView)pv.findViewById(R.id.lines_list)).setText(thisStation.getLinesString());
            return pv;
			

		}
	}
    
    
    

}
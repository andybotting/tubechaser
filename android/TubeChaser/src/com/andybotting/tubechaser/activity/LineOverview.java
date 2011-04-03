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

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.andybotting.tubechaser.R;
import com.andybotting.tubechaser.objects.Line;
import com.andybotting.tubechaser.provider.TfLTubeLineStatus;
import com.andybotting.tubechaser.provider.TubeChaserProvider;
import com.andybotting.tubechaser.provider.TubeChaserContract.Lines;
import com.andybotting.tubechaser.utils.PreferenceHelper;
import com.andybotting.tubechaser.utils.UIUtils;

import com.andybotting.tubechaser.provider.TubeChaserProviderException;


public class LineOverview extends ListActivity {
	
    private static final String TAG = "TubeChaser";
    private static final boolean LOGV = Log.isLoggable(TAG, Log.INFO);
    
    private static final int UPDATE_MINS = 10;

	private ListAdapter mListAdapter;
	private List<Line> mLines;
	private Context mContext;
	private TubeChaserProvider mProvider;
	private PreferenceHelper mPreferenceHelper;
	
	private String mErrorMessage;
	private int mErrorRetry = 0;
	private final int MAX_ERRORS = 3;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_overview);

		// Home button
		findViewById(R.id.btn_title_home).setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {       
		    	UIUtils.goHome(LineOverview.this);
		    }
		});	
		
		// Refresh button
		findViewById(R.id.btn_title_refresh).setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {       
		    	new GetTubeStatus().execute();
		    }
		});	
        		
		// Search button
		findViewById(R.id.btn_title_search).setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	UIUtils.goSearch(LineOverview.this);
		    }
		});
     
        mContext = this.getBaseContext();
        mPreferenceHelper = new PreferenceHelper();
        mProvider = new TubeChaserProvider();
        
        long lastUpdate = mPreferenceHelper.getLastUpdateTimestamp();
        long timeDiff = UIUtils.dateDiff(lastUpdate);
       
        if (timeDiff > UPDATE_MINS * 60000) {
            // 10 minutes
        	new GetTubeStatus().execute();
        }
        
        updateLinesOverview();
    }
    
    public void updateLinesOverview() {
    	mLines = mProvider.getLines(mContext);
    	
    	mListAdapter = new LinesListAdapter();
        setListAdapter(mListAdapter);
        
        long lastUpdate = mPreferenceHelper.getLastUpdateTimestamp();
        long timeDiff = UIUtils.dateDiff(lastUpdate);
        ((TextView) findViewById(R.id.title_text)).setText(UIUtils.timeString(timeDiff));
    }

    
    private void updateRefreshStatus(boolean isRefreshing) {
    	findViewById(R.id.btn_title_refresh).setVisibility(isRefreshing ? View.GONE : View.VISIBLE);
        findViewById(R.id.title_refresh_progress).setVisibility(isRefreshing ? View.VISIBLE : View.GONE);
    }
    
    private class GetTubeStatus extends AsyncTask<Void, Void, Void> {

        protected void onPreExecute() {
        	((TextView) findViewById(R.id.title_text)).setText("Updating...");
        	updateRefreshStatus(true);
        }

        protected Void doInBackground(Void... unused) {
        	TfLTubeLineStatus tubeLineStatus = new TfLTubeLineStatus();
        	try {
                if (LOGV) Log.v(TAG, "Updating Tube Status");
                tubeLineStatus.updateTubeStatus(mContext);
			} catch (TubeChaserProviderException e) {
			    mErrorMessage = e.getMessage();
			}
        	return null;
        }

        protected void onPostExecute(Void unused) {        	
        	if (mErrorMessage != null) {
        		// Update error
        		((TextView) findViewById(R.id.title_text)).setText("Update Error");
        		
        		// If we've had MAX_ERROR number of errors
        		if (mErrorRetry == MAX_ERRORS) {
            		String error = "Error downloading Tube status information (" + mErrorMessage + ")";
            		UIUtils.popToast(mContext, error);          		
            		mErrorMessage = null;
            		mErrorRetry = 0;
        		}
        		else {
        			new GetTubeStatus().execute();
        		}
        		// Increment the error count
        		mErrorMessage = null;
        		mErrorRetry++;
        	}
        	else {
        		// Success updating, show lines
        		updateLinesOverview();
        	}
        	
        	// Hide the update spinner
        	updateRefreshStatus(false);
        }
    }
   
    
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // Start details activity for selected item
        Uri lineUri = ContentUris.withAppendedId(Lines.CONTENT_URI, id);
        Intent intent = new Intent(this, LineDetail.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(lineUri);
        startActivity(intent);
    }
    

	private class LinesListAdapter extends BaseAdapter {

		public int getCount() {
			return mLines.size();
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
    			pv = inflater.inflate(R.layout.lines_list_row, parent, false);
            }
            else {
                pv = convertView;
            }
            
			Line line = (Line) mLines.get(position);
			
            ((TextView)pv.findViewById(R.id.line_name)).setText(line.getLineName());
            
			// Parse our 6-char hex value into an int for android
			int colour = Color.parseColor("#" + line.getColour());
			((View)pv.findViewById(R.id.line_colour)).setBackgroundColor(colour);

			// Status message e.g. Good service
            ((TextView)pv.findViewById(R.id.line_status)).setText(line.getStatus());
            
            // Set the green/orange/red/gray status icon
            int imageID = getResources().getIdentifier(line.getStatusImage(), "drawable", "com.andybotting.tubechaser");
            ((ImageView)pv.findViewById(R.id.line_status_image)).setImageResource(imageID);
            
            return pv;
			

		}
	}
    
}

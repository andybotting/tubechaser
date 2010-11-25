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

import com.andybotting.tubechaser.R;
import com.andybotting.tubechaser.objects.Line;
import com.andybotting.tubechaser.provider.TubeChaserProvider;
import com.andybotting.tubechaser.provider.TubeChaserContract.Lines;
import com.andybotting.tubechaser.utils.UIUtils;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

public class LineDetail extends TabActivity {

	// Tab definitions
	public static final String TAB_STATUS = "status";
    public static final String TAB_STATIONS = "stations";

	private Line mLine;
	private Context mContext;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_detail);

        Uri uri = getIntent().getData();
        
        mContext = this.getBaseContext();
        TubeChaserProvider provider = new TubeChaserProvider();
        
        mLine = provider.getLine(mContext, uri);

		// Set the title bar text
        ((TextView) findViewById(R.id.title_text)).setText(mLine.getLineName());
		
        // Set the title bar colour
		String lineColour = mLine.getColour();
		int colour = Color.parseColor("#" + lineColour);		
        UIUtils.setTitleBarColor(findViewById(R.id.title_container), colour);
        
        // Setup the tabs
        setupStatusTab();
        setupStationsTab();
        
        // If it's good service, then use the status as the desc
        String statusDesc = mLine.getStatusDesc();

        if ( (statusDesc == null) || (statusDesc.length() < 2) ) {
        	((TextView) findViewById(R.id.line_status)).setText(mLine.getStatus());
        	getTabHost().setCurrentTabByTag(TAB_STATIONS);
        }
        else {
        	((TextView) findViewById(R.id.line_status)).setText(mLine.getStatusDesc());
        	getTabHost().setCurrentTabByTag(TAB_STATUS);
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

    private void setupStatusTab() {
        final TabHost host = getTabHost();
        // 
        host.addTab(host.newTabSpec(TAB_STATUS)
                .setIndicator(buildIndicator(R.string.description_status)) 
                .setContent(R.id.tab_line_status));
    }
    
    private void setupStationsTab() {
        final TabHost host = getTabHost();
        
        final Uri lineUri = getIntent().getData();
        final Uri stationsUri = Lines.buildStationsUri(lineUri);
        
        final Intent intent = new Intent(Intent.ACTION_VIEW, stationsUri);
        intent.putExtra(StationsList.EXTRA_LINE, lineUri);
        intent.addCategory(Intent.CATEGORY_TAB);

        host.addTab(host.newTabSpec(TAB_STATIONS)
                .setIndicator(buildIndicator(R.string.description_stations))
                .setContent(intent));
    }
    
    private View buildIndicator(int textRes) {
        final TextView indicator = (TextView) getLayoutInflater().inflate(R.layout.tab_indicator, getTabWidget(), false);
        indicator.setText(textRes);
        return indicator;
    }
    
}

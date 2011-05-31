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

package com.andybotting.tubechaser.utils;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.andybotting.tubechaser.R;
import com.andybotting.tubechaser.objects.Line;

public class InfoWindow {

	private final Activity mActivity;

	public InfoWindow(final Activity mActivity) {
		this.mActivity = mActivity;
	}
	
	public List<View> getInfoWindows(List<Line> lines) {
		
		List<View> infoViews = new ArrayList<View>();
		
		// List of possible status windows
		List<Line> goodService = new ArrayList<Line>();
    	List<Line> severeDelays = new ArrayList<Line>();
    	List<Line> minorDelays = new ArrayList<Line>();
		List<Line> suspended = new ArrayList<Line>();
		List<Line> closures = new ArrayList<Line>();
		
		// Info window items
		String statusMessage;
		String statusTitle;
		int statusImage;
		
			
    	for (int i=0; i < lines.size(); i++) {
    		
    		Line line = lines.get(i); 
    		
    		if (line.getStatus().contains("Severe")) {
    			severeDelays.add(line);
    		}
    		else if (line.getStatus().contains("Minor")) {
    			minorDelays.add(line);
    		}
    		else if (line.getStatus().contains("losure")) {
    			closures.add(line);
    		}
    		else if (line.getStatus().contains("uspended")) {
    			suspended.add(line);
    		}
    		else if (line.getStatus().contains("Closed")) {
    			if (line.getId() == Line.LINE_WATERLOO_AND_CITY) {
    				// If this is Waterloo and City, we'll just remove it completely
    				// so goodService.size() == lines.size() on nights and weekends
    				lines.remove(i);
    			}
    			else {
    				closures.add(line);
    			}
    		}
    		else if (line.getStatus().contains("Good")) {
    			goodService.add(line);
    		}
    	} 

		
		if (goodService.size() == lines.size()) {
			// All lines have good service!
			statusTitle = "Good Service";
			statusImage = R.drawable.info_window_good;
			statusMessage = "There is a Good Service on all lines.";
			infoViews.add(buildView(statusTitle, statusMessage, statusImage));
		}

		else {
			if (goodService.size() > lines.size()-2 ) {
				// If we have most lines good service
				statusTitle = "Good Service";
				statusImage = R.drawable.info_window_good;
				statusMessage = "There is a " + statusTitle + " on all other lines.";
				infoViews.add(buildView(statusTitle, statusMessage, statusImage));				
			}
			if (!severeDelays.isEmpty()) {
				statusTitle = "Severe Delays";
				statusImage = R.drawable.info_window_severe;
				statusMessage = buildLines(severeDelays) + (severeDelays.size() > 1 ? " have " : " has ") + "severe delays.";	
				infoViews.add(buildView(statusTitle, statusMessage, statusImage));
			}
			if (!minorDelays.isEmpty()) {
				statusTitle = "Minor Delays";
				statusImage = R.drawable.info_window_minor;
				statusMessage = buildLines(minorDelays) + (minorDelays.size() > 1 ? " have " : " has ") + "minor delays.";
				infoViews.add(buildView(statusTitle, statusMessage, statusImage));
			}
			if (!suspended.isEmpty()) {
				statusTitle = "Suspended";
				statusImage = R.drawable.info_window_closure;
				statusMessage = buildLines(suspended) + (suspended.size() > 1 ? " are " : " is ") + "suspended.";
				infoViews.add(buildView(statusTitle, statusMessage, statusImage));
			}			
			if (!closures.isEmpty()) {
				statusTitle = "Line Closures";
				statusImage = R.drawable.info_window_closure;
				statusMessage = "Closures on the " + buildLines(closures) + ".";
				infoViews.add(buildView(statusTitle, statusMessage, statusImage));
			}
		}
		return infoViews;
	}

	private View buildView(String statusTitle, String statusMessage, int statusImage) {
		
		View infoView = mActivity.getLayoutInflater().inflate(R.layout.info_window, null);
		
		((TextView) infoView.findViewById(R.id.info_window_title)).setText(statusTitle);
		((TextView) infoView.findViewById(R.id.info_window_subtitle)).setText(statusMessage);
		((ImageButton) infoView.findViewById(R.id.info_window_icon)).setImageResource(statusImage);

		return infoView;
	}
	
	
	
	
    /**
     * Build a string of lines from a given list
     */
	private String buildLines(List<Line> lines) {
		
		String linesString = "";
		
		for(int i=0; i < lines.size(); i++) {
			Line l = lines.get(i);
	
			linesString += l.getName();
			if (i < lines.size() -2) {
				linesString += ", ";
			}
			else if (i == lines.size() -2){
				linesString += " and ";
			}
		}
		
		if (lines.size() > 1)
			linesString += " Lines";
		else
			linesString += " Line";
		
		return linesString;
		
	}
	

}

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

package com.andybotting.tubechaser.provider;

import java.io.IOException;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.andybotting.tubechaser.objects.DepartureBoard;
import com.andybotting.tubechaser.objects.NextDeparture;
import com.andybotting.tubechaser.objects.Platform;
import com.andybotting.tubechaser.objects.Line;
import com.andybotting.tubechaser.objects.Station;
import com.andybotting.tubechaser.provider.TubeChaserContract.LinesColumns;
import com.andybotting.tubechaser.provider.TubeChaserContract.StationsColumns;
import com.andybotting.tubechaser.provider.TubeChaserProviderException;
import com.andybotting.tubechaser.utils.PreferenceHelper;

public class TfLTubeProvider {
	
    private static final String TAG = "TfLTubeProvider";
    private static final boolean LOGV = Log.isLoggable(TAG, Log.DEBUG);
	
    Pattern pattern;
    Matcher matcher;
    
	List<Line> mLines;
	List<Station> mStations;
	Context mContext;

	final String userAgent = "Mozilla/5.0 (X11; U; Linux x86_64; en-GB; rv:1.9.2.11) Gecko/20101013 Ubuntu/10.10 (maverick) Firefox/3.6.11";
	final String tubeStatusUrl = "http://www.tfl.gov.uk/tfl/livetravelnews/realtime/tube/default.html";
	final String departureBoardUrl = "http://www.tfl.gov.uk/tfl/livetravelnews/departureboards/tube/default.asp?LineCode=%s&StationCode=%s";
	
	
	public void updateTubeStatus (Context context) throws TubeChaserProviderException {
		
		mContext = context;
		TubeChaserProvider provider = new TubeChaserProvider();
		PreferenceHelper preferenceHelper = new PreferenceHelper(mContext);	
		
	    try {
	    	
	    	if (LOGV) Log.v(TAG, "Fetching (url=" + tubeStatusUrl + ")");
	    	
	        Document doc = Jsoup.connect(tubeStatusUrl).userAgent(userAgent).get();
	        
	        Elements lines = doc.select("li.ltn-line");
            mLines = provider.getLines(mContext);
	
	        for (Element line : lines) {
	        	
	        	// Return the first 'class' object to match our name
	        	// <h3 class="hammersmithandcity ltn-name">H'smith &amp; City</h3>
	        	String statusLineCode = line.getElementsByTag("h3").attr("class").split(" ")[0];
	        	
	        	// Look for a h4.ltn-title element
	        	// <div class="status problem">
	        	//  <h4 class="ltn-title">Part suspended, Severe delays</h4> 
	        	String status = line.select("div.status").first().getElementsByClass("ltn-title").text();           	
	        	if (status.matches("")) {
	        		// <div class="status">Good service</div>
	        		status = line.select("div.status").first().ownText();
	        	}
	        	            	
	            // <div class="message">
	            //  <p>Suspended between blah blah.</p>
	        	String statusDesc = line.select("div.message").select("p").toString();
        	
	        	for (Line thisLine : mLines) {
	        		// Use contains() to we can match hammersmith (db) to hammersmithandcity (tfl)
	        		if (statusLineCode.contains(thisLine.getShortName())) {
	        			
	        			if (LOGV) Log.v(TAG, "Found status (line=" + thisLine.getName() + " status=" + status);
	        			
	        			Uri uri = thisLine.getUri();
	        			ContentValues values = new ContentValues();
	        			values.put(LinesColumns.STATUS, status);
	        			values.put(LinesColumns.STATUS_DESC, statusDesc);
	        			String where = BaseColumns._ID + " = " + thisLine.getId();
	        			// Perform DB update
	        			context.getContentResolver().update(uri, values, where, null);
	        		}
	        	}
	        }

        	if (mLines.size() > 8) {
        		// Set the last update timestamp
        		preferenceHelper.setLastUpdateTimestamp();
        	}

	        
	        
	    	Elements stations = doc.select("li.ltn-station");

            mStations = provider.getStations(mContext);
		    
		    for (Element station : stations) {
		    
		    	String statusStation = station.select("h4.ltn-name").first().ownText();
		    	String statusNews = station.select("div.message").select("p").text();

	        	for (Station thisStation : mStations) {
	        		if (statusStation.contains(thisStation.getName())) {
	        			Uri uri = thisStation.getUri();
	        			ContentValues values = new ContentValues();	        			
	        			values.put(StationsColumns.NEWS, statusNews);
	        			String where = BaseColumns._ID + " = " + thisStation.getId();	
	        			// Perform DB update
	        			context.getContentResolver().update(uri, values, where, null);
	        			
	        			if (LOGV) Log.v(TAG, "Found news (station=" + thisStation.getName() + ")");
	        		}
	        	}
		    }

	    }

	    catch (IOException e) {
    		throw new TubeChaserProviderException(e.getMessage());
    	}


	}
	
	
	public DepartureBoard getNextDepartures (String lineCode, String stationCode) throws TubeChaserProviderException {	
	
		String url = String.format(departureBoardUrl, lineCode, stationCode);
		if (LOGV) Log.v(TAG, "Fetching (url=" + url);
		
		DepartureBoard departureBoard = new DepartureBoard();
		
	    try {
	       	url = String.format(url);
	       	Document doc = Jsoup.connect(url).userAgent(userAgent).get();
            
            //<p class='timestamp'>Last updated  11:03:40 |
            String timestamp = doc.select("p.timestamp").first().ownText();
            pattern = Pattern.compile("([0-9]{2}:[0-9]{2}:[0-9]{2})");
            matcher = pattern.matcher(timestamp);
            
    		Date date = new Date();

	        if (matcher.find()) {
	            String match = matcher.group();  // 11:03:40
    			String[] words = match.split(":");
    			date.setHours(Integer.parseInt(words[0]));
    			date.setMinutes(Integer.parseInt(words[1]));
    			date.setSeconds(Integer.parseInt(words[2]));
    			departureBoard.setLastUpdated(date);
	        }
	        
        
	
	        Elements tables = doc.select("table.board");
            for (Element table : tables) {

            	Platform platform = new Platform();
            	           	
            	// Westbound - Platform 1
            	String header = table.getElementsByTag("caption").text();
            	
                pattern = Pattern.compile("Platform (\\d+)");
                matcher = pattern.matcher(header);
                
    	        // Find the next occurrence
    	        if (matcher.find()) {
    	        	
    	        	// Regex to find the platform number
                	platform.setPlatformNumber(matcher.group(1)); // Platform (3)
    	            
    	            // Remove 'Platform X' which we matched before from header
    	            header = header.replace(matcher.group(0), "");          
    	            // Remove the ' - ' string
    	            header = header.replace(" - ", "");
    	            
    	            platform.setDirection(header);
    	        }
            	
            	
            	//1. Ealing Broadway / At Earl's Court Platform 4 / 3 mins 
            	Elements dests = table.select("td.destination");
            	Elements locations = table.select("td.message");
            	Elements times = table.select("td.time");

            	for (int i=0; i < dests.size(); i++) {
            		NextDeparture nextDeparture = new NextDeparture();

            		// Split the 1. from Ealing Broadway
            		String destination = dests.get(i).text().split("\\. ")[1];
            		nextDeparture.setDestination(destination);

            		nextDeparture.setLocation(locations.get(i).text());
            		nextDeparture.setTime(times.get(i).text());
            		
            		platform.addNextDeparture(nextDeparture);

            	}

            	if (LOGV) Log.v(TAG, "Found Platform: " + platform);
            	departureBoard.addPlatform(platform);
        		
            }
           

	    }
	    catch (IOException e) {
    		throw new TubeChaserProviderException(e.getMessage());
    	}
	    
	    return departureBoard;
	
	}
	
}

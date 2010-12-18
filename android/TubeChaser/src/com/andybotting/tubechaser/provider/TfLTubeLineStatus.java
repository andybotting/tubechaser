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

import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.andybotting.tubechaser.objects.Line;
import com.andybotting.tubechaser.provider.TubeChaserContract.LinesColumns;
import com.andybotting.tubechaser.utils.PreferenceHelper;
import com.andybotting.tubechaser.provider.TubeChaserProviderException;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class TfLTubeLineStatus {
	
    private static final String TAG = "TfLTubeLineStatus";
    private static final boolean LOGV = Log.isLoggable(TAG, Log.INFO);

	private static final String LINES_STATUS_URL = "http://cloud.tfl.gov.uk/TrackerNet/LineStatus";
    
	public void updateTubeStatus (Context context) throws TubeChaserProviderException {
	
		TubeChaserProvider provider = new TubeChaserProvider();
		PreferenceHelper preferenceHelper = new PreferenceHelper(context);	

		try {
			
			// Update Lines
			URL url = new URL(LINES_STATUS_URL);
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(url.openStream()));
			doc.getDocumentElement().normalize();
	
			List<Line> lines = provider.getLines(context);
			
			NodeList nodeList = doc.getElementsByTagName("LineStatus");
	
			for (int i=0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);

				Element lineStatusElement = (Element) node;
				String statusDetails = lineStatusElement.getAttribute("StatusDetails"); // StatusDetails="Due to earlier faulty trains."
				
				// Line
				NodeList lineList = lineStatusElement.getElementsByTagName("Line");
				Element nameElement = (Element) lineList.item(0);
				
				String lineID = nameElement.getAttribute("ID");      // Line ID="4"
				String lineName = nameElement.getAttribute("Name");	 // Name="Jubilee"
				
				// Status
				NodeList statusList = lineStatusElement.getElementsByTagName("Status");
				Element statusElement = (Element) statusList.item(0);
				
				String statusCode = statusElement.getAttribute("ID");           // ID="MD"
				String status = statusElement.getAttribute("Description");      // Description="Minor Delays"
				String statusClass = statusElement.getAttribute("CssClass");    // CssClass="GoodService"
				String statusIsActive = statusElement.getAttribute("IsActive"); // IsActive="true"
				
				// Loop through our lines, until we find a match
				for (Line line : lines) {
					if (lineID.matches(line.getTfLID())) {
						if (LOGV) Log.v(TAG, "Found status (line=" + line.getName() + " status=" + status);
						
		    			Uri uri = line.getUri();
		    			
		    			ContentValues values = new ContentValues();
		    			values.put(LinesColumns.STATUS, status);
		    			values.put(LinesColumns.STATUS_DESC, statusDetails);
		    			values.put(LinesColumns.STATUS_CLASS, statusClass);
		    			values.put(LinesColumns.STATUS_CODE, statusCode);
		    			String where = BaseColumns._ID + " = " + line.getId();
		    			context.getContentResolver().update(uri, values, where, null); // Update the DB entry
					}
				}
	
			}
		
			// Update our timestamp
			if (nodeList.getLength() > 5) {
				preferenceHelper.setLastUpdateTimestamp();
			}

		} 
		catch (Exception e) {
			//System.out.println("XML Pasing Excpetion = " + e);
			throw new TubeChaserProviderException(e.getMessage());
		}
		
	}

}

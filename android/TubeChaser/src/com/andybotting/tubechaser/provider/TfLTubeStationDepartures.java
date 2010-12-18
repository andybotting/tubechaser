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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.andybotting.tubechaser.objects.DepartureBoard;
import com.andybotting.tubechaser.objects.NextDeparture;
import com.andybotting.tubechaser.objects.Platform;

import android.util.Log;

public class TfLTubeStationDepartures {
	
    private static final String TAG = "TfLTubeLineStatus";
    private static final boolean LOGV = Log.isLoggable(TAG, Log.INFO);

	private static final String DEPARTURES_URL = "http://cloud.tfl.gov.uk/TrackerNet/PredictionDetailed/%s/%s";
    
	public DepartureBoard getNextDepartures (String lineCode, String stationCode) throws TubeChaserProviderException {	
		 
		// <?xml version="1.0" encoding="utf-8"?>
		// <ROOT xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns="http://trackernet.lul.co.uk">
		//   <WhenCreated>14 Dec 2010 16:48:55</WhenCreated>
		//   <Line>D</Line>
		//   <LineName>District Line</LineName>
		//   <S Code="WKN" Mess="" N="West Kensington." CurTime="16:48:55">
		//     <P N="Westbound - Platform 1" Num="1" TrackCode="TDBT-598WEK">
		//       <T LCID="1286329" SetNo="021" TripNo="0" SecondsTo="60" TimeTo="1:30" Location="Between Earl's Court and West Kensington" Destination="Richmond" DestCode="374" Order="0" DepartTime="16:48:43" DepartInterval="60" Departed="0" Direction="0" IsStalled="0" TrackCode="TDBQ-BQQ-BZWEK" LN="D" />
		//       <T LCID="1287963" SetNo="000" TripNo="0" SecondsTo="353" TimeTo="6:00" Location="Between South Kensington and Gloucester Road" Destination="Ealing Broadway" DestCode="116" Order="0" DepartTime="16:48:47" DepartInterval="353" Departed="0" Direction="0" IsStalled="0" TrackCode="TDHVGLR_2" LN="D" />
		//     </P>
		//     <P N="Eastbound - Platform 2" Num="2" TrackCode="TDDQWEK">
		//       <T LCID="1288317" SetNo="124" TripNo="0" SecondsTo="113" TimeTo="2:00" Location="Approaching Barons Court" Destination="Tower Hill" DestCode="432" Order="0" DepartTime="16:48:41" DepartInterval="113" Departed="0" Direction="0" IsStalled="0" TrackCode="TD591AB" LN="D" />
		//       <T LCID="1287793" SetNo="014" TripNo="0" SecondsTo="365" TimeTo="6:00" Location="Between Stamford Brook and Ravenscourt Park" Destination="Upminster" DestCode="440" Order="0" DepartTime="16:48:52" DepartInterval="365" Departed="0" Direction="0" IsStalled="0" TrackCode="TP577C.579ABCD" LN="D" />
		//     </P>
		//   </S>
		// </ROOT>		 
		try {	
			 
			DepartureBoard departureBoard = new DepartureBoard();		
			URL url = new URL(String.format(DEPARTURES_URL, lineCode, stationCode));
				
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(url.openStream()));
			doc.getDocumentElement().normalize();
		
			// Station
			// <S Code="WKN" Mess="" N="West Kensington." CurTime="16:48:55">
			NodeList stationNodeList = doc.getElementsByTagName("S");
			Node stationNode = stationNodeList.item(0);
		 	Element stationElement = (Element) stationNode;
		 	
		 	String currentTime = stationElement.getAttribute("CurTime");  // "16:48:55"
		
		 	// Platform
		 	// <P N="Eastbound - Platform 2" Num="2" TrackCode="TDDQWEK">
			NodeList platformNodeList = stationElement.getElementsByTagName("P");
					
			for (int j = 0; j < platformNodeList.getLength(); j++) {
				
				Platform platform = new Platform();
				
				Node platformNode = platformNodeList.item(j);
					
				Element platformElement = (Element) platformNode;
				String platformName = platformElement.getAttribute("N");
				String platformMessage = platformElement.getAttribute("Mess");
				String platformNumber = platformElement.getAttribute("Num");
				
				platform.setPlatformNumber(platformNumber);
				
                Pattern pattern = Pattern.compile("Platform (\\d+)");
                Matcher matcher = pattern.matcher(platformName);
                
    	        // Find the next occurrence
    	        if (matcher.find()) {
    	            
    	            // Remove 'Platform X' which we matched before from header
                	platformName = platformName.replace(matcher.group(0), "");
                	
    	            // Remove the ' - ' from the string
    	            platformName = platformName.replace(" - ", "");
    	            
    	            // Should be left with 'Eastbound'
    	            platform.setDirection(platformName);
    	        }
                
				// Departure (Train)
				// <T LCID="1288317" SetNo="124" TripNo="0" SecondsTo="113" TimeTo="2:00" 
				// 		Location="Approaching Barons Court"	Destination="Tower Hill" DestCode="432" 
				// 		Order="0" DepartTime="16:48:41" DepartInterval="113" Departed="0" 
				// 		Direction="0" IsStalled="0" TrackCode="TD591AB" LN="D" />
				NodeList departureNodeList = platformElement.getElementsByTagName("T");
				
				for (int k = 0; k < departureNodeList.getLength(); k++) {
					
					
					
					Node departureNode = departureNodeList.item(k);
					Element departureElement = (Element) departureNode;
					
					String departureLCID = departureElement.getAttribute("LCID");
					String departureSetNo = departureElement.getAttribute("SetNo");
					String departureTripNo = departureElement.getAttribute("TripNo");
					String departureSecondsTo = departureElement.getAttribute("SecondsTo");
					String departureTimeTo = departureElement.getAttribute("TimeTo");
					String departureLocation = departureElement.getAttribute("Location");
					String departureDestination = departureElement.getAttribute("Destination");
					String departureDestCode =  departureElement.getAttribute("DestCode");
					String departureOrder = departureElement.getAttribute("Order");
					String departureDepartTime = departureElement.getAttribute("DepartTime");
					String departureDepartInterval = departureElement.getAttribute("DepartInterval");
					String departureDeparted = departureElement.getAttribute("Departed");
					String departureDirection = departureElement.getAttribute("Direction");
					String departureIsStalled = departureElement.getAttribute("IsStalled");
					String departureTrackCode = departureElement.getAttribute("TrackCode");
					String departureLineCode = departureElement.getAttribute("LN");

					// Only add entries that match our requested line! Stupid TfL API! 
					if (lineCode.matches(departureLineCode)) {
						NextDeparture nextDeparture = new NextDeparture();
						nextDeparture.setDestination(departureDestination);
						nextDeparture.setLocation(departureLocation);
						nextDeparture.setTime(Integer.parseInt(departureSecondsTo));

						if (LOGV) Log.v(TAG, ("  -> " + departureDestination + " (" + departureDestCode + "): " + departureSecondsTo + " at " + departureLocation));

						platform.addNextDeparture(nextDeparture);
					}
				}
				
				if (platform.numberOfDepartures() > 0) { 
					if (LOGV) Log.v(TAG, "Found Platform: " + platform);
					departureBoard.addPlatform(platform);
				}
			}
			
			return departureBoard;
			
		}
		catch (Exception e) {
			throw new TubeChaserProviderException(e.getMessage());
		}
			


	 }
}

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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.andybotting.tubechaser.TubeChaser;
import com.andybotting.tubechaser.objects.DepartureBoard;
import com.andybotting.tubechaser.objects.NextDeparture;
import com.andybotting.tubechaser.objects.Platform;
import com.andybotting.tubechaser.utils.PreferenceHelper;

import android.content.Context;
import android.util.Log;

public class NatRailDepartures implements ServiceDepartures {
	
    private static final String TAG = "NatRailDepartures";
    private static final boolean LOGV = Log.isLoggable(TAG, Log.INFO);
    
    //private static final String DEPARTURES_URL = "http://realtime.nationalrail.co.uk/ldbws/ldb1.asmx";
    //private static final String DEPARTURES_URL = "http://realtime.nationalrail.co.uk/ldbws/ldb1.asmx";
    
	public DepartureBoard getNextDepartures (String stationCode) throws TubeChaserProviderException {

		DepartureBoard departureBoard = null;
		HttpURLConnection httpConn = null;
		InputStream is = null;
		OutputStream os = null;
		  
		try {

			// Get the application context
			PreferenceHelper preferenceHelper = new PreferenceHelper();
			String tokenValue = preferenceHelper.getNationalRailAPICode();
			
			String feedURL = "http://realtime.nationalrail.co.uk/LDBWS/ldb2.asmx";
			String userAgent = "NRE OS X Widget";
			String numRecs = "20";
			String filterCrs = "";
			String filterType = "to";
			
			// Open an HTTP Connection object
			httpConn = (HttpURLConnection) new URL(feedURL).openConnection();
			
			if (LOGV) Log.v(TAG, "Fetching URL (" + feedURL  +")");
			
			httpConn.setRequestMethod("POST");			
		    httpConn.setRequestProperty("User-Agent", userAgent);
		    httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
		    httpConn.setRequestProperty("SOAPAction", "http://thalesgroup.com/RTTI/2008-02-20/ldb/GetDepartureBoard");  

			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);			
			
//			String soapEnvelope = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
//			"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" + 
//			"<soap:Header>" +
//			"<AccessToken><TokenValue>" + preferenceHelper.getNationalRailAPICode() + "</TokenValue></AccessToken>" +
//			"</soap:Header>" +
//			"<soap:Body>" + 
//			"<GetDepartureBoardRequest xmlns=\"http://thalesgroup.com/RTTI/2007-10-10/ldb/types\">" + 
//			"<userAgent>WindowsGadgetNRE 1.21</userAgent>" + 
//			"<crs>" + stationCode + "</crs>" +
//			"</GetDepartureBoardRequest>" +
//			"</soap:Body>" +
//			"</soap:Envelope>";
			
			String soapEnvelope = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
				"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" +
					"<soap:Header>" + 
						"<AccessToken><TokenValue>" + tokenValue +  "</TokenValue></AccessToken>" + 
					"</soap:Header>" + 
					"<soap:Body>" + 
						"<GetDepartureBoardRequest xmlns=\"http://thalesgroup.com/RTTI/2008-02-20/ldb/types\">" +
							"<userAgent>" + userAgent + "</userAgent>" + 
							"<numRows>" + numRecs + "</numRows>" + 
							"<crs>" + stationCode + "</crs>" + 
							"<filterCrs>" + filterCrs + "</filterCrs>" + 
							"<filterType>" + filterType + "</filterType>" + 
						"</GetDepartureBoardRequest>" + 
					"</soap:Body>" + 
				"</soap:Envelope>";
			
			if (LOGV) Log.v(TAG, "SOAP Envelope (" + soapEnvelope  +")");
			
			//httpConn.connect();
		    os = httpConn.getOutputStream();
		    os.write(soapEnvelope.toString().getBytes());

		    // Read Response from the Server
		    is = httpConn.getInputStream();
		    departureBoard = parseResponse(is);
		    
		}
		catch (Exception e) {
			throw new TubeChaserProviderException(e);
		}
		finally {
				
			try {
				if(is!= null)
					is.close();
				if(os != null)
					os.close();
				if(httpConn != null)
					httpConn.disconnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
		return departureBoard;
			
	}
	
	
	public static DepartureBoard parseResponse(InputStream is) throws TubeChaserProviderException {
		//  <trainServices>
		//    <service>
		//      <origin>
		//        <location>
		//          <locationName>Clapham Junction</locationName>
		//          <crs>CLJ</crs>
		//        </location>
		//      </origin>
		//      <destination>
		//        <location>
		//          <locationName>Willesden Junction</locationName>
		//          <crs>WIJ</crs>
		//        </location>
		//      </destination>
		//      <std>19:45</std>
		//      <etd>On time</etd>
		//      <platform>2</platform>
		//      <operator>London Overground</operator>
		//      <operatorCode>LO</operatorCode>
		//      <serviceID>2JnoiQaKU5fHkQmop1txWQ==</serviceID>
		//    </service>
		//    <service>
		//      <origin>
		//        <location>
		//          <locationName>Stratford (London)</locationName>
		//          <crs>SRA</crs>
		//        </location>
		//      </origin>
		//      <destination>
		//        <location>
		//          <locationName>Clapham Junction</locationName>
		//          <crs>CLJ</crs>
		//        </location>
		//      </destination>
		//      <std>19:50</std>
		//      <etd>On time</etd>
		//      <platform>3</platform>
		//      <operator>London Overground</operator>
		//      <operatorCode>LO</operatorCode>
		//      <serviceID>1hSyVLpqFDhANBM36tJICg==</serviceID>
		//    </service>
		//  </trainServices>		
		
		DepartureBoard departureBoard = new DepartureBoard();

		try {
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			
			Document doc = db.parse(is);
			doc.getDocumentElement().normalize();
			
			String requestTimeString = doc.getElementsByTagName("generatedAt").item(0).getFirstChild().getNodeValue();
			Date requestTime = parseRequestDate(requestTimeString);
			
			NodeList serviceNodeList = doc.getElementsByTagName("service");			

			List<Platform> platforms = new ArrayList<Platform>();
			
			for (int i=0; i < serviceNodeList.getLength(); i++) {
				Node serviceNode = serviceNodeList.item(i);
				Element serviceElement = (Element) serviceNode;
				
				String destination = serviceElement.getElementsByTagName("locationName").item(0).getFirstChild().getNodeValue();
				String stdString = serviceElement.getElementsByTagName("std").item(0).getFirstChild().getNodeValue();
				String etdString = serviceElement.getElementsByTagName("etd").item(0).getFirstChild().getNodeValue();
				
				String platformNumber = "Unknown";
				Node platformNode = serviceElement.getElementsByTagName("platform").item(0);
				if (platformNode != null)
					platformNumber = platformNode.getFirstChild().getNodeValue();
				
				
                Pattern pattern = Pattern.compile("(\\d+:\\d+)");
                Matcher matcher = pattern.matcher(etdString);
                Date stdTime = parseDepartDate(stdString);
                int time = 0;
                if (matcher.find()) {
                	// Train is late, so use etd string for time
                	Date etdTime = parseDepartDate(etdString);
                	time = secondsDifference(requestTime, etdTime);
                }
                else {
                	time = secondsDifference(requestTime, stdTime);
                }


				NextDeparture nextDeparture = new NextDeparture();
				nextDeparture.setDestination(destination);
				nextDeparture.setLocation(etdString);
				nextDeparture.setTime(time);
				
				if (LOGV) Log.v(TAG, "Found NatRail departure -> " + destination + ": " + time + "");

				// Find an existing platform
				Platform platform = null;
				for (Platform existingPlatform : platforms) {
					if (existingPlatform.getPlatformNumber().matches(platformNumber))
						platform = existingPlatform;
				}

				// If this platform doesn't exist yet
				if (platform == null) {
					platform = new Platform();
					platform.setPlatformNumber(platformNumber);
					platforms.add(platform);
				}
				
				// Finally, add our departure to the right platform
				platform.addNextDeparture(nextDeparture);
				
			}
			
			departureBoard.setPlatforms(platforms);
			
		} 
		catch (Exception e) {
			throw new TubeChaserProviderException(e);
		}
		
		return departureBoard;
	}
	
	
	private static Date parseRequestDate(String dateString) {
		DateFormat df = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss");
		Date date = new Date();
		//<generatedAt>2011-01-06T19:46:13.4123124+00:00</generatedAt>
		
		try {
			// Strip the date to become: 2011-01-06T19:46:13
			String fixedDate = dateString.substring(0, 18);
			date = df.parse(fixedDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
		}	
		
		return date;
	}
	
	private static Date parseDepartDate(String dateString) {
		Date date = new Date();

		// Split the hh:mm string
		int hour = Integer.parseInt(dateString.split(":")[0]);
		int min = Integer.parseInt(dateString.split(":")[1]);
		
		date.setHours(hour);
		date.setMinutes(min);
		
		return date;
	}	

	// Get minutes away
	private static int secondsDifference(Date request, Date depart) {
		long diff = depart.getTime() - request.getTime();
		int sec = (int)diff/1000;
		return sec;
	}
	
	@Override
	public void setLine(String code) {
		// Not required for DLR
	}
		
}

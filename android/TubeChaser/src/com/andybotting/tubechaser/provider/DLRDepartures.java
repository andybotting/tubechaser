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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.andybotting.tubechaser.objects.DepartureBoard;
import com.andybotting.tubechaser.objects.NextDeparture;
import com.andybotting.tubechaser.objects.Platform;
import com.andybotting.tubechaser.utils.StringUtil;

import android.util.Log;

public class DLRDepartures implements ServiceDepartures {
	
    private static final String TAG = "DLRDepartures";
    private static final boolean LOGV = Log.isLoggable(TAG, Log.INFO);
    
	private static final String DEPARTURES_URL = "http://www.dlrlondon.co.uk/xml/mobile/%s.xml";
    
    private static Pattern pattern;
    private static Matcher matcher;

	public DepartureBoard getNextDepartures (String stationCode) throws TubeChaserProviderException {	

		DepartureBoard departureBoard = null;
		HttpURLConnection httpConn = null;
		InputStream is = null;
		OutputStream os = null;

	    try {
	       	
	    	String url = String.format(DEPARTURES_URL, stationCode, stationCode);
	    	httpConn = (HttpURLConnection) new URL(url).openConnection();
			httpConn.setRequestMethod("GET");
			httpConn.setDoOutput(false);
			httpConn.setDoInput(true);
					
			if (LOGV) Log.v(TAG, "Fetching URL (" + url  +")");
			
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
		//		<?xml version="1.0" encoding="UTF-8"?>
		//		<ttBoxset xmlns="http://www.dlrmobile.org/schemas/sitemap/0.9">
		//		  <div id="ttbox">
		//		    <div id="platformleft">
		//		      <img src="p1l.gif" alt="" width="54" height="86" border="0" />
		//		    </div>
		//		    <div id="platformmiddle">
		//		      <div id="line1">
		//		      	1 Lewisham
		//			  </div>
		//		      <div id="clearall">
		//		      </div>
		//		      <div id="line23">
		//		        <p>
		//					2 LEWISHAM       10 MINS
		//
		//					<br />
		//
		//					3 LEWISHAM       20 MINS
		//				</p>
		//		      </div>
		//		      <div id="time">
		//		         00:00
		//			  </div>
		//		    </div>
		//		    <div id="platformright">
		//		      <img src="p2r.gif" alt="" width="57" height="86" border="0" />
		//		    </div>
		//		  </div>
		//		  <div id="ttbox">
		//		    <div id="platformleft">
		//		      <img src="p3l.gif" alt="" width="54" height="86" border="0" />
		//		    </div>
		//		    <div id="platformmiddle">
		//		      <div id="line1">
		//		        1 Stratford       5 mins
		//            </div>
		//		      <div id="clearall">
		//		      </div>
		//		      <div id="line23">
		//		        <p>
		//		           2 STRATFORD      15 MINS
		//
		//		           <br />
		//
		//		           3 STRATFORD      25 MINS
		//              </p>
		//		      </div>
		//		      <div id="time">
		//              00:00
		//            </div>
		//		    </div>
		//		    <div id="platformright">
		//		      <img src="p4r.gif" alt="" width="57" height="86" border="0" />
		//		    </div>
		//		  </div>
		//		</ttBoxset>	
		
		DepartureBoard departureBoard = new DepartureBoard();

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			
			Document doc = db.parse(is);
			doc.getDocumentElement().normalize();
			
			NodeList serviceNodeList = doc.getElementsByTagName("div");
			
			List<Platform> platforms = new ArrayList<Platform>();
			Platform platform = null;
			NextDeparture nextDeparture = null;
			
			for (int i=0; i < serviceNodeList.getLength(); i++) {
				Node serviceNode = serviceNodeList.item(i);
				Element serviceElement = (Element) serviceNode;
				
				String nodeID = serviceElement.getAttribute("id");

				if (nodeID.matches("platformleft")) {
					// <img src="p5l.gif" alt="" width="54" height="86" border="0" />
					NodeList imgList = serviceElement.getElementsByTagName("img");
					Element imgElement = (Element) imgList.item(0);
					
					String imgSrc = imgElement.getAttribute("src"); // src="p5l.gif"
					
					pattern = Pattern.compile("p(\\d+)l.gif");
					matcher = pattern.matcher(imgSrc);
					 
					if (matcher.find()) {
						platform = new Platform();
						platform.setPlatformNumber(matcher.group(1));
						platforms.add(platform);
					}

				}

				// Parse the first departure
				if (nodeID.matches("line1")) {
					String value = serviceElement.getFirstChild().getNodeValue();
				
					pattern = Pattern.compile("\\d+\\s+(.*)\\s+(\\d+)\\s+min?");
					matcher = pattern.matcher(value);
										 
					if (matcher.find()) {
						String destination = StringUtil.titleCase(matcher.group(1));
						String timeString = matcher.group(2);
						
						int time = 0;
						if (timeString != null) {
							time = Integer.parseInt(timeString) * 60;
						}

						nextDeparture = new NextDeparture();
						nextDeparture.setDestination(destination);
						nextDeparture.setTime(time);
						platform.addNextDeparture(nextDeparture);
						if (LOGV) Log.v(TAG, "Found DLR departure -> " + destination + ": " + time + "");
					}	
					
				}

				// Parse the 2nd and 3rd departure
				if (nodeID.matches("line23")) {
					
					NodeList line23NodeList = serviceElement.getElementsByTagName("p").item(0).getChildNodes();
					for (int j=0; j < line23NodeList.getLength(); j++) {
						Node line23Node = line23NodeList.item(j);
						String line = line23Node.getNodeValue();
					
						if (line != null) {
							pattern = Pattern.compile("\\d+\\s+(.*)\\s+(\\d+)\\s+MIN");
							matcher = pattern.matcher(line);
						
							if (matcher.find()) {
								String destination = StringUtil.titleCase(matcher.group(1));
								String timeString = matcher.group(2);
								
								int time = 0;
								if (timeString != null)
									time = Integer.parseInt(timeString) * 60;
								
								nextDeparture = new NextDeparture();
								nextDeparture.setDestination(destination);
								nextDeparture.setTime(time);
								platform.addNextDeparture(nextDeparture);
								if (LOGV) Log.v(TAG, "Found DLR departure -> " + destination + ": " + time + "");
							}
						}
					}
				}

				// Wipe out our nextDeparture, so we don't carry anything over to the next look
				nextDeparture = null;
				
			}
			
			// Clean out the platforms, and just keep those with departures
			for (int i = 0; i < platforms.size(); i++) {
				Platform thisPlatform = platforms.get(i);
				if (thisPlatform.numberOfDepartures() == 0) {
					platforms.remove(i);
				}
			}
			
			// Set the final platforms for the departureboard
			departureBoard.setPlatforms(platforms);
			
		} 
		catch (Exception e) {
			throw new TubeChaserProviderException(e);
		}
		
		return departureBoard;
		
		
	}

	@Override
	public void setLine(String code) {
		// Not required for DLR
	}

		
}

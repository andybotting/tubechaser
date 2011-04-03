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

package com.andybotting.tubechaser.objects;

import java.util.List;

import com.andybotting.tubechaser.provider.TubeChaserContract.Stations;
import com.google.android.maps.GeoPoint;

import android.location.Location;
import android.net.Uri;

public class Station {
	private int id;
	private String name;
	private String linesString;
	private String tflId;
	private double latitude;
	private double longitude;
	private Location location;
	private String status;
	private String statusCode;
	private String statusDesc;
	private boolean stepFree = false;
	private List<Line> lines;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getLinesString() {
		return linesString;
	}

	public void setLinesString(String linesString) {
		this.linesString = linesString;
	}
	
	public String getTflId() {
		return tflId;
	}

	public void setTflId(String tflId) {
		this.tflId = tflId;
	}
	
	public double getLatitude() {
		return latitude;
	}	
	
	public void setLatitude(Double latitude) {
		this.latitude = latitude;		
	}
	
	public double getLongitude() {
		return longitude;
	}	 
	
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
		
	}

	public void setLocation(Location location) {
		 this.location = location;
	}
	
	
	public Location getLocation() {
		if (location != null) {
			return location;
		}
		else {
			location = new Location("dummy");
			location.setLongitude(longitude);
			location.setLatitude(latitude);
			return location;
		}
	}	
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getStatus() {
		return status;
	}	
	
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	
	public String getStatusCode() {
		return statusCode;
	}	
	
	public void setStatusDesc(String statusDesc) {
		this.statusDesc = statusDesc;
	}
	
	public String getStatusDesc() {
		return statusDesc;
	}	
		
	public void setStepFree(int stepFree) {
		if (stepFree == 1)
			this.stepFree = true;
	}

	public boolean isStepFree() {
		return stepFree;
	}
	
	public void setLines(List<Line> lines) {
		this.lines = lines;
	}
	
	public List<Line> getLines() {
	   return lines;
	}   	
	
	public String getLinesListString() {
		String linesString = "";

		if(lines != null && lines.size() > 0){
			
			for(int i=0; i < lines.size(); i++) {
				Line l = lines.get(i);
				linesString += l.getName();
			
				if (i < lines.size() -2)
					linesString += ", ";
				else if (i == lines.size() -2)
					linesString += " and ";
			}
			
			// End with 'Line' or 'Lines'
			if (lines.size() > 1)
				linesString += " Lines";
			else
				linesString += " Line";

		}

		return linesString;
	}
	
	public double distanceTo(Location location) {
		double distance = this.getLocation().distanceTo(location);
		return distance;
	}
	
    public String formatDistanceTo(Location location){
    	
    	double distance = this.distanceTo(location);

    	String result = "0m";
    	
    	if(distance > 10000) {
    		// More than 10kms
    		distance = distance / 1000;
    		result = (int)distance + "km";
    	}
    	else if(distance > 999) {
    		distance = distance / 1000;
    		result = roundToDecimals(distance, 1) + "km";
    	}
    	else {
    		result = (int)distance + "m";
    	}
    	
    	return result;
    }
	
	private static double roundToDecimals(double value, int decimalPlaces) {
    	int intValue = (int)((value * Math.pow(10, decimalPlaces)));
    	return (((double)intValue) / Math.pow(10, decimalPlaces));
    }

	public GeoPoint getGeoPoint() {
        int lat1E6 = (int) (latitude * 1E6);
        int lng1E6 = (int) (longitude * 1E6);
        GeoPoint point = new GeoPoint(lat1E6, lng1E6);
        return point;
	}

	
	public String toString() {
		return name;
	}

    public Uri getUri() {
        return Uri.withAppendedPath(Stations.CONTENT_URI, "" + id);
    }




	
}

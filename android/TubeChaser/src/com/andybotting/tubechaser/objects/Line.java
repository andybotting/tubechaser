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

import java.util.Date;
import android.net.Uri;
import com.andybotting.tubechaser.provider.TubeChaserContract.Lines;


public class Line {
	
	// Line status
    public static final int STATUS_GOOD = 0;
    public static final int STATUS_MINOR = 1;
    public static final int STATUS_SEVERE = 2;  
    public static final int STATUS_UNKNOWN = 3;  
    
    // Line types
    public static final int TYPE_TUBE = 0;
    public static final int TYPE_DLR = 1;
    public static final int TYPE_OVERGROUND = 2;
    public static final int TYPE_NATIONAL_RAIL = 3;
    
    // Tube lines
    public static final int LINE_BAKERLOO = 0;
    public static final int LINE_CENTRAL = 1;
    public static final int LINE_CIRCLE = 2;
    public static final int LINE_DISTRICT = 3;
    public static final int LINE_HAMMERSMITH_AND_CITY = 4;
    public static final int LINE_JUBILEE = 5;
    public static final int LINE_METROPOLITAN = 6;
    public static final int LINE_NORTHERN = 7;
    public static final int LINE_PICCADILLY = 8;
    public static final int LINE_VICTORIA = 9;
    public static final int LINE_WATERLOO_AND_CITY = 10;
        
	private int id;
	private String name;
	private String shortName;
	private String code;
	private String tflid;
	private int type;
	private String colour;
	
	private String status = "";      // "Good Service"
	private String statusCode = "";  // "GS" or "MD"
	private String statusClass = ""; // "GoodService" or "DisruptedService"
	private String statusDesc = "";  // "Due to an earlier Police investigation at Hammersmith. "
	private Date fetchedTime;

	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	public String getLineName() {
		if (type == TYPE_TUBE)
			return name + " Line";
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public void setTfLID(String tflid) {
		this.tflid = tflid;
	}

	public String getTfLID() {
		return tflid;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public String getColour() {
		return colour;
	}

	public void setColour(String colour) {
		this.colour = colour;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		if (status == null)
			return "Unknown";
		return status;
	}

	public void setStatusDesc(String statusDesc) {
		this.statusDesc = statusDesc;
	}

	public String getStatusDesc() {
		if (statusDesc == null)
			return "Unknown";
		return statusDesc;
	}
	
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getStatusCode() {
		if (statusCode == null)
			return "NA";
		return statusCode;
	}

	public void setStatusClass(String statusClass) {
		this.statusClass = statusClass;
	}

	public String getStatusClass() {
		if (statusClass == null)
			return "Unknown";
		return statusClass;
	}
	
	public int getStatusLevel() {
		String status = this.getStatus();

		// Severe
		if (status.contains("Severe"))
			return STATUS_SEVERE;
		else if (status.contains("Planned Closure"))
			return STATUS_SEVERE;
		else if (status.contains("Closed"))
			return STATUS_SEVERE;
		else if (status.contains("Suspended"))
			return STATUS_SEVERE;

		// Minor
		else if (status.contains("Minor Delays"))
			return STATUS_MINOR;
		else if (status.contains("Part Closure"))
			return STATUS_MINOR;
		else if (status.contains("Part Suspended"))
			return STATUS_MINOR;
		else if (status.contains("Special Service"))
			return STATUS_MINOR;
		
		// Good
		else if (status.contains("Good Service"))
			return STATUS_GOOD;
		
		// Unknown
		else if (status.contains("Unknown"))
			return STATUS_UNKNOWN;
		else
			return STATUS_UNKNOWN;
	}	
	
	public String getStatusImage() {
		int status = this.getStatusLevel();
		
        switch (status) {
	        case STATUS_GOOD:
	            return "status_good";
	        case STATUS_MINOR:
	            return "status_minor";   
	        case STATUS_SEVERE:
	            return "status_severe";
	        case STATUS_UNKNOWN:
	            return "status_unknown"; 
	        default:
	        	return "status_unknown";
        }
    }

	public void setFetchedTime() {
		this.fetchedTime = new Date();
	}

	public Date getFetchedTime() {
		return fetchedTime;
	}
	
	// Get minutes away
	public int getFetchedAge() {
		Date now = new Date();

		long diff = now.getTime() - this.getFetchedTime().getTime(); 
		int minutes = (int)diff/60000;
		return minutes;
	}
	
	public String toString() {
		return "Line: " + name;
	}

    public Uri getUri() {
        return Uri.withAppendedPath(Lines.CONTENT_URI, "" + id);
    }



	
}

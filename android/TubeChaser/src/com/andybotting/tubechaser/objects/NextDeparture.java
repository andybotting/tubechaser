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


public class NextDeparture implements Comparable <NextDeparture> {

	private String destination;
	private String location;
	private int time = 0;

	public void setDestination(String destination) {
		this.destination = destination;
	}
	public String getDestination() {
		return destination;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	public String getLocation() {
		return location;
	}
	
	public void setTime(int seconds) {
		this.time = seconds;
	}
	
	public int getTime() {
		return time;
	}
	
	/**
	 * Generate a human friendly string representing time to departure
	 * @return String
	 */
	public String humanMinutesAway() {
	
		// Because of how we set the minutes and seconds, for National Rail parsing
		// it could be up to -60 seconds difference, so allow for it
		if (time < -60) {
			return "Err";
		}
		else if (time < 60) {
			return "Now";
		}
		else {
			int mins = time/60;
			
			if (mins > 1)
				return mins + " mins";
			else
				return mins + " min";
		}
	}

	/**
	 * Comparison method for sorting a list of NextDepartures
	 */
	public int compareTo(NextDeparture otherDeparture) {

		int thisTime = this.time;
		int otherTime = otherDeparture.getTime();
		
		// Test the difference between this, and the given NextDeparture obj
		if(thisTime < otherTime) {
			return -1;
		} 
		else if(thisTime > otherTime) {
			return 1;
		}
		return 0; 
	}

	
	public String toString() {
		return String.format("Departure %s in %s", destination, time);
	}
	
}

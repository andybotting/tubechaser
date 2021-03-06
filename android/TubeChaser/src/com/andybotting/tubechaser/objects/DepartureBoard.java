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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class DepartureBoard {

	private Station tubeStation;
	private Date lastUpdated;
	private List<Platform> platforms = new ArrayList<Platform>(); 
	
	public void setTubeStation(Station tubeStation) {
		this.tubeStation = tubeStation;
	}
	
	public Station getTubeStation() {
		return tubeStation;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	
	public Date getLastUpdated() {
		return lastUpdated;
	}
	
	public void setPlatforms(List<Platform> platforms) {
		this.platforms = platforms;
	}
	
	public void addPlatform(Platform platform) {
		this.platforms.add(platform);
	}
	
	public List<Platform> getPlatforms() {
		return platforms;
	}
	
	public int numberOfPlatforms() {
		return platforms.size();
	}
	
	public Platform getPlatform(int location) {
		return platforms.get(location);
	}

	public String toString() {
		return String.format("DepartureBoard %s", platforms);
	}

}

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
import java.util.StringTokenizer;

public class StringUtil {

	  /**
	   * Parses a string of given stop:line entries into a list
	   */
	  public static ArrayList<String> parseString(String list) {
	      ArrayList<String> result = new ArrayList<String>();
	      StringTokenizer tokenizer = new StringTokenizer(list, ",");
	      while (tokenizer.hasMoreTokens()) {
	          result.add(tokenizer.nextToken());
	      }
	      return result;
	  }
	  
	  /**
	   * Build a station:line string for a given list of favourites
	   * @param list of station:line entries
	   * @return string of station:line,station:line entries
	   */
	  public static String makeString(ArrayList<String> list) {
		  String result = "";
		  for (String item : list) {
			  result = String.format("%s%s,", result, item);
		  }
		  
		  return result;
	  }
	
	
	
}

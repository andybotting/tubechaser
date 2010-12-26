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

package com.andybotting.tubechaser.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.andybotting.tubechaser.R;
import com.andybotting.tubechaser.objects.Station;
import com.andybotting.tubechaser.provider.TubeChaserProvider;
import com.andybotting.tubechaser.ui.BalloonItemizedOverlay;
import com.andybotting.tubechaser.utils.UIUtils;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class StationMap extends MapActivity {    
	
	public static final String EXTRA_STATION = "com.andybotting.tubechaser.extra.STATION";
	
	private List<Overlay> mMapOverlays;

	private MapController mMapController;	
    private MapView mMapView;
    private MyLocationOverlay mMyLocationOverlay;

	Station mStation;
	Context mContext;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_map);
                
        final Uri stationUri = getIntent().getParcelableExtra(EXTRA_STATION);
        
        mContext = this.getBaseContext();
        TubeChaserProvider provider = new TubeChaserProvider();
        
        mStation = provider.getStation(mContext, stationUri);
        
        ((TextView) findViewById(R.id.title_text)).setText(mStation.getName());
        
       	mMapView = (MapView) findViewById(R.id.mapView);
       	mMapView.setBuiltInZoomControls(true);
       	
        mMapController = mMapView.getController();
        mMapOverlays = mMapView.getOverlays();
        
        mMyLocationOverlay = new MyLocationOverlay(this, mMapView);
        mMapOverlays.add(mMyLocationOverlay);

        mMapView.setClickable(true);
        mMapView.setEnabled(true);
        
		// Home button
		findViewById(R.id.btn_title_home).setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {       
		    	UIUtils.goHome(StationMap.this);
		    }
		});	
		
		// My Location button
		findViewById(R.id.btn_title_myloc).setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		    	GeoPoint myLoc = mMyLocationOverlay.getMyLocation();
		    	if (myLoc != null) {
		    		mMapView.getController().animateTo(myLoc);
		    	}
		    	else {
		    		UIUtils.popToast(getApplicationContext(), "Unable to find your location");
		    	}
		    }
		});	
        
        
        displayStation(mStation);
    }
        

	public class MyItemizedOverlay extends BalloonItemizedOverlay<OverlayItem> {

	    private ArrayList<OverlayItem> m_overlays = new ArrayList<OverlayItem>();

	    public MyItemizedOverlay(Drawable defaultMarker, MapView mapView) {
	        super(boundCenter(defaultMarker), mapView);
	    }

	    public void addOverlay(OverlayItem overlay) {
	        m_overlays.add(overlay);
	        populate();
	    }

	    @Override
	    protected OverlayItem createItem(int i) {
	        return m_overlays.get(i);
	    }

	    @Override
	    public int size() {
	        return m_overlays.size();
	    }

	    @Override
	    protected boolean onBalloonTap(int index) {
	        return true;
	    }

	}

    private void displayStation(Station mStation) {
    	
    	Drawable drawable = this.getResources().getDrawable(R.drawable.map_marker);
    	MyItemizedOverlay itemizedOverlay = new MyItemizedOverlay(drawable, mMapView);
    	mMapOverlays.add(itemizedOverlay);
	
		GeoPoint point = mStation.getGeoPoint();
		String title = mStation.getName();
		String snippet = mStation.getLinesString();
		OverlayItem overlayitem = new OverlayItem(point, title, snippet);
    	itemizedOverlay.addOverlay(overlayitem);
	    
	    mMapController.setZoom(17);
	    mMapController.setCenter(point);
    }

    
    @Override
    protected void onResume() {
        super.onResume();
        mMyLocationOverlay.enableMyLocation();
    }

    @Override
    protected void onStop() {
        mMyLocationOverlay.disableMyLocation();
        super.onStop();
    }
    
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}

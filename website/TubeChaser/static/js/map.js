function showMap() {

	// Check to see if this browser can run the Google API
	if (GBrowserIsCompatible()) {

		// Display the map, with some controls and set the initial location 
		var map = new GMap2(document.getElementById("map"));

		map.setCenter(new GLatLng(default_lat, default_lng), default_zoom);

		// ====== Restricting the range of Zoom Levels =====
		// Get the list of map types		
		var mt = map.getMapTypes();

		// Overwrite the getMinimumResolution() and getMaximumResolution() methods
		for (var i=0; i<mt.length; i++) {
			mt[i].getMinimumResolution = function() {return min_zoom;}
			mt[i].getMaximumResolution = function() {return max_zoom;}
		}

		// Set Map specifics
		var customUI = map.getDefaultUI();
		customUI.maptypes.normal = true;
		customUI.maptypes.satellite = false;
		customUI.maptypes.hybrid = false;
		customUI.maptypes.physical = false;
		customUI.zoom.scrollwheel = false;
		customUI.controls.smallzoomcontrol3d = true;
		customUI.controls.scalecontrol = false;
		map.setUI(customUI);
	
		// Add a move listener to restrict the bounds range
		//GEvent.addListener(map, "move", function() {
		//	checkBounds();
		//});

		// The allowed region which the whole map must be within
		
		//bounds_north = -37.75
		//bounds_south = -37.80
		//bounds_east = 144.80
		//bounds_west = 145.15

		south_west = new GLatLng(bounds_south, bounds_west)
		north_east = new GLatLng(bounds_north, bounds_east)


		var allowedBounds = new GLatLngBounds(south_west, north_east);
		
		// If the map position is out of range, move it back
		function checkBounds() {
			// Perform the check and return if OK
			if (allowedBounds.contains(map.getCenter())) {
				return;
			}

			// It's not OK, so find the nearest allowed point and move there
			var C = map.getCenter();
			var X = C.lng();
			var Y = C.lat();
	
			var AmaxX = allowedBounds.getNorthEast().lng();
			var AmaxY = allowedBounds.getNorthEast().lat();
			var AminX = allowedBounds.getSouthWest().lng();
			var AminY = allowedBounds.getSouthWest().lat();

			if (X < AminX) {X = AminX;}
			if (X > AmaxX) {X = AmaxX;}
			if (Y < AminY) {Y = AminY;}
			if (Y > AmaxY) {Y = AmaxY;}
			//alert ("Restricting "+Y+" "+X);
			map.setCenter(new GLatLng(Y,X));
		}

		function createHeatMap() {
			var myCopyright = new GCopyrightCollection("© ");
			myCopyright.addCopyright(new GCopyright('', new GLatLngBounds(new GLatLng(-90,-180), new GLatLng(90,180)), 0,''));

			var tilelayer = new GTileLayer(myCopyright);
			tilelayer.getTileUrl = function(point, zoom) { return "/tile/" + zoom + "_" + point.y + "_" + point.x +".png"; };
			tilelayer.isPng = function() { return true; };
			tilelayer.getOpacity = function() { return 1.0; };
	
			var tilelayeroverlay = new GTileLayerOverlay(tilelayer);
			map.addOverlay(tilelayeroverlay);
		}

		// Make the heat map tiles
		createHeatMap(map);

	}
	
	// display a warning if the browser was not compatible
	else {
		alert("Sorry, the Google Maps API is not compatible with this browser");
	}

	// This Javascript is based on code provided by the
	// Community Church Javascript Team
	// http://www.bisphamchurch.org.uk/	 
	// http://econym.org.uk/gmap/


}

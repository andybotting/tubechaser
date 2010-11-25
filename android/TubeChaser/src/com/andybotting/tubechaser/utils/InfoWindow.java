package com.andybotting.tubechaser.utils;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.andybotting.tubechaser.R;
import com.andybotting.tubechaser.objects.Line;



public class InfoWindow {

	private final Activity mActivity;

	public InfoWindow(final Activity mActivity) {
		this.mActivity = mActivity;
	}
	
	public List<View> getInfoWindows(List<Line> lines) {
		
		List<View> infoViews = new ArrayList<View>();
		
		// List of possible status windows
		List<Line> goodService = new ArrayList<Line>();
    	List<Line> severeDelays = new ArrayList<Line>();
    	List<Line> minorDelays = new ArrayList<Line>();
		List<Line> closures = new ArrayList<Line>();
		
		// Info window items
		String statusMessage;
		String statusTitle;
		int statusImage;
		
		
    	for (Line line : lines) {
    		if (line.getStatus().contains("Severe delays")) {
    			severeDelays.add(line);
    		}
    		else if (line.getStatus().contains("Minor delays")) {
    			minorDelays.add(line);
    		}
    		else if (line.getStatus().contains("closure")) {
    			closures.add(line);
    		}
    		else if (line.getStatus().contains("Closed")) {
    			closures.add(line);
    		}
    		else if (line.getStatus().contains("Good service")) {
    			goodService.add(line);
    		}
    	} 

		
		if (goodService.size() == lines.size()) {
			// All lines have good service!
			statusTitle = "Good Service";
			statusImage = R.drawable.info_window_good;
			statusMessage = "There is a Good Service on all lines.";
			infoViews.add(buildView(statusTitle, statusMessage, statusImage));
		}
		else {
			if (!severeDelays.isEmpty()) {
				statusTitle = "Severe Delays";
				statusImage = R.drawable.info_window_severe;
				statusMessage = buildLines(severeDelays) + (severeDelays.size() > 1 ? " have " : " has ") + statusTitle;	
				infoViews.add(buildView(statusTitle, statusMessage, statusImage));
			}
			if (!minorDelays.isEmpty()) {
				statusTitle = "Minor Delays";
				statusImage = R.drawable.info_window_minor;
				statusMessage = buildLines(minorDelays) + (minorDelays.size() > 1 ? " have " : " has ") + statusTitle;
				infoViews.add(buildView(statusTitle, statusMessage, statusImage));
			}
			if (!closures.isEmpty()) {
				statusTitle = "Line Closures";
				statusImage = R.drawable.info_window_closure;
				statusMessage = "Closures on the " + buildLines(closures) + ".";
				infoViews.add(buildView(statusTitle, statusMessage, statusImage));
			}
		}
		
		
		return infoViews;
	}

	private View buildView(String statusTitle, String statusMessage, int statusImage) {
		
		View infoView = mActivity.getLayoutInflater().inflate(R.layout.info_window, null);
		
		((TextView) infoView.findViewById(R.id.info_window_title)).setText(statusTitle);
		((TextView) infoView.findViewById(R.id.info_window_subtitle)).setText(statusMessage);
		((ImageButton) infoView.findViewById(R.id.info_window_icon)).setImageResource(statusImage);

		return infoView;
	}
	
	
	
	
    /**
     * Build a string of lines from a given list
     */
	private String buildLines(List<Line> lines) {
		
		String linesString = "";
		
		for(int i=0; i < lines.size(); i++) {
			Line l = lines.get(i);
	
			linesString += l.getName();
			if (i < lines.size() -2) {
				linesString += ", ";
			}
			else if (i == lines.size() -2){
				linesString += " and ";
			}
		}
		
		if (lines.size() > 1)
			linesString += " Lines";
		else
			linesString += " Line";
		
		return linesString;
		
	}
	

}

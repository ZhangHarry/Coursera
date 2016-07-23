package module3;

//Java utilities libraries
import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.List;

//Processing library
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.providers.Microsoft;
import processing.core.PApplet;

//Unfolding libraries
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.utils.MapUtils;

//Parsing library
import parsing.ParseFeed;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author ZhangHr
 * Date: July 17, 2015
 * */
public class EarthquakeCityMap extends PApplet {

	// You can ignore this.  It's to keep eclipse from generating a warning.
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFLINE, change the value of this variable to true
	private static final boolean offline = true;
	
	// Less than this threshold is a light earthquake
	public static final float THRESHOLD_MODERATE = 5;
	// Less than this threshold is a minor earthquake
	public static final float THRESHOLD_LIGHT = 4;

	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "data/blankLight-1-3.mbtiles";
	
	// The map
	private UnfoldingMap map;
	
	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";

	//todo personal data constant
	private static int radioMinor = 10;
	private static int radiusLight = 15;
	private static int radiusModerate = 20;
	private int colorMinor = color(0, 0, 255);
	private int coloroLight = color(255, 255, 0);
	private int colorModerate = color(255, 0, 0);

	public void setup() {
		size(950, 600, OPENGL);

		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 700, 500, new MBTilesMapProvider(mbTilesString));
			map = new UnfoldingMap(this, 200, 50, 700, 500, new Microsoft.RoadProvider());
		    earthquakesURL = "2.5_week.atom"; 	// Same feed, saved Aug 7, 2015, for working offline
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 700, 500, new Google.GoogleMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
			earthquakesURL = "2.5_week.atom";
		}
		
	    map.zoomToLevel(2);
	    MapUtils.createDefaultEventDispatcher(this, map);	
			
	    // The List you will populate with new SimplePointMarkers
	    List<Marker> markers = new ArrayList<Marker>();

	    //Use provided parser to collect properties for each earthquake
	    //PointFeatures have a getLocation method
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    
	    // These print statements show you (1) all of the relevant properties 
	    // in the features, and (2) how to get one property and use it
	    if (earthquakes.size() > 0) {
	    	PointFeature f = earthquakes.get(0);
	    	System.out.println(f.getProperties());
	    	Object magObj = f.getProperty("magnitude");
	    	float mag = Float.parseFloat(magObj.toString());
	    	// PointFeatures also have a getLocation method
	    }

	    // Here is an example of how to use Processing's color method to generate 
	    // an int that represents the color yellow.  
	    int yellow = color(255, 255, 0);
	    
	    //TODO: Add code here as appropriate
		for (PointFeature feature : earthquakes){
			SimplePointMarker marker = createMarker(feature);
			markers.add(marker);
		}
		map.addMarkers(markers);
	}
		
	// A suggested helper method that takes in an earthquake feature and 
	// returns a SimplePointMarker for that earthquake
	// TODO: Implement this method and call it from setUp, if it helps
	private SimplePointMarker createMarker(PointFeature feature)
	{
		SimplePointMarker marker = new SimplePointMarker(feature.getLocation(), feature.getProperties());
		System.out.println(feature.getProperties());
		Object magObj = feature.getProperty("magnitude");
		float mag = Float.parseFloat(magObj.toString());
		if (mag < THRESHOLD_LIGHT){
			marker.setColor(colorMinor);
			marker.setRadius(radioMinor);
		}else if (mag < THRESHOLD_MODERATE){
			marker.setColor(coloroLight);
			marker.setRadius(radiusLight);
		}else {
			marker.setColor(colorModerate);
			marker.setRadius(radiusModerate);
		}
		return marker;
	}
	
	public void draw() {
	    background(10);
	    map.draw();
	    addKey();
	}


	// helper method to draw key in GUI
	// TODO: Implement this method to draw the key
	private void addKey() 
	{	
		// Remember you can use Processing's graphics methods here
		int planeX = 10, planeY = 50, planeHeight = 200, planeWidth = 160;
		fill(255, 255, 255);
		rect(planeX, planeY, planeWidth, planeHeight);

		fill(colorMinor);
		ellipse(planeX + planeWidth / 4, planeY + 10, radioMinor, radioMinor);
		fill(0);
		text("below 4.0", planeX + planeWidth / 2, planeY + 10);

		fill(coloroLight);
		ellipse(planeX + planeWidth / 4, planeY + 70, radiusLight, radiusLight);
		fill(0);
		text("4.0+ Magnitude", planeX + planeWidth / 2, planeY + 70);

		fill(colorModerate);
		ellipse(planeX + planeWidth / 4, planeY + 140, radiusModerate, radiusModerate);
		fill(0);
		text("5.0+ Magnitude", planeX + planeWidth / 2, planeY + 140);
	}

	public static void main(String[] args){
		PApplet.main( new String[] { "--present", new EarthquakeCityMap().getClass().getName() });
	}
}

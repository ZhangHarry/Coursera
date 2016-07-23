package module4;

import java.util.*;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author ZhangHr
 * Date: July 17, 2015
 * */
public class EarthquakeCityMap extends PApplet {
	
	// We will use member variables, instead of local variables, to store the data
	// that the setUp and draw methods will need to access (as well as other methods)
	// You will use many of these variables, but the only one you should need to add
	// code to modify is countryQuakes, where you will store the number of earthquakes
	// per country.
	
	// You can ignore this.  It's to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFILINE, change the value of this variable to true
	private static final boolean offline = false;
	
	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	

	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	
	// The files containing city names and info and country names and info
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";
	
	// The map
	private UnfoldingMap map;
	
	// Markers for each city
	private List<Marker> cityMarkers;
	// Markers for each earthquake
	private List<Marker> quakeMarkers;

	// A List of country markers
	private List<Marker> countryMarkers;

	//todo add personal data constant
	protected static int radioMinor = 5;
	protected static int radiusLight = 10;
	protected static int radiusModerate = 15;
	protected int colorMinor = color(0, 0, 255);
	protected int coloroLight = color(255, 255, 0);
	protected int colorModerate = color(255, 0, 0);
	
	public void setup() {		
		// (1) Initializing canvas and map tiles
		size(900, 700, OPENGL);
		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom";  // The same feed, but saved August 7, 2015
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 650, 600, new Google.GoogleMapProvider());
			map = new UnfoldingMap(this, 200, 50, 700, 500, new Microsoft.RoadProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
		    //earthquakesURL = "2.5_week.atom";
		}
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// FOR TESTING: Set earthquakesURL to be one of the testing files by uncommenting
		// one of the lines below.  This will work whether you are online or offline
//		earthquakesURL = "est1.atom";
		//earthquakesURL = "test2.atom";
		
		// WHEN TAKING THIS QUIZ: Uncomment the next line
		//earthquakesURL = "quiz1.atom";
		
		
		// (2) Reading in earthquake data and geometric properties
	    //     STEP 1: load country features and markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		
		//     STEP 2: read in city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for(Feature city : cities) {
		  cityMarkers.add(new CityMarker(city));
		}
	    
		//     STEP 3: read in earthquake RSS feed
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    quakeMarkers = new ArrayList<Marker>();
	    
	    for(PointFeature feature : earthquakes) {
		  //check if LandQuake
		  if(isLand(feature)) {
		    quakeMarkers.add(new LandQuakeMarker(feature));
		  }
		  // OceanQuakes
		  else {
		    quakeMarkers.add(new OceanQuakeMarker(feature));
		  }
	    }

	    // could be used for debugging
	    printQuakes();
	 		
	    // (3) Add markers to map
	    //     NOTE: Country markers are not added to the map.  They are used
	    //           for their geometric properties
	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);
	    
	}  // End setup
	
	
	public void draw() {
		background(0);
		map.draw();
		addKey();
		
	}
	
	// helper method to draw key in GUI
	// TODO: Update this method as appropriate
	// display left panel
	private void addKey() {	
		// Remember you can use Processing's graphics methods here
		// pane
		fill(255, 250, 240);
		rect(25, 50, 150, 500);

		int panelX = 50, panelYGap = 50, symbolYS = 125, textX = 75;
		// earthquake key pane
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", panelX, symbolYS - panelYGap);

		fill(color(30,30,30));
		triangle(panelX, symbolYS + 5, panelX + 5, symbolYS, panelX + 10, symbolYS + 5);
		fill(255, 250, 240);
		ellipse(panelX, symbolYS + panelYGap, radiusLight, radiusLight);
		fill(255, 250, 240);
		rect(panelX, symbolYS + panelYGap * 2, 10, 10);

		fill(0, 0, 0);
		text("City Marker", textX, symbolYS);
		text("LandQuake", textX, symbolYS + panelYGap);
		text("Ocean Quake", textX, symbolYS+panelYGap*2);

		// Magnitude pane
		symbolYS = symbolYS+panelYGap*4;
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("size~Magnitude", panelX, symbolYS - panelYGap);

		fill(colorMinor);
		ellipse(panelX, symbolYS, radioMinor, radioMinor);
		fill(coloroLight);
		ellipse(panelX, symbolYS + panelYGap, radiusLight, radiusLight);
		fill(colorModerate);
		ellipse(panelX, symbolYS + panelYGap * 2, radiusModerate, radiusModerate);
		fill(255, 250, 240);
		ellipse(panelX, symbolYS + panelYGap * 3, radiusModerate, radiusModerate);
		line(panelX-radiusModerate, symbolYS + panelYGap * 3-radiusModerate,
				panelX + radiusModerate, symbolYS + panelYGap * 3 + radiusModerate);
		line(panelX-radiusModerate, symbolYS + panelYGap * 3 + radiusModerate,
				panelX+radiusModerate, symbolYS + panelYGap * 3-radiusModerate);

		fill(0, 0, 0);
		text("shallow", textX, symbolYS);
		text("intermediate", textX, symbolYS+panelYGap);
		text("deep", textX, symbolYS+panelYGap*2);
		text("Past hour", textX, symbolYS+panelYGap*3);
	}

	
	
	// Checks whether this quake occurred on land.  If it did, it sets the 
	// "country" property of its PointFeature to the country where it occurred
	// and returns true.  Notice that the helper method isInCountry will
	// set this "country" property already.  Otherwise it returns false.
	private boolean isLand(PointFeature earthquake) {
		
		// IMPLEMENT THIS: loop over all countries to check if location is in any of them
		
		// TODO: Implement this method using the helper method isInCountry
		// has finished this todo
		for (Marker country : countryMarkers){
			if (isInCountry(earthquake, country)){
				earthquake.addProperty("country", country.getStringProperty("name"));
				return true;
			}
		}
		// not inside any country
		return false;
	}
	
	// prints countries with number of earthquakes
	// You will want to loop through the country markers or country features
	// (either will work) and then for each country, loop through
	// the quakes to count how many occurred in that country.
	// Recall that the country markers have a "name" property, 
	// And LandQuakeMarkers have a "country" property set.
	// This method is asked to display each country with 1 or more earthquakes and
	// the number of earthquakes detected in that country
	private void printQuakes() {
		// TODO: Implement this method
		// has finished this todo
		Map<String, Integer> countryQuakes = new HashMap<>();
		for (Marker marker : quakeMarkers) {
			String country = marker.getStringProperty("country");
			if (country != null) {
				Integer number = countryQuakes.get(country);
				if (number != null) {
					countryQuakes.put(country, number + 1);
				} else {
					countryQuakes.put(country, 1);
				}
			}
		}
		Set set = countryQuakes.entrySet();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			System.out.println("country : "+entry.getKey()+"; earthquakeNum : "+entry.getValue());
		}
	}
	
	
	// helper method to test whether a given earthquake is in a given country
	// This will also add the country property to the properties of the earthquake 
	// feature if it's in one of the countries.
	// You should not have to modify this code
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		// getting location of feature
		Location checkLoc = earthquake.getLocation();

		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		if(country.getClass() == MultiMarker.class) {
				
			// looping over markers making up MultiMarker
			for(Marker marker : ((MultiMarker)country).getMarkers()) {
					
				// checking if inside
				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));
						
					// return if is inside one
					return true;
				}
			}
		}
			
		// check if inside country represented by SimplePolygonMarker
		else if(((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			
			return true;
		}
		return false;
	}

	public static void main(String[] args){
		PApplet.main( new String[] { "--present", new EarthquakeCityMap().getClass().getName() });
	}
}

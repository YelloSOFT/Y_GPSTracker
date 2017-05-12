package ru.yellosoft_club.y_gpstracker;

public class Geo {

	public final static double DEG_TO_RAD = 0.0174532925199432958;
	
	/**Calculates distance between two points
	 * @param lat1 latitude of point 1, in degrees
	 * @param lon1 longitude of point 1, in degrees
	 * @param lat2 latitude of point 2, in degrees
	 * @param lon2 longitude of point 2, in degrees
	 * @return distance, in meters */
	public final static double distance(double lat1, double lon1, double lat2, double lon2) {
		double R = 6371.0; // km
		double dLat = (lat2 - lat1) * DEG_TO_RAD;
		double dLon = (lon2 - lon1) * DEG_TO_RAD; 
		double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0) + Math.cos(lat1 * DEG_TO_RAD) * Math.cos(lat2 * DEG_TO_RAD) * Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0);
		double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double d = R * c;
		
		return d * 1000.0;
	}
	
	public final static double e6ToDbl(int e6) {
		return (double)e6 / (double)1E6;
	}
	
	public final static int dblToE6(double dbl) {
		return (int) Math.round(dbl * 1E6);
	}
	
	/** Same as {@link Geo#distance(double, double, double, double)}, but uses E6 coordinates instead of double */
	public final static double distanceE6(int lat1E6, int lon1E6, int lat2E6, int lon2E6) {
		return distance(e6ToDbl(lat1E6), e6ToDbl(lon1E6), e6ToDbl(lat2E6), e6ToDbl(lon2E6));
	}
	
	/**Returns a boolean indicating whether the specified coordinate is valid.</br>
	 * A coordinate is considered invalid if it meets at least one of the following criteria:</br>
	 * <ol>
	 * <li>Its latitude is greater than 90 degrees or less than -90 degrees.</li>
	 * <li>Its longitude is greater than 180 degrees or less than -180 degrees.</li>
	 * </ol>
	 * @param lat latitude, in degrees
	 * @param lon longitude, in degrees
	 * @return true if the coordinate is valid or false if it is not.*/
	public final static boolean isCoordinateValid(double lat, double lon) {
		if (lat > 90.0 || lat < -90.0)
			return false;
		
		if (lon > 180.0 || lon < -180.0)
			return false;
		
		return true;
	}
	
}

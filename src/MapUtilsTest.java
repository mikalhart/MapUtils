import static java.lang.System.out;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Random;

public class MapUtilsTest 
{
	static final double austin_lat = 30.242434;
	static final double austin_lng = -97.826207;
	static final double maidenhead_lat = 51.521860;
	static final double maidenhead_lng = -0.721642;
	static final double morocco_lat = 31.745255;
	static final double morocco_lng = -7.987568;
	static final double lilongwe_lat = -13.967523;
	static final double lilongwe_lng = 33.765231;
	static final double daressalaam_lat = -6.764210;
	static final double daressalaam_lng = 39.208727;
	static final double malawi_s_lat = -17.123;
	static final double malawi_w_lng = 32.735;
	static final double malawi_n_lat = -9.254;
	static final double malawi_e_lng = 35.963;

	// Utility function
	private static void showLocalTimeAt(MapUtils mapUtils, String locName, double lat, double lng)
	{
		String zoneName = mapUtils.zoneNameFromGPS(lat, lng);
		LocalDateTime Now = LocalDateTime.now(ZoneOffset.UTC);
		LocalDateTime localNow = MapUtils.localTimeFromZoneName(Now, zoneName);
		String localNowString = localNow.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
		
		out.format("At %s %f, %f Zone: %s Localtime %s", locName, lat, lng, zoneName, localNowString);

		Optional<String> defaultLanguage = MapUtils.defaultLanguageFromZoneName(zoneName);
		if (defaultLanguage.isPresent())
			out.format(" Language: %s", defaultLanguage.get());
		
		Optional<SpecialRegionInfo> sri = MapUtils.getSpecialRegionInfo(lat, lng);
		if (sri.isPresent())
			out.format(" Override: %s (region %s)",  sri.get().overrideLanguage, sri.get().regionName);
		
		out.println();
	}
	
	// Main function
	public static void main(String[] args)
	{
		// Create the MapUtils object (this may take several seconds)
		out.println("*** Creating the MapUtils object ***");
        long start = System.currentTimeMillis();
		MapUtils mapUtils = new MapUtils();
        long total = System.currentTimeMillis() - start;
        out.println("MapUtils initialization took " + total + " milliseconds");
		
		out.println("*** Showing the time at various places around the world ***");
		showLocalTimeAt(mapUtils, "Lilongwe", lilongwe_lat, lilongwe_lng);
		showLocalTimeAt(mapUtils, "Dar es Salaam", daressalaam_lat, daressalaam_lng);
		showLocalTimeAt(mapUtils, "Austin", austin_lat, austin_lng);
		showLocalTimeAt(mapUtils, "Maidenhead", maidenhead_lat, maidenhead_lng);
		
		out.format("%n*** Showing the time at places on same latitude as Austin ***%n");
		for (int i=-180; i<180; i += 5)
			showLocalTimeAt(mapUtils, "Austin latitude", austin_lat, i);

		out.format("%n*** Showing the time at places on same latitude as Maidenhead ***%n");
		for (int i=-180; i<180; i += 5)
			showLocalTimeAt(mapUtils, "Maidenhead latitude", maidenhead_lat, i);

		out.format("%n*** Detecting summer time (DST) changes in Morocco ***%n");
		// Testing funky Moroccan timezone (DST changes sometime 4 times per year)
		for (LocalDateTime time = LocalDateTime.of(2018, 01, 01, 0, 0, 0);
				time.isBefore(LocalDateTime.of(2030, 1, 1, 0, 0, 0));
				time = time.plusMinutes(30))
		{
			String zoneName = mapUtils.zoneNameFromGPS(morocco_lat, morocco_lng);
			LocalDateTime lt1 = MapUtils.localTimeFromZoneName(time, zoneName);
			LocalDateTime lt2 = MapUtils.localTimeFromZoneName(time.plusMinutes(30), zoneName);
			if (!lt1.isEqual(lt2.minusMinutes(30)))
				out.format("Transition: At %f/%f in zone %s there was a time hiccup at %s%n", morocco_lat, morocco_lng, zoneName, lt2.toString());
		}

		out.format("%n*** Detecting summer time (DST) changes in Austin ***%n");
		for (LocalDateTime time = LocalDateTime.of(2018, 01, 01, 0, 0, 0);
				time.isBefore(LocalDateTime.of(2030, 1, 1, 0, 0, 0));
				time = time.plusMinutes(30))
		{
			String zoneName = mapUtils.zoneNameFromGPS(austin_lat, austin_lng);
			LocalDateTime lt1 = MapUtils.localTimeFromZoneName(time, zoneName);
			LocalDateTime lt2 = MapUtils.localTimeFromZoneName(time.plusMinutes(30), zoneName);
			if (!lt1.isEqual(lt2.minusMinutes(30)))
				out.format("Transition: At %f/%f in zone %s there was a time hiccup at %s%n", austin_lat, austin_lng, zoneName, lt2.toString());
		}

		out.format("%n*** Detecting summer time (DST) changes in Maidenhead ***%n");
		for (LocalDateTime time = LocalDateTime.of(2018, 01, 01, 0, 0, 0);
				time.isBefore(LocalDateTime.of(2030, 1, 1, 0, 0, 0));
				time = time.plusMinutes(30))
		{
			String zoneName = mapUtils.zoneNameFromGPS(maidenhead_lat, maidenhead_lng);
			LocalDateTime lt1 = MapUtils.localTimeFromZoneName(time, zoneName);
			LocalDateTime lt2 = MapUtils.localTimeFromZoneName(time.plusMinutes(30), zoneName);
			if (!lt1.isEqual(lt2.minusMinutes(30)))
				out.format("Transition: At %f/%f in zone %s there was a time hiccup at %s%n", maidenhead_lat, maidenhead_lng, zoneName, lt2.toString());
		}

		// Select 1000 random places in and around Malawi and see if there is
		// any "special" region that they are in
		out.format("%n*** 1000 random places in/around Malawi: are they in special language region? ***%n");
		for (int i=0; i<1000; ++i)
		{
			Random r = new Random();
			double randomLat = malawi_s_lat + (malawi_n_lat - malawi_s_lat) * r.nextDouble();
			double randomLng = malawi_w_lng + (malawi_e_lng - malawi_w_lng) * r.nextDouble();
			
			String zoneName = mapUtils.zoneNameFromGPS(randomLat, randomLng);
			Optional<String> defaultLanguage = MapUtils.defaultLanguageFromZoneName(zoneName);
			Optional<SpecialRegionInfo> sri = MapUtils.getSpecialRegionInfo(randomLat, randomLng);
			out.format("At %f/%f, the zone name is %s and the default language is %s%n",
				randomLat, randomLng, zoneName, 
				defaultLanguage.isPresent() ? defaultLanguage.get() : "UNKNOWN");
			if (sri.isPresent())
				out.format("   but it's in a ***SPECIAL REGION*** %s with override language %s%n", sri.get().regionName, sri.get().overrideLanguage);

		}
	}

}

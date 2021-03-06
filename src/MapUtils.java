import java.time.*;
import java.util.Optional;
import net.iakovlev.timeshape.TimeZoneEngine;

/**
 * Class {@link MapUtils} is used to generate locale-specific time and language information
 * based on latitude, longitude, and Universal Time -- values commonly generated by GPS modules.
 */
public class MapUtils
{
	private TimeZoneEngine engine = null;
	private static final ZoneId gmt = ZoneId.of("Etc/GMT");
	
    /**
     * Creates the {@link MapUtils} object.  This loads the timeshape engine and may take a long time.
     */
	MapUtils()
	{
		engine = TimeZoneEngine.initialize();
	}
	
    /**
     * Queries the {@link TimeZoneEngine} for a {@link java.time.ZoneId}
     * based on geo coordinates.
     *
     * @param lat  latitude part of query
     * @param lng longitude part of query
     * @return String representing official timezone name
     */
	public String zoneNameFromGPS(double lat, double lng)
	{
        Optional<ZoneId> s = engine.query(lat, lng);
        if (s.isPresent())
        {
        	return s.get().toString();
        }

        // if location is not in a legal (political) timezone, then synthesize one of the form Etc/GMTx
        else
        {
    		int offsetGMT = (int)((lng + 187.5f) / 15.0) - 12;
   	    	return offsetGMT == 0 ? "Etc/GMT" :
   	    		offsetGMT > 0 ? String.format("Etc/GMT-%d", offsetGMT) :
   	    		String.format("Etc/GMT+%d", -offsetGMT);
        }
	}
	
    /**
     * Transforms GPS (Universal) time to local time for the given timezone.
     *
     * @param gpsTime  time (UT) obtained from GPS device
     * @param zoneName name of timezone as returned by zoneNameFromGPS
     * @return the local time in the given timezone
     */
	public static LocalDateTime localTimeFromZoneName(LocalDateTime gpsTime, String zoneName)
	{
		ZonedDateTime gmtTime = ZonedDateTime.of(gpsTime, gmt);
		ZoneId zoneId = ZoneId.of(zoneName);
		return gmtTime.withZoneSameInstant(zoneId).toLocalDateTime();
	}
	
    /**
     * Returns the default language spoken in the given timezone.
     * based on geo coordinates.
     *
     * @param zoneName name of timezone as returned by zoneNameFromGPS
     * @return {@link Optional<String>#of(language)} if input corresponds
     * to a zone this library knows, or {@link Optional#empty()} otherwise.  Currently works
     * only for Malawi, Tanzania, USA and UK.
     */
	public static Optional<String> defaultLanguageFromZoneName(String zoneName)
	{
		switch(zoneName)
		{
		case "Africa/Blantyre" :
			return Optional.of("ny");
		case "Africa/Dar_es_Salaam" :
			return Optional.of("sw");
		case "America/Anchorage":
		case "America/Boise":
		case "America/Chicago":
		case "America/Denver":
		case "America/Detroit":
		case "America/Indiana/Indianapolis":
		case "America/Indiana/Knox":
		case "America/Indiana/Marengo":
		case "America/Indiana/Petersburg":
		case "America/Indiana/Tell_City":
		case "America/Indiana/Vevay":
		case "America/Indiana/Vincennes":
		case "America/Indiana/Winamac":
		case "America/Juneau":
		case "America/Kentucky/Louisville":
		case "America/Kentucky/Monticello":
		case "America/Los_Angeles":
		case "America/New_York":
		case "America/Nome":
		case "America/North_Dakota/Beulah":
		case "America/North_Dakota/Center":
		case "America/North_Dakota/New_Salem":
		case "America/Phoenix":
		case "America/Sitka":
			return Optional.of("en_US");
		case "Europe/London":
			return Optional.of("en_GB");
		}
		return Optional.empty();
	}
	
    /**
     * Returns "special region" info, if any exists, for given location.
     *
     * @param lat  latitude part of query
     * @param lng longitude part of query
     * @return {@link Optional<SpecialRegionInfo>#of(SpecialRegionInfo)} if provided location
     * is inside a known "special region" or {@link Optional#empty()} otherwise.
     */
	public static Optional<SpecialRegionInfo> getSpecialRegionInfo(double lat, double lng)
	{
		return SpecialRegionInfo.getSpecialRegionInfo(lat, lng);
	}
}


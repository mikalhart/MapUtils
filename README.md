# MapUtils
## Overview

**MapUtils** is a Java library built for onebillion’s Android tablets. Its general purpose
is to derive local time and language information from a GPS input stream for
devices that don’t have network connectivity and therefore cannot easily get
these data otherwise.

Specifically, given a latitude, a longitude, and the
Universal time—data readily obtained from GPS—calculate the _local_ time and the _local_ language in regions where tablets are deployed.

## Dependencies

**MapUtils** makes
broad use of the Java **[timeshape](https://github.com/RomanIakovlev/timeshape)** library,
which uses the polygons published by **[timezone-boundary-builder](https://github.com/evansiroky/timezone-boundary-builder/releases)**
to map location info to timezone. 
Timeshape can be automatically linked to your project by including the
following Maven dependency in pom.xml:

```xml
<dependency>
  <groupId>net.iakovlev</groupId>
  <artifactId>timeshape</artifactId>
  <version>2018d.3</version>
</dependency>
```

**MapUtils** (and **timeshape**) require Java 8, and rely especially
on its new time/date classes.

## API

**MapUtils** exports two
classes: **MapUtils** and **SpecialRegionInfo**. **MapUtils** is the general library engine object, with three public
static methods and one non-static method. 
**SpecialRegionInfo** is a simple
class with two public properties designed to encapsulate info about custom
regions.  The public methods in **MapUtils** are:


### zoneNameFromGPS

```
Access: non-static
Arguments: double lat, double lng
Returns: String
```

_Returns a canonical
timezone name, e.g., **America/Chicago**
or **Etc/GMT-10**, for the given
coordinate.  This always returns a
non-empty String for legal values of lat and lng (-90 &lt;= lat &lt;= 90 and
-180 &lt;= lng &lt;= 180).  Being
non-static, this method is requires the creation of a **MapUtils** object, which make take several seconds as it loads the **timeshape** engine. This is the only
method that makes use of **timeshape**._


### localTimeFromZoneName

```
Access: static
Arguments: LocalDateTime gpsTime, String zoneName
Returns: LocalDateTime
```
_Given the Universal
Time and the canonical name of the local timezone, this method returns the
local time equivalent of the provided gpsTime._


### defaultLanguageFromZoneName

```
Access: static
Arguments: String zoneName
Returns: Optional<String>
```

_Given the canonical
name of the local timezone, return the canonical code of the default language
that is spoken there, if known, and Optional.empty() if not._


### getSpecialRegionInfo

```
Access: static
Arguments: double lat, double lng
Returns: Optional<SpecialRegionInfo>
```

_Given a location,
return information about the smallest “special region” that contains that
coordinate, if any, or Optional.empty() if none._



The public properties in **SpecialRegionInfo** are:


### regionName

```
Type: String
```

_The name of the special region, i.e. “Dzaleka Refugee Camp”_
 

### overrideLanguage

```
Type: String
```
_The language spoken in the special region, which may differ from the default language of the enclosing
time zone or larger special regions._

## Usage

I imagine that **MapUtils**
will be mostly used at device startup to determine the local time and
language.  This is a five-step process:

* Create the **MapUtils** object.  This will take several seconds as the timeshape engine loads.

* Once the GPS location is known, determine the local timezone using **mapUtils.zoneNameFromGPS**.  After this completes the **MapUtils** object may be released: it will no longer be needed.

* Determine the local time in that timezone using the static method **MapUtils.localTimeFromZoneName**.

* Determine the default language in the timezone using the static method **MapUtils.defaultLanguageFromZoneName**.

* Determine whether the device is in any of onebillion’s “special language” regions using the static method **MapUtils.getSpecialRegionInfo**. If it is, use the override language provided in the returned value.

## Caveats

The **timeshape**
library is very memory consumptive—192 MB is the reported value—and takes a
very long time (multiple seconds) to load. 
I would recommend that the startup process described above be done in a background
thread.  As soon as the local timezone
name is known (acquired from **mapUtils.zoneNameFromGPS**),
the mapUtils object may be released, freeing this memory.  The static methods in **MapUtils** do not rely on **timeshape**.

## Maintenance

There are a few areas of this library that require immediate
or occasional curating:

* The **defaultLanguageFromZoneName** method uses a static internal lookup table that initially only covers Malawi, Tanzania, UK, and USA.  As onebillion expands to different regions, this table (a simple Java switch statement) will have to be extended.

* The **getSpecialRegionInfo** method uses a static polygon lookup table (found in the **SpecialRegionInfo** class) that should be updated to reflect all known onebillion special language regions. The easiest way to do this is for an expert to modify [this map](https://www.google.com/maps/@-12.0318197,33.0516939,8z/data=!3m1!4b1!4m2!6m1!1s1oFNef_5u61v5XSxIkxOVQ-YnFAsQKBZM).  Each special region is a polygon with a specially formatted name &lt;layer&gt;.&lt;region name&gt;, for example **1.Dzaleka Refugee Camp**.  The polygon description should contain the language spoken in that region, for example “Zambian English”.  The layer number is used for nested regions: when a given point is contained by more than one special region, the region with the higher layer number predominates. For example, a location that is within **1.Tumbuka Language Area** and also **2.Three Chichewa Villages** will be reported to be in the latter by **getSpecialRegionInfo**.  Once the map is complete, export it to KML using the built-in Google Maps tool “Export to KMZ/KML”.  The resulting KML file can be easily processed with a tool I’ve build to generate Java code exactly in the form expected by **SpecialRegionInfo**.

* Timezone definitions occasionally change.  The **timeshape** library should periodically be updated as needed.

## Future Questions

Direct any questions to me on this forum.

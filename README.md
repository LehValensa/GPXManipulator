# GPXManipulator
Tool to make changes in GPX files.
EARLY DEVELOPMENT STAGE.

# Use case
You just came back from a bicycle trip and want to share this trip with others. Connect your garmin device to the PC. Click on gps_sync.sh. Script will automatically do for you:
* waits until OS will mount garmin device
* copies new track from garmin to PC
* deletes trackpoints near your home location.
* uploads track to gpsies.com.
* launches browser with track uploaded to gpsies.
* unmounts garmin device
After script is finished, you can check it on gpsies, change track name to more readable than TrackYYYMMDD..., send an URL of track to your friends, so they can see where you've just been and how many you've ridden.

# Features
* Change track name.
* Delete points within predefined HotSpot area.
* Upload track to www.gpsies.com.
* Launch browser with track uploaded (visualize track).
* Add newlines to track and format it into human-readable form.
* Work through proxy with optional authentication

# Comparison to similar tools
* GPXManipulator does not change an XML structure of GPX. The structure of changed GPX remains the same, as in original file.
The only XML formatting is done using JAXB functions. JAXB does not change a structure (I believe).
* It is command-line tool, so it can be "injected" into custom chain of scripts. Once setup, then it does its job without human intervention.

# Requirements to run
* Ubuntu linux (or other recent linux). Windows will be OK for GPXManipulator itself.
* Java version 1.7 or higher. Perhaps, slightly lower versions will be OK too.
* Cygwin to run bash script gps_sync.sh in Windows

# How to use
* Download GPXManipulator.jar file.
* Run "java -jar GPXManipulator.jar [...command-line options...]"
* After options of GPXManipulator are tuned - insert them into gps_sync.sh script that is responsible to run the whole synchronization chain.

# Command-line options
	usage: java -jar GPXManupulator.jar <-i input_file.gpx> <-o output_file.gpx> [options]
	            where options are:
	 -d,--debug-mode                       Set debug level and detailed
	                                       tracing
	    --gpsies-activity <arg>            Assigned activity to track.
	                                       Example: walking
	    --gpsies-authenticate-hash <arg>   Use given authentication hash
	                                       instead of username/password
	    --gpsies-description <arg>         Description of track
	    --gpsies-launch-browser            Launch browser to see uploaded file
	    --gpsies-password <arg>            Password for GPSies upload
	    --gpsies-track-is-public           Make uploaded track visible to
	                                       other users
	    --gpsies-username <arg>            Username for GPSies upload
	 -h,--help                             Command line help
	    --hotspot-lat-max <arg>            Maximal lattitude value of HotSpot
	    --hotspot-lat-min <arg>            Minimal lattitude value of HotSpot
	    --hotspot-lon-max <arg>            Maximal lontitude value of HotSpot
	    --hotspot-lon-min <arg>            Minimal lontitude value of HotSpot
	    --http-proxy-host <arg>            Use given proxy host
	    --http-proxy-password <arg>        Use given password if proxy
	                                       requires authentication
	    --http-proxy-port <arg>            Use given proxy port
	    --http-proxy-use-system            Use proxy defined in operating
	                                       system
	    --http-proxy-user <arg>            Use given username if proxy
	                                       requires authentication
	 -i,--input-file <arg>                 Input GPX file
	 -o,--output-file <arg>                Output GPX file
	    --preserve-creator                 Do not change creator field, leave
	                                       original GPX creator.
	    --track-name <arg>                 Set new track name

If proxy should be used when uploading to GPSies, use Java option:

	-Djava.net.useSystemProxies=true
If proxy requires authentication, use following Java options:

	-Dhttp.proxyUser=${proxyUser} -Dhttp.proxyPassword=${proxyPassword}

Exploring password for GPSies in command line is not secure. Use authentication hash instead. To find out what is authentication hash for your login, upload some file to GPSies with debug level set on:

	java -jar GPXManipulator.jar -i somefile.gpx -d --gpsies-username <username> --gpsies-password <password> | grep authHash 
 
# Development
GPXManipulator is witten on Java using Eclipse IDE.
JAR is built by ANT.
Code uses:
    * - JAXB to read/write GPX file.
    * - Apache commons-cli library to read command-line options.
Nothing very special is used, so code should be portable.

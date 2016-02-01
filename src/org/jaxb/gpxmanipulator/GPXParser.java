package org.jaxb.gpxmanipulator;

import org.jaxb.gpxbind.GpxType;
import org.jaxb.gpxbind.TrkType;
import org.jaxb.gpxbind.TrksegType;
import org.jaxb.gpxbind.WptType;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;

// Parser that makes custom changes in GPX.
public class GPXParser {
	private static final Logger log = Logger.getLogger("");
	
	// Coordinates of security "Hot Spot".
	// Trackpoints inside of this HotSpot will be cleared from GPX.
	public static BigDecimal hotSpotLatMax = null;
	public static BigDecimal hotSpotLatMin = null;
	public static BigDecimal hotSpotLonMax = null;
	public static BigDecimal hotSpotLonMin = null;
	
	public static Integer pointsRemains = 0;	// Total count of trackpoints that remain in track after HotSpot processing.
	
	// Make changes in GPX.
    public void parseGPX(GpxType gpx, CommandLine cmd) throws Exception
    {
    	// Read and validate coordinates of HotSpot.
		this.setHotSpot(
					cmd.getOptionValue("hotspot-lat-min"),
					cmd.getOptionValue("hotspot-lat-max"),
					cmd.getOptionValue("hotspot-lon-min"),
					cmd.getOptionValue("hotspot-lon-max")
					);
    	
        // Change creator of GPX, since this program modifies GPX.
        // Preserve original creator at the end of string.
    	if ( ! cmd.hasOption("preserve-creator") )
    	{
    		log.info("Change GPX creator field");
            gpx.setCreator("org.jaxb.gpxmanipulator. Original - " + gpx.getCreator().toString());
    	}
    	
            // Go through all treckpoints in GPX file
            // and make necessary manipulations with them.
            log.info("Read treckpoints");
            pointsRemains=0;
            
            // Iterate over tracks
            List<TrkType> trkList = gpx.getTrk();
            for (int i = 0; i < trkList.size(); i++) {
                TrkType trk = trkList.get(i);
                log.finer( "Track name = [" + trk.getName() + "]");
                
                // Change track name
                // Note: if GPX contains several tracks, then all them will have the same name.
                //       But it is uncommon for GPX file to contain more than one track.
                trk.setName(cmd.getOptionValue("track-name", trk.getName()));
               
                // Iterate over segments
                List<TrksegType> segList = trk.getTrkseg();
                for (int j = 0; j < segList.size(); j++) {
                    TrksegType seg = segList.get(j);
                    log.finer( "Segment number [" + j + "]");
                   
                   
                    // Iterate over trackpoints
                    List<WptType> ptList = seg.getTrkpt();
                    for (int k = 0; k < ptList.size(); k++) {
                        WptType pt = ptList.get(k);
                        log.finer( 
                                "Track point number [" + k +
                                "] lat= [" + pt.getLat().toString() +
                                "] lon= [" + pt.getLon().toString() +
                                "] ele= [" + pt.getEle().toString() +
                                "] time=[" + pt.getTime().toString() +
                                "]"
                        );
                       
                        // Delete points near your "HotSpot" (home, office),
                        // in order to hide your actual location from others.
                        if (hotSpotLatMin != null) // Assuming: if at least one coordinate is set, then HotSpot is defined.
                        {
                        	// If Trackpoint is within HotSpot rectangle, then delete it.
	                        if ((( pt.getLat().compareTo(hotSpotLatMin) == 1 ) && ( pt.getLat().compareTo(hotSpotLatMax) == -1 ))
	                        	&&
	                        	(( pt.getLon().compareTo(hotSpotLonMin) == 1 ) && ( pt.getLon().compareTo(hotSpotLonMax) == -1 )))
	                        {
	                        	ptList.remove(k);
	                            k--;
	                            log.finer( "Point removed");
	                        }
                        }
                        
                    }
                    
                    pointsRemains += ptList.size();
                }
            }
    }
    
	// copy coordinates of security "Hot Spot" from command-line options.
	public void setHotSpot(String LatMin, String LatMax, String LonMin, String LonMax) throws Exception
	{
		// if at least one coordinate is set
		if ( ! ( LatMin == null && LatMax == null && LonMin == null && LonMax == null ))
		{
			// then check if all the rest coordinates of HotSpot rectangle are set
			if ( LatMin != null && LatMax != null && LonMin != null && LonMax != null)
			{
				hotSpotLatMin = new BigDecimal(LatMin);
				hotSpotLatMax = new BigDecimal(LatMax);
				hotSpotLonMin = new BigDecimal(LonMin);
				hotSpotLonMax = new BigDecimal(LonMax);
				log.info("Hot spot is defined");

			} else
			{
				throw new Exception("Not all 4 coordinates are set for HotSpot");
			}
		} else
		{
			// HotSpot is not defined
			return;
		}
	}
	
	public Integer getNumPoints() {
		return pointsRemains;
	}
}
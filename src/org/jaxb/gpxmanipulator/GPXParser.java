package org.jaxb.gpxmanipulator;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

// Parser that makes custom changes in GPX.
public class GPXParser {
	private static final Logger log = Logger.getLogger("");
	
    public void parseGPX(GpxType gpx) throws Exception
    {
            // Change creator of GPX, since this program modifies GPX.
            // Preserve original creator at the end of string.
            gpx.getCreator().toString();
            gpx.setCreator("org.jaxb.gpxmanipulator. Original - " + gpx.getCreator().toString());
           
            // go through all treckpoints in GPX file
            // and make necessary manipulations with them.
            List<TrkType> trkList = gpx.getTrk();
            for (int i = 0; i < trkList.size(); i++) {
                TrkType trk = trkList.get(i);
                log.finer( "Trek name = [" + trk.name + "]");
               
                List<TrksegType> segList = trk.getTrkseg();
                for (int j = 0; j < segList.size(); j++) {
                    TrksegType seg = segList.get(j);
                    log.finer( "Segment number [" + j + "]");
                   
                   
                    List<WptType> ptList = seg.getTrkpt();
                    for (int k = 0; k < ptList.size(); k++) {
                        WptType pt = ptList.get(k);
                        log.finer( 
                                "Trek point number [" + k +
                                "] lat= [" + pt.lat.toString() +
                                "] lon= [" + pt.lon.toString() +
                                "] ele= [" + pt.ele.toString() +
                                "] time=[" + pt.time.toString() +
                                "]"
                        );
                       
                        // Delete points near your "HotSpot" (home, office),
                        // in order to hide your actual location from others.
                        //BigDecimal latMin = new BigDecimal("50.4400000000");
                        //BigDecimal latMax = new BigDecimal("50.4800000000");
                       
                        BigDecimal latMin = new BigDecimal("50.4635");
                        BigDecimal latMax = new BigDecimal("50.4640");
                        if (( pt.lat.compareTo(latMin) == 1 ) && ( pt.lat.compareTo(latMax) == -1 )) {
                        	ptList.remove(k);
                            k--;
                            log.finer( "Point removed");
                        } else {
                        	log.finer( "Point remains in trek");
                        }
                    }
                }
            }
    }
}
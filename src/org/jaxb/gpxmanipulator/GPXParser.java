package org.jaxb.gpxmanipulator;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import javax.xml.bind.*;

public class GPXParser {
    public static void main(String[] args) {

      try {

            File gpxFile = new File("/home/leh/gpx/live/upload_test_nl_input4java.gpx");
            File parsedFile = new File("/home/leh/gpx/live/upload_test_nl_parsed.gpx");
            
            // AAA
    	  /*File gpxFile = new File("C:\\src\\java\\workspace\\upload_test_nl.gpx");
          File parsedFile = new File("C:\\src\\java\\workspace\\upload_test_nl_parsed.gpx");
          */
          
            JAXBContext jc = JAXBContext.newInstance("org.jaxb.gpxmanipulator");

            Unmarshaller unmarshaller = jc.createUnmarshaller();
            Marshaller jaxbMarshaller = jc.createMarshaller();
     
            // Unmarshal GpxType    
            GpxType gpx = (GpxType) JAXBIntrospector.getValue( unmarshaller.unmarshal(gpxFile) );
          
            // Change creator of GPX, since this program modifies GPX.
            // Preserve original creator at the end of string.
            gpx.getCreator().toString();
            gpx.setCreator("org.jaxb.gpxmanipulator. Original - " + gpx.getCreator().toString());
           
            // go through all treckpoints in GPX file
            // and make necessary manipulations with them.
            List<TrkType> trkList = gpx.getTrk();
            for (int i = 0; i < trkList.size(); i++) {
                TrkType trk = trkList.get(i);
                System.out.println("Trek name = [" + trk.name + "]");
               
                List<TrksegType> segList = trk.getTrkseg();
                for (int j = 0; j < segList.size(); j++) {
                    TrksegType seg = segList.get(j);
                    System.out.println("Segment number [" + j + "]");
                   
                   
                    List<WptType> ptList = seg.getTrkpt();
                    for (int k = 0; k < ptList.size(); k++) {
                        WptType pt = ptList.get(k);
                        System.out.println(
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
                            System.out.println("Point removed");
                        } else {
                            System.out.println("Point remains in trek");
                        }

                    }
                    
                }
            }
   
            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
   
            // Marshal GPX to file.
            jaxbMarshaller.marshal(gpx, parsedFile);
          
            // debug output of resulting GPX
            //jaxbMarshaller.marshal(gpx, System.out);

          } catch (JAXBException e) {
              e.printStackTrace();
          }

    }
}
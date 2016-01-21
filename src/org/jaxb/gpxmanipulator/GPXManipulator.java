package org.jaxb.gpxmanipulator;

import java.io.File;
import javax.xml.bind.*;

// Main class of GPXManipulator tool.
public class GPXManipulator {
	public static GpxType gpx;					// root object that contains whole GPX from XML file.
	public static JAXBContext jc;
	public static Unmarshaller jaxbUnmarshaller;
	public static Marshaller jaxbMarshaller;
	public static File gpxFile;					// input GPX file
	public static File parsedFile;				// output GPX file

	public static void main(String[] args) {

        gpxFile = new File("/home/leh/gpx/live/upload_test_nl_input4java.gpx");
        parsedFile = new File("/home/leh/gpx/live/upload_test_nl_parsed.gpx");
        
     // BBB
  	  /*File gpxFile = new File("C:\\src\\java\\workspace\\upload_test_nl.gpx");
        File parsedFile = new File("C:\\src\\java\\workspace\\upload_test_nl_parsed.gpx");
        */

	    // Read XML file and put its content into Java objects.  
        try {
	            jc = JAXBContext.newInstance("org.jaxb.gpxmanipulator");

	            jaxbUnmarshaller = jc.createUnmarshaller();
	            jaxbMarshaller = jc.createMarshaller();
	     
	            // Unmarshal GpxType    
	            gpx = (GpxType) JAXBIntrospector.getValue( jaxbUnmarshaller.unmarshal(gpxFile) );
	            
	      } catch (JAXBException e) {
              e.printStackTrace();
          }
	      
        // Make custom changes with XML (parse GPX)
	    GPXParser parser = new GPXParser();
	    parser.parseGPX(gpx);
	      
	    // Compose back changed XML and write it into the output file.
	    try {
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

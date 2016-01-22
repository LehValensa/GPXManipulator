package org.jaxb.gpxmanipulator;

import java.io.File;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import javax.xml.bind.*;

import java.util.logging.Logger;

// Main class of GPXManipulator tool.
public class GPXManipulator {
	public static GpxType gpx;					// root object that contains whole GPX from XML file.
	public static JAXBContext jc;
	public static Unmarshaller jaxbUnmarshaller;
	public static Marshaller jaxbMarshaller;
	public static File gpxFile;					// input GPX file
	public static File parsedFile = null;		// output GPX file
	private static final Logger log = Logger.getLogger("");

	public static void main(String[] args) throws Exception {
		
		// set up logger
        // LOG this level to the log
        log.setLevel(Level.FINER);

        ConsoleHandler handler = new ConsoleHandler();
        // PUBLISH this level
        handler.setLevel(Level.FINER);
        log.addHandler(handler);
        
        
		// Read command line arguments
		CliHandler clh = new CliHandler(args);

		gpxFile = new File(clh.getInputFile());
		
		if (clh.getOutputFile() != null)
			parsedFile = new File(clh.getOutputFile());
		
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
        try {
	    GPXParser parser = new GPXParser();
	    parser.parseGPX(gpx);
        } catch (Exception e) {
        	log.log(Level.SEVERE, "Unable to parse GPX");
        	e.printStackTrace();
        }
	      
	    // Compose back changed XML and write it into the output file.
	    try {
	          // output pretty printed
	          jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	 
	          // Marshal GPX to file.
	          if (parsedFile != null) {
	        	  jaxbMarshaller.marshal(gpx, parsedFile);
	          } else {
	        
	        	  // send resulting GPX to the screen
	        	  jaxbMarshaller.marshal(gpx, System.out);
	          }
	          
	    } catch (JAXBException e) {
              e.printStackTrace();
        }
	}

}

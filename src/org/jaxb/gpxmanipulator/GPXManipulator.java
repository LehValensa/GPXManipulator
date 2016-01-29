package org.jaxb.gpxmanipulator;

import java.io.File;
import java.io.PipedOutputStream;
import java.io.PipedInputStream;
import java.io.StringWriter;
import java.util.logging.Handler;
import java.util.logging.Level;
import javax.xml.bind.*;

import org.jaxb.gpxbind.GpxType;

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
	private static GPXParser parser =  new GPXParser();

	public static void main(String[] args) throws Exception {
		
		// Initial logger setup
		// Change format of log messages: single-row output, remove time and executing class.
        System.setProperty("java.util.logging.SimpleFormatter.format",
        		"[%4$s] %5$s%6$s%n");
		// Single-row, bug nothing is removed: "%1$tF %1$tT [%4$s] %2$s %5$s%6$s%n"
        
        
		// Read command line arguments
		CliHandler clh = new CliHandler(args);

		gpxFile = new File(clh.getInputFile());
		log.info("Config -> Input file is: " + clh.getInputFile());
		
		if (clh.getOutputFile() != null)
			parsedFile = new File(clh.getOutputFile());
	
		log.info("Config -> Output file is: " + clh.getOutputFile());
		
		if (clh.isDebugMode()) {
	
	        // Set detailed trace level for debug mode.
	        log.info("Config -> Debug level is set.");
			
			// Set new level for logger 
	        log.setLevel(Level.FINER);

			// Set new level for default ConsoleHandler too.
	        Handler[] h = log.getHandlers();
	        h[0].setLevel(log.getLevel());
	        
		}
		log.info("Config -> Logger level is: " + log.getLevel().toString());

		
		// Read XML file and put its content into Java objects.  
        try
        {
	            jc = JAXBContext.newInstance("org.jaxb.gpxmanipulator");

	            jaxbUnmarshaller = jc.createUnmarshaller();
	            jaxbMarshaller = jc.createMarshaller();
	     
	            // Unmarshal GpxType    
	            gpx = (GpxType) JAXBIntrospector.getValue( jaxbUnmarshaller.unmarshal(gpxFile) );
	            
	    }
        catch (JAXBException e)
        {
        	exitLog("Error: unable to read GPX file.", e);
        }
        
        
	      
        // Make custom changes with XML (parse GPX)
        try
        {
        	parser.parseGPX(gpx, clh.cmd);
        }
        catch (Exception e)
        {
        	exitLog("Error: unable to parse GPX", e);
        }
	      
	    // Compose back changed XML and write it into the output file.
        
       // output pretty printed
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        try
	    {
	          // Marshal GPX to file.
	          if (parsedFile != null)
	          {
	        	  jaxbMarshaller.marshal(gpx, parsedFile);
	          }
	          else
	          {
	        	  // send resulting GPX to the screen
	        	  jaxbMarshaller.marshal(gpx, System.out);
	          }
	          
	    }
	    catch (JAXBException e)
	    {
	    	exitLog("Error: unable to write GPX file", e);
        }
	    
        // Upload track to gpsies.com
        if ( clh.cmd.getOptionValue("gpsies-username") != null
          || clh.cmd.getOptionValue("gpsies-authenticate-hash") != null) {
	        
	        StringWriter gpxRaw = new StringWriter();
	        try {
		        jaxbMarshaller.marshal(gpx, gpxRaw);

		        // take track name from CLI, otherwise - from input filename.
		        String gpsiesFilename = clh.cmd.getOptionValue("track-name", gpxFile.getName());
		        gpsiesFilename = gpsiesFilename.replaceAll("(?i)\\.gpx", ""); // Remove .GPX extension
		        log.finer("gpsiesFilename=" + gpsiesFilename);
		        
		        // upload track
		        UploadGpsiesFunction gpsies_uploader = new UploadGpsiesFunction();
		        gpsies_uploader.startUpload(gpxRaw, gpsiesFilename, clh.cmd);
		        
	        } catch (Exception e) {
	          exitLog("Error: uploading to GPSies", e);
	        }
        }
        
        System.exit(ExitCode.EXIT_OK.getId());
	}
	
	private static void exitLog(String message, Exception e)
	{
    	log.log(Level.SEVERE, message + " : " +e.getLocalizedMessage());
        System.exit(ExitCode.EXIT_RUNTIME_ERROR.getId());
	}

}

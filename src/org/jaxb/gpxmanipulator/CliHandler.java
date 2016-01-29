package org.jaxb.gpxmanipulator;

import java.util.logging.Logger;
import org.apache.commons.cli.*;

// Handler of options passed via Command Line Interface.
// Usage example is taken from:
//    http://www.javadocexamples.com/java_source/org/biojava/app/BioGetSeq.java.html
/* My options: -i "C:\\src\\java\\workspace\\upload_test_nl.gpx" -o "C:\\src\\java\\workspace\\upload_test_nl_parsed.gpx"
 */
public class CliHandler
{
	 private static final Logger log = Logger.getLogger("");
	 private String[] args;
	 public  Options opts;
	 public CommandLine cmd;

	 public CliHandler(String[] args) throws Exception
	 {
		  this.args = args;
		  opts = createOptions();
		  this.parse();
	 }
	 
	 private static Options createOptions()
     {
	    Options opts = new Options();
	    boolean hasArg = true;
	    OptionGroup groupRequired = new OptionGroup(); // at least 1 of options from this group is mandatory
	    												// Create another group if there is another mandatory option.
	    groupRequired.setRequired(true);
	    
	    // Define command-line options.
	    // Mandatory options go to a special group(s),
	    // the rest options are just added to "opts" list.
	    Option inputFile = new Option("i", "input-file", hasArg, "Input GPX file");
	    groupRequired.addOption(inputFile); // set this option mandatory (either -i <GPX> or -h should be specified)
	
	    opts.addOption(new Option("o",	"output-file",			hasArg, "Output GPX file"));
	    opts.addOption(new Option("d",	"debug-mode",			! hasArg, "Set debug level and detailed tracing"));
	    opts.addOption(new Option(null,	"hotspot-lat-min",		hasArg, "Minimal lattitude value of HotSpot"));
	    opts.addOption(new Option(null,	"hotspot-lat-max",		hasArg, "Maximal lattitude value of HotSpot"));
	    opts.addOption(new Option(null,	"hotspot-lon-min",		hasArg, "Minimal lontitude value of HotSpot"));
	    opts.addOption(new Option(null,	"hotspot-lon-max",		hasArg, "Maximal lontitude value of HotSpot"));
	    opts.addOption(new Option(null,	"preserve-creator",		! hasArg, "Do not change creator field, leave original GPX creator."));
	    opts.addOption(new Option(null,	"track-name",			hasArg, "Set new track name"));
	    opts.addOption(new Option(null,	"gpsies-username",		hasArg, "Username for GPSies upload"));
	    opts.addOption(new Option(null,	"gpsies-password",		hasArg, "Password for GPSies upload"));
	    opts.addOption(new Option(null,	"gpsies-authenticate-hash",	hasArg, "Use given authentication hash instead of username/password"));
	    opts.addOption(new Option(null,	"gpsies-track-is-public",	! hasArg, "Make uploaded track visible to other users"));
	    opts.addOption(new Option(null,	"gpsies-activity",		hasArg, "Assigned activity to track. Example: walking"));
	    opts.addOption(new Option(null,	"gpsies-description",	hasArg, "Description of track"));
	    opts.addOption(new Option(null,	"gpsies-launch-browser",! hasArg, "Launch browser to see uploaded file"));

	    Option help = new Option("h",	"help",				! hasArg, "Command line help");
	    groupRequired.addOption(help); // set this option mandatory (either -i <GPX> or -h should be specified)
	    
	    opts.addOptionGroup(groupRequired); // at least 1 of these options should be specified in this group
	    return opts;
      }
	 
	 // "Getters" for some command-line options that are used by main() block.
	 // The rest options are passed to GPX parser and used there directly,
	 // so no need to define getters for them here. 
	 public String getInputFile()
	 {
		 return cmd.getOptionValue("i");
	 }
	 
	 public String getOutputFile()
	 {
		 return cmd.getOptionValue("o");
	 }
	 
	 public boolean isDebugMode()
	 {
		 return cmd.hasOption("d");
	 }
	 
	 public void parse()
	 {
		 try
		 {
			 cmd = new GnuParser().parse(opts, args);
			 
			 if (cmd.hasOption("h"))
				 exitHelp(opts, ExitCode.EXIT_OK.getId(), null);
		 }
		 catch (Exception e)
		 {
				exitHelp(opts, ExitCode.EXIT_CONFIG_ERROR.getId(), e.getLocalizedMessage());
		 }
	 }

	 public void exitHelp(Options opts, int exitValue, String message)
	 {
		 if (exitValue != 0)
			 log.severe("Failed to parse comand line properties\n" + message);
		 
		 HelpFormatter formater = new HelpFormatter();
		 formater.printHelp("java org.jaxb.gpxmanipulator.GPXManupulator [options]\nwhere options are:", opts);
		 System.exit(exitValue);
	 }	 
}
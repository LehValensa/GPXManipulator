package org.jaxb.gpxmanipulator;

import java.util.logging.Level;
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
	 private static Options opts;
	 public static CommandLine cmd;

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
	    
	    // Define options
	    Option inputFile = new Option("i", "input-file", hasArg, "Input GPX file");
	    groupRequired.addOption(inputFile); // set this option mandatory (either -i <GPX> or -h should be specified)
	
	    Option outputFile = new Option("o", "output-file", hasArg, "Output GPX file");
	    opts.addOption(outputFile);
	
	    Option help = new Option("h", "help", ! hasArg, "Command line help");
	    opts.addOption(help);
	    groupRequired.addOption(help); // set this option mandatory (either -i <GPX> or -h should be specified)
	    
	    opts.addOptionGroup(groupRequired); // at least 1 of these options should be specified in this group
	    return opts;
      }
	 
	 public String getInputFile()
	 {
		 return cmd.getOptionValue("i");
	 }
	 
	 public String getOutputFile()
	 {
		 return cmd.getOptionValue("o");
	 }
	 
	 public void parse()
	 {
		 try
		 {
			 cmd = new GnuParser().parse(opts, args);
			 
			 if (cmd.hasOption("h"))
				 exitHelp(opts, 0, "");
		 }
		 catch (Exception e)
		 {
				exitHelp(opts, 1, e.getMessage());
		 }
	 }

	 private void exitHelp(Options opts, int exitValue, String message)
	 {
		 if (exitValue == 0)
			 //System.out.println(message);
			 log.info(message);
		 else
			 log.severe("Failed to parse comand line properties\n" + message);
			 //System.err.println(message);
		 
		 HelpFormatter formater = new HelpFormatter();
		 formater.printHelp("java org.jaxb.gpxmanipulator.GPXManupulator", opts);
		 System.exit(exitValue);
	 }	 
}
package org.jaxb.gpxmanipulator;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.*;

// Handler of options passed via Command Line Interface.
// TODO: refactor using http://www.javadocexamples.com/java_source/org/biojava/app/BioGetSeq.java.html

public class CliHandler {
	 private static final Logger log = Logger.getLogger(CliHandler.class.getName());
	 private String[] args = null;
	 private Options options = new Options();
	 private CommandLine cmd = null;

	 public CliHandler(String[] args) {

	  this.args = args;

	  options.addOption("h", "help", false, "show help.");
	  options.addOption("i", "input-file", true, "Input GPX file");
	  options.addOption("o", "output-file", true, "Output GPX file");
	  options.getOption("o").setRequired(true);

	 }

	 public String getInputFile() {
		 return cmd.getOptionValue("i");
	 }
	 
	 public String getOutputFile() {
		 return cmd.getOptionValue("o");
	 }
	 
	 public void parse() {
	  //CommandLineParser parser = new BasicParser();
		 CommandLineParser parser = new GnuParser();
	  
	  try {
	   cmd = parser.parse(options, args);

	   if (cmd.hasOption("h"))
	    help();
/*
	   if (cmd.hasOption("i")) {
	    log.log(Level.INFO, "Using cli argument -i=" + cmd.getOptionValue("i"));
	    //inputf=cmd.getOptionValue("i");
	    // Whatever you want to do with the setting goes here
	    
	   } else {
	    log.log(Level.SEVERE, "MIssing i option");
	    help();
	   }
*/
	  } catch (ParseException e) {
	   log.log(Level.SEVERE, "Failed to parse comand line properties\n" + e.getMessage());
	   help();
	  }
	 }

	 private void help() {
	  // This prints out some help
	  HelpFormatter formater = new HelpFormatter();

	  formater.printHelp("GPXManupulator", options);
	  System.exit(0);
	 }
	}

/*
import org.apache.commons.cli.*;

public class CliHandler {
 
    public final static String INPUT_FILE = "input-file";
    //public final static String USER_PER_ROOM = "user-per-room";
 
    private Options options;
    private CommandLine line;
 
    public CliHandler() {args
 
        options = new Options();
        final Option configFileOption = Option.builder("cf")
                .argName("configfile")
                .hasArg()
                .desc("Config file for Genome Store")
                .build();
        
        options.addOption( Option.Builder.withLongOpt(INPUT_FILE)
                                .withDescription( "Input GPX file" )
                                .hasArg()
                                .withArgName("NUMBER")
                                .create());
 
        options.addOption(Option.Builder.withLongOpt("help")
                .withDescription("Print help")
                .create("h"));

    }
 
    public void parse(String[] args) throws Exception{
 
        CommandLineParser parser = new PosixParser();
        line = parser.parse(options, args);
        if(line.hasOption("help")) {
            throw new Exception("Print help and exit");
        }
    }
 
    public Integer getInputFile() {
        return getIntOption(INPUT_FILE);
    }
 
 
    private Integer getIntOption(String optionName) {
        Integer val = 0;
        if( line.hasOption( optionName ) ) {
            try {
                val = Integer.parseInt(line.getOptionValue( optionName ));
                val = val > 0 ? val : -val;
            } catch (NumberFormatException e) {
            }
            return val;
        } else {
            return val;
        }
    }
 
    public void printCliHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("chat application", "Read following instructions for tuning chat work",
                options, "Developed by Acestime.Com");
    }
}
*/
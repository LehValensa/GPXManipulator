package org.jaxb.gpxmanipulator;

import java.util.logging.Logger;

// Enumeration of exit codes used in the program
public enum ExitCode {
	EXIT_OK				(0, "Program terminated successfully"),
	EXIT_CONFIG_ERROR	(1, "Error in configuration or command line parameters"),
	EXIT_RUNTIME_ERROR	(2, "Runtime error occured"),
	EXIT_NO_POINTS		(3, "No points in output track");

	private final int id;
	private final String msg;
	
	private static final Logger log = Logger.getLogger("");

	ExitCode(int id, String msg) {
	  this.id = id;
	  this.msg = msg;
	}

	public int getId() {
	  return this.id;
	}

	public String getMsg() {
	  return this.msg;
	}
	
	// Exit program due to error.
	public static void exitLog(String message, Exception e)
	{
    	log.severe(message + " : " + e.getLocalizedMessage());
        System.exit(EXIT_RUNTIME_ERROR.getId());
	}

	// Exit program normally
	public static void exitLog(String message)
	{
	    log.info(message);
	    System.exit(EXIT_OK.getId());
	}

	// Exit program with given exit code
	public static void exitNolog(int exitValue)
	{
		System.exit(exitValue);
	}


}

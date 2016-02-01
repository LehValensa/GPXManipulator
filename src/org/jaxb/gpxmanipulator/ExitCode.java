package org.jaxb.gpxmanipulator;

// Enumeration of exit codes used in the program
public enum ExitCode {
	EXIT_OK				(0, "Program terminated successfully"),
	EXIT_CONFIG_ERROR	(1, "Error in configuration or command line parameters"),
	EXIT_RUNTIME_ERROR	(2, "Runtime error occured"),
	EXIT_NO_POINTS		(3, "No points in output track");

	private final int id;
	private final String msg;

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

}

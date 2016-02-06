package org.jaxb.gpxmanipulator;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public class Tools {
	private static final Logger log = Logger.getLogger("");
	
	// Login to http proxy
	private static String proxyUser;
	private static String proxyPassword;

	// prepare password hash	
	public static String getAuthHash (String username, String password) {
		String validationString = username + "|" + DigestUtils.md5Hex(password);
		String encryptedString = new String(Base64.encodeBase64(validationString.getBytes()));
		return encryptedString;
	}
	
	// setup system to use HTTP proxy
	public static void setupProxy (CommandLine cmd) {
		
		// Use proxy host and port defined in OS (works in Windows and Gnome 2 only)
		if (cmd.hasOption("http-proxy-use-system")) {
			System.setProperty("java.net.useSystemProxies", "true");
			log.info("System proxy will be used");
		}
		
		// Use proxy host and port defined in command line
		// (overrides use of system proxy)
		String proxyHost = cmd.getOptionValue("http-proxy-host");
		String proxyPort = cmd.getOptionValue("http-proxy-port", "80");
		log.fine("proxyHost=[" + proxyHost + "] proxyPort=[" + proxyPort + "]");
		
		if (proxyHost != null) {
				System.setProperty("http.proxyHost", proxyHost);
				System.setProperty("http.proxyPort", proxyPort);
		}

		// Setup authentication if proxy requires it


		proxyUser = cmd.getOptionValue("http-proxy-user");
		proxyPassword = cmd.getOptionValue("http-proxy-password");
		// Get authentication directly from Java properties.
		// This way is a bit incorrect, since overrides properties for SOCKS authentication.
		//proxyAuthUser = System.getProperty("http.proxyUser");
		//proxyAuthPassword = System.getProperty("http.proxyPassword");
		log.fine("proxyUser=[" + proxyUser + "] proxyPassword=[" + proxyPassword + "]");

		if (proxyUser != null && proxyPassword != null) {
				Authenticator.setDefault(new Authenticator() {
					@Override
					protected PasswordAuthentication
					getPasswordAuthentication() {
						//respond only to proxy auth requests
						if (getRequestorType().equals(RequestorType.PROXY)) {
							return new PasswordAuthentication(
									proxyUser,
									proxyPassword.toCharArray());
						} else {
							return null;
						}
					}
				});
		}
		
	}
	
	// Convert input stream to string. For debugging purposes
	// Used to test web page download.
	/*
	@SuppressWarnings("resource")
	public static String getWebPageToString(String url) {
		java.util.Scanner s = null;
		String result = null;

		try {
			URL server = new URL(url);
			HttpURLConnection connection = (
				    HttpURLConnection)server.openConnection();
			connection.connect();
			InputStream is = connection.getInputStream();
			s = new java.util.Scanner(is).useDelimiter("\\A");
			result = s.hasNext() ? s.next() : "";
		} catch (MalformedURLException e) {}
		  catch (IOException ioe) {}
		finally {
			s.close();
		}
		return result;
	}
	 */

}

package org.jaxb.gpxmanipulator;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;


/**
 * Function to upload track information up to Gpsies.com
 * Leh: template is taken from gpsprune software and then reduced for batch application.
 */
public class UploadGpsiesFunction
{
	private static final Logger log = Logger.getLogger("");

	// Writer object for GPX export
	private OutputStreamWriter _writer = null;
	// URL to post form to
	private static final String GPSIES_URL = "http://www.gpsies.com/upload.do";
	// Keys for describing activities
	private static final String[] ACTIVITY_KEYS = {"trekking", "walking", "jogging",
			"mountainbiking", "biking", "motorbiking", "snowshoe", "sailing", "skating"};

	// default activity, if none specified
	private static final String DEF_ACTIVITY = ACTIVITY_KEYS[3];

	// Login to http proxy
	private static String proxyUser;
	private static String proxyPassword;


	// Convert input stream to string. For debugging purposes
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

	/**
	 * Start the upload process (require separate thread?)
	 * @param gpx 
	 */
	public void startUpload(final StringWriter gpxRaw, String _filename, CommandLine cmd) throws Exception
	{
		BufferedReader reader = null;

		try
		{
			// Setup proxy and its authentication, if specified
			setupProxy(cmd);

			// Establish connection to GPSies.
			FormPoster poster = new FormPoster(new URL(GPSIES_URL));
			log.fine("Connection to GPSies established");

			// fill in the form, add GPX file and submit the form
			poster.setParameter("device", "Prune"); // use default device name inherited from gpsprune

			// Use either username/password or authentication hash for login to GPSies.
			String username = cmd.getOptionValue("gpsies-username");
			String password = cmd.getOptionValue("gpsies-password");
			String authHash = cmd.getOptionValue("gpsies-authenticate-hash");
			if (username != null && password != null) {
				// Send plain username/password pair. It works, but insecure. Commented out.
				//poster.setParameter("username", _username);
				//poster.setParameter("password", _password);

				// Send encrypted hash instead of plain username/password pair
				authHash = getAuthHash(username, password);
				
			} else if (authHash != null) {
				// NOP
			} else {
				throw new Exception("Either username/password or authenticate hash should be specified.");
			}
			// Show hash for reuse it directly in command-line
			log.fine("authHash: " + authHash );

			poster.setParameter("authenticateHash", authHash);

			poster.setParameter("trackTypes", cmd.getOptionValue("gpsies-activity", DEF_ACTIVITY));
			poster.setParameter("filename", _filename);
			poster.setParameter("fileDescription", cmd.getOptionValue("gpsies-description", ""));
			poster.setParameter("startpointCountry", "DE");
			poster.setParameter("endpointCountry", "DE"); // both those will be corrected by gpsies
			// set track to private if not specified in command-line
			poster.setParameter("status", (cmd.hasOption("gpsies-track-is-public")?"1":"3") ); 
			poster.setParameter("submit", "speichern"); // required

			// Use Pipes to connect the gpxRaw's output with the FormPoster's input
			PipedInputStream iStream = new PipedInputStream();
			PipedOutputStream oStream = new PipedOutputStream(iStream);
			_writer = new OutputStreamWriter(oStream);

			// piped streams should be used in separate threads
			new Thread(new Runnable() {
				public void run() {
					try {
						_writer.write(gpxRaw.toString());
					} catch (IOException e) {}
					finally {
						try {_writer.close();} catch (IOException e) {}
					}
				}
			}).start();
			poster.setParameter("formFile", "filename.gpx", iStream);

			// post track to GPSies and analyze response from GPSies.
			BufferedInputStream answer = new BufferedInputStream(poster.post());
			int response = poster.getResponseCode();
			reader = new BufferedReader(new InputStreamReader(answer));
			String line = reader.readLine();

			// Try to extract gpsies page url from the returned message
			String pageUrl = null;

			if (response == 200 && line.substring(0, 2).toUpperCase().equals("OK"))
			{
				final int bracketPos = line.indexOf('[');
				if (bracketPos > 0 && line.endsWith("]")) {
					pageUrl = line.substring(bracketPos + 1, line.length()-1);
				}
			}
			if (pageUrl != null)
			{
				log.info("PageURL: " + pageUrl);
				if (cmd.hasOption("gpsies-launch-browser"))
					BrowserLauncher.launchBrowser(pageUrl);
			}
			else {
				log.severe("ERROR: null pageUrl from gpsies.");
			}
		}
		catch (MalformedURLException e) {}
		catch (IOException ioe) {
			log.severe("ERROR: IOExeption:" + ioe.getLocalizedMessage());
		}
		finally {
			try {if (reader != null) reader.close();} catch (IOException e) {}
		}

	}

	// prepare password hash	
	private String getAuthHash (String username, String password) {
		String validationString = username + "|" + DigestUtils.md5Hex(password);
		String encryptedString = new String(Base64.encodeBase64(validationString.getBytes()));
		return encryptedString;
	}

	// setup system to use HTTP proxy
	private void setupProxy (CommandLine cmd) {
		
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

}


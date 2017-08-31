package pt.healthysystems.anonhl7;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

public class HttpJSON {
	
	public static JSONObject get(URL url) throws MalformedURLException, IOException, JSONException {
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();		// TODO: Change to HttpsURLConnection
		
		// add request header
		connection.setRequestMethod("GET");
		connection.setRequestProperty("User-Agent", "HS.Farmalerta");
		connection.setRequestProperty("Accecpt", "application/json");
		
		int responseCode = connection.getResponseCode();
		if(responseCode != HttpURLConnection.HTTP_OK)
			throw new IOException("Invalid response code: " + responseCode);

		// Response from the Web Service
		return readResponseAsJSON(connection.getInputStream());
	}

	public static JSONObject post(URL url, JSONObject json) throws MalformedURLException, IOException, JSONException {
		return post(url, json.toString());
	}
	
	public static JSONObject post(URL url, String post) throws MalformedURLException, IOException, JSONException {
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();		// TODO: Change to HttpsURLConnection
		
		// add request header
		connection.setRequestMethod("POST");
		connection.setRequestProperty("User-Agent", "hl7 Anon");
		connection.setRequestProperty("Accecpt", "application/json");
		connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		
		// Send post request
		connection.setDoOutput(true);
		DataOutputStream out = new DataOutputStream(connection.getOutputStream());
		out.write(post.getBytes("UTF-8"));
		out.flush();
		out.close();
		
		int responseCode = connection.getResponseCode();
		if(responseCode != HttpURLConnection.HTTP_OK)
			throw new IOException("Invalid response code: " + responseCode);

		// Response from the Web Service
		//return readResponseAsJSON(connection.getInputStream());
		return new JSONObject();
	}
	
	
	public static JSONObject readResponseAsJSON(InputStream is) throws IOException, JSONException {
		// Response from the Web Service
		InputStreamReader streamReader = new InputStreamReader(is);
		BufferedReader reader = new BufferedReader(streamReader);
		String inputLine;
		StringBuffer response = new StringBuffer();
		
		while ((inputLine = reader.readLine()) != null)
			response.append(inputLine);
		reader.close();
		
		// return result
		return new JSONObject(response.toString());
	}
	
	
	/**
	 * Create a new authentication token
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String authentication() {
		try {
			String key = new BigInteger(512, new Random(Long.parseLong("v8jk3n89v", 36))).toString(36);
			String salt = UUID.randomUUID().toString().substring(0, 8);
			BigInteger bigInteger = new BigInteger(MessageDigest.getInstance("SHA-256").digest((key + salt).getBytes()));
			String hash = (bigInteger.signum() < 0 ? bigInteger.negate() : bigInteger).toString(36);
			return salt + hash;
		} catch(NoSuchAlgorithmException e) {
			return "";
		}
	}
	/**
	 * Check if the authentication token is valid
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static boolean authentication(String auth) {
		try {
			if(!(auth instanceof String) || auth.length() < 50) return false;
			String salt = auth.substring(0, 8);
			String hash = auth.substring(8);
			String key = new BigInteger(512, new Random(Long.parseLong("v8jk3n89v", 36))).toString(36);
			BigInteger bigInteger = new BigInteger(MessageDigest.getInstance("SHA-256").digest((key + salt).getBytes()));
			String newHash = (bigInteger.signum() < 0 ? bigInteger.negate() : bigInteger).toString(36);
			return newHash.equals(hash);
		} catch(NoSuchAlgorithmException e) {
			return false;
		}
	}
}

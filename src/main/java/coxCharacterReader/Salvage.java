package coxCharacterReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class Salvage {
	private static final String LOGIN_URL = "https://www.cityofheroesrebirth.com/public/login";
	private static final String MANAGE_URL = "https://www.cityofheroesrebirth.com/public/manage";
	private static final String CHAR_PAGE_URL = "https://www.cityofheroesrebirth.com/public/api/character/raw?q=";

	public static void main(String[] args) throws ClientProtocolException, IOException, URISyntaxException {
		Properties p = getConfig();
        CookieStore cookieStore = new BasicCookieStore();
        HttpClientContext  localContext = HttpClientContext.create();
        localContext.setCookieStore(cookieStore);
        
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(LOGIN_URL);
		request.addHeader("User-Agent", "Apache HTTPClient");
		HttpResponse response = client.execute(request, localContext);
		HttpEntity entity1 = response.getEntity();
		String content = EntityUtils.toString(entity1);
		
		List<NameValuePair> parms = parseLogin(content, null);
		parms.add(new BasicNameValuePair("username", p.getProperty("username")));
		parms.add(new BasicNameValuePair("password", p.getProperty("password")));
		parms.add(new BasicNameValuePair("nextpage", "login"));
		parms.add(new BasicNameValuePair("submit", "login"));
		loginAttempt(LOGIN_URL, parms,  localContext);
		request = new HttpGet(MANAGE_URL);
		request.addHeader("User-Agent", "Apache HTTPClient");
		response = client.execute(request, localContext);
		entity1 = response.getEntity();
		content = EntityUtils.toString(entity1);
		List<String> charIds = getCharacterIds(content);
		Map<String, Map> matrix = new HashMap<String, Map>();
		List<String> salvageTypes = new ArrayList<String>();
		List<String> characters = new ArrayList<String>();
		for(String charid : charIds) {
			String apiUrl = CHAR_PAGE_URL + charid;
			client = HttpClientBuilder.create().build();
			request = new HttpGet(apiUrl);

			request.addHeader("User-Agent", "Apache HTTPClient");
			response = client.execute(request);

			HttpEntity entity = response.getEntity();
			String charContent = EntityUtils.toString(entity);
			String name = parseCharName(charContent);
			/* Unfortunately the url doesn't always work 
			 * So skip if the name cannot be parsed */
			if (name != null ) {
				System.out.println(String.format("Processing %s", name));
				Map<String, String> salvage = parseSalvage(charContent);
				for (String k :salvage.keySet()) {
					if (!salvageTypes.contains(k)) {
						salvageTypes.add(k);
					}
				}
				matrix.put(name, salvage);
				characters.add(name);
			}
		}
		Collections.sort(salvageTypes);
		Collections.sort(characters);
		File file = new File(p.getProperty("filename"));
		BufferedWriter writer = new BufferedWriter(new FileWriter(file,true));
		writer.write("Character," + String.join(",", salvageTypes)+"\n");
		for (String c : characters) {
			writer.write(String.format("\"%s\"", c));
			Map<String, String> salvage = matrix.get(c);
			for (String s : salvageTypes) {
				if (salvage.containsKey(s)) {
					writer.write(String.format(",%s", salvage.get(s)));
				} else {
					writer.write(",0");
				}
			}
			writer.write("\n");
		}
		writer.write("\n");
		writer.close();
		System.out.println("END");
	}

	private static String loginAttempt(String url, List<NameValuePair> parms, HttpContext localContext) throws IOException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parms, Consts.UTF_8);

            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(entity);

            // Create a custom response handler
            ResponseHandler < String > responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 500) {
                    HttpEntity responseEntity = response.getEntity();
                    return responseEntity != null ? EntityUtils.toString(responseEntity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };
            String responseBody = httpclient.execute(httpPost, responseHandler, localContext);
            return responseBody;
        }
	}

	private static List<NameValuePair> parseLogin(String content, List<NameValuePair> parms) {
		if (parms == null) 
			parms = new ArrayList<>();
		Reader inputString = new StringReader(content);
		BufferedReader reader = new BufferedReader(inputString);
		String line;
		try {
			line = reader.readLine();
			while (line != null) {
				if(line.contains("name=\"csrf_name\"")) {
					parms.add(new BasicNameValuePair("csrf_name", extractValue(line)));
				} else if (line.contains("name=\"csrf_value\"")) {
					parms.add(new BasicNameValuePair("csrf_value", extractValue(line)));
				}
				line = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return parms;
	}

	private static String parseCharName(String content) {
		final String regex = "^Name \\\"(.+)\\\"";
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
        	return matcher.group(1);
        }
		return null;
	}

	private static String extractValue(String line) {
		String regex = "value=\\\"(.+)\\\"";
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
        	return matcher.group(1);
        }
		return null;
	}

	private static List<String> getCharacterIds(String manageContent) {
		List<String> ids = new ArrayList<String>();
		final String regex = "character/raw\\?q=(.+)\\\"";
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(manageContent);
        while (matcher.find()) {
        	ids.add(matcher.group(1));
        }
        return ids;
	}

	private static Map<String, String> parseSalvage(String content) {
		Map<String, String> salvage = new HashMap<String, String>();
        final String regex = "InvSalvage0\\[0].S_(\\S+)\\s(\\S+)";
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
        	salvage.put(matcher.group(1), matcher.group(2));
        }
		return salvage;
	}
	private static Properties getConfig() throws IOException {
		FileReader reader = new FileReader("properties.cfg");
		Properties p = new Properties();
		p.load(reader);
		if (p.getProperty("username") != null &&
				p.getProperty("password") != null &&
				p.getProperty("filename") != null) {
			return p;
		}
		throw new IOException("Invalid Config File: Missing Parameters");
	}
}

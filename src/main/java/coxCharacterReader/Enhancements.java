package coxCharacterReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
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
import org.codehaus.plexus.util.StringUtils;

public class Enhancements {
	private static final String LOGIN_URL = "https://www.cityofheroesrebirth.com/public/login";
	private static final String MANAGE_URL = "https://www.cityofheroesrebirth.com/public/manage";
	private static final String CHAR_PAGE_URL = "https://www.cityofheroesrebirth.com/public/api/character/raw?q=";

	public static void main(String[] args) throws ClientProtocolException, IOException, URISyntaxException {
		Properties p = getConfig();
		
		List<String> recipeTypes = new ArrayList<String>();
		List<String> characters = new ArrayList<String>();

		for (RebirthAccount account : getAccounts(p)) {
			System.out.println(account.getUsername());
			System.out.println(StringUtils.repeat("=", account.getUsername().length()));
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
			parms.add(new BasicNameValuePair("username", account.getUsername()));
			parms.add(new BasicNameValuePair("password", account.getPassword()));
			parms.add(new BasicNameValuePair("nextpage", "login"));
			parms.add(new BasicNameValuePair("submit", "login"));
			loginAttempt(LOGIN_URL, parms,  localContext);
			request = new HttpGet(MANAGE_URL);
			request.addHeader("User-Agent", "Apache HTTPClient");
			response = client.execute(request, localContext);
			entity1 = response.getEntity();
			content = EntityUtils.toString(entity1);
			List<String> charIds = getCharacterIds(content);
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
					System.out.println(StringUtils.repeat("=", 11 + name.length()));
					System.out.println(String.format("Processing %s", name));
					System.out.println(StringUtils.repeat("=", 11 + name.length()));
					Map<String, String> attribs = parseCharacterAttribs(charContent);
					Map<Integer, Boost> enhancements = parseEnhancements(charContent);
					List<String> recipes = parseRecipes(charContent);
					characters.add(name);
					for (Map.Entry<Integer, Boost> entry : enhancements.entrySet()) {
						Boost b = entry.getValue();
						if ("-1".equalsIgnoreCase(b.getPowerId())) {
							System.out.println(String.format("Enhancement: %s %s",
									b.getBoostName(),
									b.getLevel()));
						}
					}
				}
			}
			
		}
		
		Collections.sort(characters);
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

	private static List<RebirthAccount> getAccounts(Properties p) {
		List<RebirthAccount> accounts = new ArrayList<>();
		if (p.getProperty("account.1.username") != null && p.getProperty("account.1.password") != null) {
			accounts.add(new RebirthAccount(p.getProperty("account.1.username"), p.getProperty("account.1.password")));
		}
		if (p.getProperty("account.2.username") != null && p.getProperty("account.2.password") != null) {
			accounts.add(new RebirthAccount(p.getProperty("account.2.username"), p.getProperty("account.2.password")));
		}
		if (p.getProperty("account.3.username") != null && p.getProperty("account.3.password") != null) {
			accounts.add(new RebirthAccount(p.getProperty("account.3.username"), p.getProperty("account.3.password")));
		}
		return accounts;
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
	private static Map<String, String> parseCharacterAttribs(String content) {
		Map<String, String> attribs = new HashMap<String, String>();
		Scanner scanner = new Scanner(content);
		while (scanner.hasNextLine()) {
		  String line = scanner.nextLine();
		  if (line.startsWith("Class ")) {
			  attribs.put("Archetype", line.split(" ")[1]);
		  } else if (line.startsWith("Origin ")) {
			  attribs.put("Origin", line.split(" ")[1]);
		  } else if (line.startsWith("Level ")) {
			  int lvl = Integer.valueOf(line.split(" ")[1]) + 1;
			  attribs.put("Level", String.valueOf(lvl));
		  } else if (line.startsWith("Ents2[0].originalPrimary ")) {
			  attribs.put("Primary", line.split(" ")[1]);
		  } else if (line.startsWith("Ents2[0].originalSecondary ")) {
			  attribs.put("Secondary", line.split(" ")[1]);
		  }
		}
		scanner.close();
		return attribs;
	}

	private static List<String> parseRecipes(String content) {
		List<String> recipes = new ArrayList<String>();
		final String regex = "InvRecipeInvention\\[\\d\\].c[\\d]*Type.\\\"([a-zA-Z0-9_]*)\\\"";
		final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
		final Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			String recipeName =  matcher.group(1);
			if (!"none".equalsIgnoreCase(recipeName)) {
				System.out.println(String.format("Recipe: %s", recipeName));
				recipes.add(recipeName);
			}
		}
		return recipes;
	}

	private static Map<Integer, Boost> parseEnhancements(String content) {
		Map<Integer, Boost> boosts = new HashMap<Integer, Boost>();
		final String regex = "Boosts\\[(\\d*)\\].([a-zA-Z]*)\\s(\\S*)";
		final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
		final Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			Integer num = Integer.valueOf(matcher.group(1));
			Boost b = boosts.get(num);
			if (b == null) {
				b = new Boost();
			}
			b.setProperty(matcher.group(2).replace("\"", ""), matcher.group(3).replace("\"", ""));
			boosts.put(num, b);
		}
		return boosts;
	}

	private static Properties getConfig() throws IOException {
		FileReader reader = new FileReader("properties.cfg");
		Properties p = new Properties();
		p.load(reader);
		if (p.getProperty("account.1.username") != null &&
				p.getProperty("account.1.password") != null &&
				p.getProperty("filename") != null) {
			return p;
		}
		throw new IOException("Invalid Config File: Missing Parameters");
	}
}

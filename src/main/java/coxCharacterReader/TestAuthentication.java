package coxCharacterReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
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

public class TestAuthentication {
	private static final String LOGIN_URL = "https://www.cityofheroesrebirth.com/public/login";
	private static final String MANAGE_URL = "https://www.cityofheroesrebirth.com/public/manage";
	private static final String CHAR_PAGE_URL = "https://www.cityofheroesrebirth.com/public/api/character/raw?q=";

	public static void main(String[] args) throws ClientProtocolException, IOException, URISyntaxException {
		if (args.length <2)
			throw new IllegalArgumentException("2 parameters expected, Username and password");
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
		parms.add(new BasicNameValuePair("username", args[0]));
		parms.add(new BasicNameValuePair("password", args[1]));
		parms.add(new BasicNameValuePair("nextpage", "login"));
		parms.add(new BasicNameValuePair("submit", "login"));
		loginAttempt(LOGIN_URL, parms,  localContext);
		request = new HttpGet(MANAGE_URL);
		request.addHeader("User-Agent", "Apache HTTPClient");
		response = client.execute(request, localContext);
		entity1 = response.getEntity();
		content = EntityUtils.toString(entity1);
		List<String> charIds = getCharacterIds(content);
		System.out.println("\"Character\",\"BackAlleyBrawlerGloves\",\"HamidonCostume\",\"LordRecluseMask\",\"StatesmanMask\"," +
				"\"AncientArtifact\",\"SpellScroll\"");
		for(String charid : charIds) {
			String apiUrl = CHAR_PAGE_URL + charid;
			client = HttpClientBuilder.create().build();
			request = new HttpGet(apiUrl);

			request.addHeader("User-Agent", "Apache HTTPClient");
			response = client.execute(request);

			HttpEntity entity = response.getEntity();
			String charContent = EntityUtils.toString(entity);
			System.out.println(parseChar(charContent));
		}
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
	private static String parseChar(String content) {
		Reader inputString = new StringReader(content);
		BufferedReader reader = new BufferedReader(inputString);
		String name = "";
		String StatesmanMask = "0";
		String LordRecluseMask = "0";
		String BackAlleyBrawlerGloves = "0";
		String HamidonCostume = "0";
		String SpellScroll = "0";
		String AncientArtifact = "0";
		
		try {
			String line = reader.readLine();
			while (line != null) {
				if (line.startsWith("Name ")) {
					name = line.substring(5);
				} else if (line.startsWith("InvSalvage0[0].S_StatesmanMask_H2006")) {
					StatesmanMask = line.substring(37);
				} else if (line.startsWith("InvSalvage0[0].S_LordRecluseMask_H2006")) {
					LordRecluseMask = line.substring(39);
				} else if (line.startsWith("InvSalvage0[0].S_BackAlleyBrawlerGloves_H2006")) {
					BackAlleyBrawlerGloves = line.substring(46);
				} else if (line.startsWith("InvSalvage0[0].S_HamidonCostume_H2006")) {
					HamidonCostume = line.substring(38);
				} else if (line.startsWith("InvSalvage0[0].S_SpellScroll")) {
					SpellScroll = line.substring(29);
				} else if (line.startsWith("InvSalvage0[0].S_AncientArtifact")) {
					AncientArtifact = line.substring(33);
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return String.format("%s,%s,%s,%s,%s,%s,%s", 
				name, 
				BackAlleyBrawlerGloves, 
				HamidonCostume, 
				LordRecluseMask, 
				StatesmanMask,
				AncientArtifact,
				SpellScroll);
	}
}

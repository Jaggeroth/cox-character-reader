package coxCharacterReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class TestReader {
	private static final String CHAR_PAGE_URL = "https://www.cityofheroesrebirth.com/public/api/character/raw?q=";
	private static final String HTML_SCOPE = "https://www.cityofheroesrebirth.com";
	private static final String[] CHAR_IDS = new String[] {"08nGByBakqt1vF4f%2FIWUUA%3D%3D",
			"oYhL8jGfTiiLJ4PYl3rEWg%3D%3D",
			"BYzK5AI%2B8UUygO4bER12GQ%3D%3D",
			"ewXYFSUBUEuEeM9N51rABw%3D%3D",
			"m3xk569OxXoDEDBueB%2B3rw%3D%3D",
			"brGxzOVXny%2Fg7%2FnbZnQtig%3D%3D",
			"1G8d3Fy22ly7dM19FCPUgw%3D%3D",
			"vx4Se40GHxWfbQtmgcQqzbBRNT3T7sJs4KVvk%2BNuU30%3D",
			"UfrnB8NmomKig06mARxwSg%3D%3D",
			"7TuqbPdjm0h8KH6nOf8olA%3D%3D",
			"psJ%2FZtoRZkVvKdxRCyxQFA%3D%3D",
			"2YLRd%2BkAQPUfJSYCr0c20g%3D%3D",
			"vT3N2NWXZlZKGDTgQCPx0w%3D%3D",
			"UmhZPd0LpJr6xw8ElI8cVQ%3D%3D",
			"apies7bQdMbj4VZDO8tsJg%3D%3D",
			"3N0FWfBUToeH2cihOipgsA%3D%3D",
			"uD3Bz0ggEYJLKA5tDTF45A%3D%3D",
			"mQ2Wzt57EOHCQ1H55Eex0w%3D%3D",
			"k%2BByHz9mqPpSp%2B%2BPiPbF0w%3D%3D",
			"MO%2F77Gq%2BSJFj%2BOPoM4J6tA%3D%3D",
			"mBwc%2BMl7QxTpiQ2JC9s6Og%3D%3D",
			"2z4%2FMbj2qdKnS%2BbfqJlIvtE5kLSFeQUkCvpkp3Pa%2BJk%3D",
			"MIgSPyNyAlWFmiE%2BQtuvfKOuIa7cHwOiuJl0xhcUcmE%3D",
			"Q31HEECDf1pyLh2EZwDcHQ%3D%3D",
			"63D4ozHBLJr5yOd1x%2BxDcA%3D%3D",
			"imOITP62qDn2rNG8Z7iYzg%3D%3D",
			"uun%2Be9zHjb61ROCzUWEKUg%3D%3D",
			"iZB9ANlP5V1aXy%2FgJUCkJw%3D%3D",
			"dpJYf%2FM4lp7thQccPRGXSw%3D%3D",
			"DQtkIZdJvnJGrnEiPc6tPg%3D%3D",
			"B0haPz6iwOZnxvXGlGEDHg%3D%3D",
			"iAcoxLtMWZUb3EuzPdErXg%3D%3D",
			"%2FFyUF2sBHoYLfWUi4YilGw%3D%3D",
			"oKU4K%2FFr8HpnugIfuy1tFQ%3D%3D",
			"l20E4ql0qKD%2BCtKaL8jIsQ%3D%3D",
			"PHdecFHg1DVamjqtrmU98w%3D%3D",
			"vGDdAJqPKhpLz2b8ap3SmQ%3D%3D",
			"jIWUx%2FIbhsq3PCw3juyYuA%3D%3D"};

	public static void main(String[] args) throws IOException {

		HttpGet request = null;

		try {
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(
					new AuthScope(CHAR_PAGE_URL, 80),
					new UsernamePasswordCredentials("Jagged", "Defend3r76"));
			CloseableHttpClient httpclient = HttpClients.custom()
					.setDefaultCredentialsProvider(credsProvider)
					.build();
			System.out.println("\"Character\",\"BackAlleyBrawlerGloves\",\"HamidonCostume\",\"LordRecluseMask\",\"StatesmanMask\"," +
					"\"AncientArtifact\",\"SpellScroll\"");
			for (String charid : CHAR_IDS) {
				HttpClient client = HttpClientBuilder.create().build();
				request = new HttpGet(CHAR_PAGE_URL + charid);

				request.addHeader("User-Agent", "Apache HTTPClient");
				HttpResponse response = client.execute(request);

				HttpEntity entity = response.getEntity();
				String content = EntityUtils.toString(entity);
				System.out.println(parseChar(content));
			}

		} finally {

			if (request != null) {

				request.releaseConnection();
			}
		}

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
			// TODO Auto-generated catch block
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

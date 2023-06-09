package coxCharacterReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class BuildInfo {
	private static final String CHAR_PAGE_URL = "https://www.cityofheroesrebirth.com/public/api/character/raw?q=<character id here>";

	public static void main(String[] args) throws IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(CHAR_PAGE_URL);
		request.addHeader("User-Agent", "Apache HTTPClient");
		HttpResponse response = client.execute(request);
		HttpEntity entity = response.getEntity();
		String charContent = EntityUtils.toString(entity);
		Map<Integer, Power> powers = parsePowers(charContent);
		Map<Integer, Boost> boosts = parseEnhancements(charContent);
		System.out.println("BUILD DATA\n==========");
		System.out.println("INHERENT POWERS\n===============");
		findInherentPowers(powers, boosts);
		System.out.println("LEVEL 1\n=======");
		findPower(1,powers, boosts);
		System.out.println("LEVEL 2\n=======");
		findPower(2,powers, boosts);
		System.out.println("LEVEL 4\n=======");
		findPower(4,powers, boosts);
		System.out.println("LEVEL 6\n=======");
		findPower(6,powers, boosts);
		System.out.println("LEVEL 8\n=======");
		findPower(8,powers, boosts);
		System.out.println("LEVEL 10\n========");
		findPower(10,powers, boosts);
		System.out.println("LEVEL 12\n========");
		findPower(12,powers, boosts);
		System.out.println("LEVEL 14\n========");
		findPower(14,powers, boosts);
		System.out.println("LEVEL 16\n========");
		findPower(16,powers, boosts);
		System.out.println("LEVEL 18\n========");
		findPower(18,powers, boosts);
		System.out.println("LEVEL 20\n========");
		findPower(20,powers, boosts);
		System.out.println("LEVEL 22\n========");
		findPower(22,powers, boosts);
		System.out.println("LEVEL 24\n========");
		findPower(24,powers, boosts);
		System.out.println("LEVEL 26\n========");
		findPower(26,powers, boosts);
		System.out.println("LEVEL 28\n========");
		findPower(28,powers, boosts);
		System.out.println("LEVEL 30\n========");
		findPower(30,powers, boosts);
		System.out.println("LEVEL 32\n========");
		findPower(32,powers, boosts);
		System.out.println("LEVEL 35\n========");
		findPower(35,powers, boosts);
		System.out.println("LEVEL 38\n========");
		findPower(38,powers, boosts);
		System.out.println("LEVEL 41\n========");
		findPower(41,powers, boosts);
		System.out.println("LEVEL 44\n========");
		findPower(44,powers, boosts);
		System.out.println("LEVEL 47\n========");
		findPower(47,powers, boosts);
		System.out.println("LEVEL 49\n========");
		findPower(49,powers, boosts);
	}
	private static Map<Integer, Power> parsePowers(String content) {
		Map<Integer, Power> powers = new HashMap<Integer, Power>();
		final String regex = "Powers\\[(\\d*)\\].([a-zA-Z]*)\\s(\\S*)";
		final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
		final Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			Integer num = Integer.valueOf(matcher.group(1));
			Power p = powers.get(num);
			if (p == null) {
				p = new Power();
			}
			p.setProperty(matcher.group(2), matcher.group(3));
			powers.put(num, p);
		}
		return powers;
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
			b.setProperty(matcher.group(2), matcher.group(3));
			boosts.put(num, b);
		}
		return boosts;
	}
	private static String findPower(int forLevel, 
			final Map<Integer, Power> powers, 
			final Map<Integer, Boost> boosts) {
		for (Map.Entry<Integer, Power> entry : powers.entrySet()) {
			Power p = entry.getValue();
			if (Integer.valueOf(p.getPowerLevelBought()).compareTo(forLevel)==0) {
				if (p.isBuildOption()) {
					System.out.println(outputPower(p));
					findBoostForPower(p, boosts);
				}
			}
		}
		return "";
	}
	private static String findBoostForPower(Power p, 
			final Map<Integer, Boost> boosts) {
		for (Map.Entry<Integer, Boost> entry : boosts.entrySet()) {
			Boost b = entry.getValue();
			if (p.getPowerId().equals(b.getPowerId())) {
				System.out.println(outputBoost(b, p));
			}
		}
		return "";
	}
	private static String outputPower(Power p) {
		return String.format("Powerset: %s Power: %s", p.getPowerSetName(), p.getPowerName());
	}
	private static String outputBoost(Boost b) {
		return String.format("%s %s %s",
				b.getIdx() != null ? String.format("Slot %s:", b.getIdx()) : "Inherent slot:",
						b.getBoostName(),
						b.getLevel() != null ? String.format("Level %s", b.getLevel()) : "");

	}
	private static String outputBoost(Boost b, Power p) {
		if (p == null)
			return outputBoost(b);
		return String.format("%s %s %s",
				b.getIdx() != null ? String.format("Slot %s for %s:", b.getIdx(), p.getPowerName()) : String.format("Inherent slot for %s:",p.getPowerName()),
						b.getBoostName(),
						b.getLevel() != null ? String.format("Level %s", b.getLevel()) : "");

	}
	private static void findInherentPowers(final Map<Integer, Power> powers, final Map<Integer, Boost> boosts) {
		for (Map.Entry<Integer, Power> entry : powers.entrySet()) {
			Power p = entry.getValue();
			if (p.isInherent()) {
				System.out.println(outputPower(p));
				findBoostForPower(p, boosts);
			}
		}
	}
}

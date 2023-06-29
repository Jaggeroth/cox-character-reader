package coxCharacterReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
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

    public static final int[] BUILD_LEVELS = new int[]{1, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 35, 38, 41, 44, 47, 49};

    public static void main(String[] args) throws IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(CHAR_PAGE_URL);
		request.addHeader("User-Agent", "Apache HTTPClient");
		HttpResponse response = client.execute(request);
		HttpEntity entity = response.getEntity();
		String charContent = EntityUtils.toString(entity);
		Map<Integer, Power> powers = new HashMap<Integer, Power>();
		powers = parsePowers(charContent);
		Map<Integer, Boost> boosts  = new HashMap<Integer, Boost>();
		boosts= parseEnhancements(charContent);
		System.out.println("BUILD DATA\n==========");
		System.out.println(String.format("NAME: %s", parseCharName(charContent)));
		System.out.println(String.format("ALIGNMENT: %s", parseAlignment(charContent)));
		Map<String, String> attribs = parseCharacterAttribs(charContent);
		System.out.println(String.format("ORIGIN: %s", attribs.get("Origin")));
		System.out.println(String.format("ARCHETYPE: %s", attribs.get("Archetype")));
		System.out.println(String.format("PRIMARY: %s", attribs.get("Primary")));
		System.out.println(String.format("SECONDARY: %s", attribs.get("Secondary")));
		System.out.println(String.format("LEVEL: %s", attribs.get("Level")));
		Integer characterLevel = Integer.parseInt(attribs.get("Level"));
		System.out.println("\nINHERENT POWERS\n===============");
		findInherentPowers(powers, boosts);
		for (int buildLevel : BUILD_LEVELS) {
			if (buildLevel <= characterLevel) {
				System.out.println(String.format("\nLEVEL %s", buildLevel));
				System.out.println(buildLevel<10?"=======":"========");
				findPower(buildLevel,powers, boosts);
			}
		}
		/***
		 * Check for and display Incarnate powers
		 **/
		if (characterLevel >= 50) {
			Power alphaSlot = null;
			Power judgementSlot = null;
			Power interfaceSlot = null;
			Power loreSlot = null;
			Power destinySlot = null;
			Power hybridSlot = null;
			boolean hasIncarnate = false;
			for (Map.Entry<Integer, Power> entry : powers.entrySet()) {
				Power p = entry.getValue();
				if (p.isIncarnatePower()) {
					if (p.getDisabled()==null) {
						hasIncarnate = true;
						if ("\"alpha\"".equalsIgnoreCase(p.getPowerSetName())) {
							alphaSlot = p;
						} else if ("\"judgement\"".equalsIgnoreCase(p.getPowerSetName())) {
							judgementSlot = p;
						} else if ("\"interface\"".equalsIgnoreCase(p.getPowerSetName())) {
							interfaceSlot = p;
						} else if ("\"lore\"".equalsIgnoreCase(p.getPowerSetName())) {
							loreSlot = p;
						} else if ("\"destiny\"".equalsIgnoreCase(p.getPowerSetName())) {
							destinySlot = p;
						} else if ("\"hybrid\"".equalsIgnoreCase(p.getPowerSetName())) {
							hybridSlot = p;
						}
					}
				}
			}
			if (hasIncarnate) {
				System.out.println("\nINCARNATE POWERS\n================");
				if (alphaSlot != null) {
					System.out.println(String.format("ALPHA SLOT: %s TIER %s", alphaSlot.getPowerName(), alphaSlot.getIncarnateTier()));
				}
				if (judgementSlot != null) {
					System.out.println(String.format("JUDGEMENT SLOT: %s TIER %s", judgementSlot.getPowerName(), judgementSlot.getIncarnateTier()));
				}
				if (interfaceSlot != null) {
					System.out.println(String.format("INTERFACE SLOT: %s TIER %s", interfaceSlot.getPowerName(), interfaceSlot.getIncarnateTier()));
				}
				if (loreSlot != null) {
					System.out.println(String.format("LORE SLOT: %s TIER %s", loreSlot.getPowerName(), loreSlot.getIncarnateTier()));
				}
				if (destinySlot != null) {
					System.out.println(String.format("DESTINY SLOT: %s TIER %s", destinySlot.getPowerName(), destinySlot.getIncarnateTier()));
				}
				if (hybridSlot != null) {
					System.out.println(String.format("HYBRID SLOT: %s TIER %s", hybridSlot.getPowerName(), hybridSlot.getIncarnateTier()));
				}
			}
		}
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
		return String.format("%s %s %s",String.format("Slot %s:", b.extractSlotNumber()),
				b.getBoostName(),
				b.getLevel() != null ? String.format("Level %s", b.getLevel()) : "");

	}
	private static String outputBoost(Boost b, Power p) {
		if (p == null)
			return outputBoost(b);
		return String.format("%s %s %s",
				String.format("Slot %s for %s:", b.extractSlotNumber(), p.getPowerName()),
				b.getBoostName(),
				b.getLevel() != null ? String.format("Level %s", b.getLevel()) : "");

	}
	/***
	 * Only return powers that are "inherent" and have been slotted.
	 * @param powers - map of powers
	 * @param boosts - map of enhancement slotting
	 ***/
	private static void findInherentPowers(final Map<Integer, Power> powers, final Map<Integer, Boost> boosts) {
		for (Map.Entry<Integer, Power> entry : powers.entrySet()) {
			Power p = entry.getValue();
			if ("\"inherent\"".equalsIgnoreCase(p.getCategoryName())) {
				if (isSlotted(p.getPowerId(), boosts)) {
					System.out.println(outputPower(p));
					findBoostForPower(p, boosts);
				}
			}
		}
	}
	private static boolean isSlotted(String powerId, final Map<Integer, Boost> boosts) {
		for (Map.Entry<Integer, Boost> entry : boosts.entrySet()) {
			Boost b = entry.getValue();
			if (powerId.equals(b.getPowerId())) {
				return true;
			}
		}
		return false;
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
	private static String parseAlignment(String content) {
		String playerType = getFirstHit("PlayerType (.+)", content);
		String playerSubType = getFirstHit("PlayerSubType (.+)", content);
		String praetorianProgress = getFirstHit("PraetorianProgress (.+)", content);
		if (playerType == null && playerSubType == null && (praetorianProgress == null || "3".equalsIgnoreCase(praetorianProgress))) {
			return "Hero";
		} else if ("1".equalsIgnoreCase(playerType) && (playerSubType == null && (praetorianProgress == null || "3".equalsIgnoreCase(praetorianProgress)))) {
			return "Villain";
		} else if (playerType == null && "2".equalsIgnoreCase(playerSubType)) {
			return "Vigilante";
		} else if ("1".equalsIgnoreCase(playerType) && "2".equalsIgnoreCase(playerSubType)) {
			return "Rogue";
		} else if (playerType == null && "2".equalsIgnoreCase(praetorianProgress)) {
			return "Resistance";
		} else if ("1".equalsIgnoreCase(playerType) && "2".equalsIgnoreCase(praetorianProgress)) {
			return "Loyalist";
		} else if ("6".equalsIgnoreCase(praetorianProgress)) {
			return "Praetorian";
		}
		return null;
	}
	private static String getFirstHit(final String regex, final String content) {
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
        	return matcher.group(1);
        }
		return null;
	}
}

package coxCharacterReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class HtmlBuildInfo {
	private static final String IMG_TAG = "<img src=\"%s\" title=\"%s\" width=\"32\" height=\"32\">";
	private static final String TB_POWER = "<table class=\"powertable\">";
	private static final String TB_POWER_ROW = "<tr><td class=\"powercol\" rowspan=\"2\">%s</td><td colspan=\"7\">%s</td></tr>";
	private static final String TB_ENHANCEMENT_CELL = "<td style=\"width: 36px;\">%s</td>";
	private static final String UNKNOWN_ICON = "images\\unknown.png";
	//private static final String CHAR_TITLE = "Level %s %s / %s %s %s %s : %s";
	// Power Jenny : lvl 50 Magic Invulnerability / Super Strength Tanker Hero
	private static final String CHAR_TITLE = "%s : lvl %s %s %s / %s %s %s";
    //private static final String CHAR_PAGE_URL = "https://www.cityofheroesrebirth.com/public/api/character/raw?q=<character id here>";
	// Enigma Tick
	private static final String CHAR_PAGE_URL = "https://www.cityofheroesrebirth.com/public/api/character/raw?q=7TuqbPdjm0h8KH6nOf8olA%3D%3D";
    
    public static final int[] BUILD_LEVELS = new int[]{1, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 35, 38, 41, 44, 47, 49};

    public static void main(String[] args) throws IOException {
    	HtmlBuildInfo hbi = new HtmlBuildInfo();
    	hbi.extractExecute("C:\\Data\\Docs\\hero-id\\characters\\enigma_tick.html", CHAR_PAGE_URL);
	}

    public CharacterProfile execute(String filename, String charContent) throws IOException {
    	Properties iconsData = getIcons();
		Map<String, String> attribs = parseCharacterAttribs(charContent);
		String alignment = parseAlignment(charContent);
		String origin = attribs.get("Origin");
		String architype = attribs.get("Archetype");
		String primary = attribs.get("Primary");
		String secondary = attribs.get("Secondary");
		String name = parseCharName(charContent);
		Integer characterLevel = 0;
		try {
		characterLevel = Integer.parseInt(attribs.get("Level"));
		} catch (NumberFormatException e) {
			// do nothing happens when char has not been played
		}
		// Power Jenny : lvl 50 Magic Invulnerability / Super Strength Tanker Hero
		String char_page_title = String.format(CHAR_TITLE,
				name,
				characterLevel,
				origin,
				primary,
				secondary,
				architype,
				alignment);
		
		Map<Integer, Power> powers = new HashMap<Integer, Power>();
		powers = parsePowers(charContent, primary, secondary);
		Map<Integer, Boost> boosts  = new HashMap<Integer, Boost>();
		boosts= parseEnhancements(charContent);

		File file = new File(filename);
		BufferedWriter writer = new BufferedWriter(new FileWriter(file,true));
		
		writer.write(getHeaderHtml(char_page_title));
		writer.write(String.format("<h1>%s %s %s</h1>",
				getAlignmentIcon(iconsData, alignment),
				name,
				getOriginIcon(iconsData, origin)));
		writer.write(String.format("<h2>%s LEVEL %s %s / %s %s</h2>",
				getArchitypeIcon(iconsData, architype),
				characterLevel,
				primary,
				secondary,
				architype));
		writer.write("<div class=\"layout\">\n<h3>INHERENT POWERS</h3>");
		writer.write(findInherentPowers(powers, boosts, iconsData));
		writer.write("<h3>POWERS</h3>");
		for (int buildLevel : BUILD_LEVELS) {
			if (buildLevel <= characterLevel) {
				//System.out.println(String.format("<h3>LEVEL %s</h3>", buildLevel));
				writer.write(findPower(buildLevel,powers, boosts, iconsData));
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
			Power genesisSlot = null;
			boolean hasIncarnate = false;
			for (Map.Entry<Integer, Power> entry : powers.entrySet()) {
				Power p = entry.getValue();
				if (p.isIncarnatePower()) {
					if (p.getDisabled()==null) {
						hasIncarnate = true;
						if ("alpha".equalsIgnoreCase(p.getPowerSetName())) {
							alphaSlot = p;
						} else if ("judgement".equalsIgnoreCase(p.getPowerSetName())) {
							judgementSlot = p;
						} else if ("interface".equalsIgnoreCase(p.getPowerSetName())) {
							interfaceSlot = p;
						} else if ("lore".equalsIgnoreCase(p.getPowerSetName())) {
							loreSlot = p;
						} else if ("destiny".equalsIgnoreCase(p.getPowerSetName())) {
							destinySlot = p;
						} else if ("hybrid".equalsIgnoreCase(p.getPowerSetName())) {
							hybridSlot = p;
						} else if ("genesis".equalsIgnoreCase(p.getPowerSetName())) {
							genesisSlot = p;
						}
					}
				}
			}
			if (hasIncarnate) {
				writer.write("<h3>INCARNATE POWERS</h3>");
				if (alphaSlot != null) {
					writer.write(String.format("ALPHA SLOT: %s TIER %s<br/>", alphaSlot.getPowerName(), alphaSlot.getIncarnateTier()));
				}
				if (judgementSlot != null) {
					writer.write(String.format("JUDGEMENT SLOT: %s TIER %s<br/>", judgementSlot.getPowerName(), judgementSlot.getIncarnateTier()));
				}
				if (interfaceSlot != null) {
					writer.write(String.format("INTERFACE SLOT: %s TIER %s<br/>", interfaceSlot.getPowerName(), interfaceSlot.getIncarnateTier()));
				}
				if (loreSlot != null) {
					writer.write(String.format("LORE SLOT: %s TIER %s<br/>", loreSlot.getPowerName(), loreSlot.getIncarnateTier()));
				}
				if (destinySlot != null) {
					writer.write(String.format("DESTINY SLOT: %s TIER %s<br/>", destinySlot.getPowerName(), destinySlot.getIncarnateTier()));
				}
				if (hybridSlot != null) {
					writer.write(String.format("HYBRID SLOT: %s TIER %s<br/>", hybridSlot.getPowerName(), hybridSlot.getIncarnateTier()));
				}
				if (genesisSlot != null) {
					writer.write(String.format("GENESIS SLOT: %s TIER %s<br/>", genesisSlot.getPowerName(), genesisSlot.getIncarnateTier()));
				}
			}
		}
		writer.write(getFooterHtml());
		writer.close();
		return new CharacterProfile(name, getArchitypeIcon(iconsData, architype),
				getAlignmentIcon(iconsData, alignment),
				characterLevel,
				getOriginIcon(iconsData, origin),
				primary,
				secondary,
				filename,
				char_page_title);
    }
 
    public CharacterProfile extractExecute(String filename, String characterUrl) throws IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(characterUrl);
		request.addHeader("User-Agent", "Apache HTTPClient");
		HttpResponse response = client.execute(request);
		HttpEntity entity = response.getEntity();
		String charContent = EntityUtils.toString(entity);
		return execute(filename, charContent);
    }

    private static Map<Integer, Power> parsePowers(String content, String primary, String secondary) {
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
			p.setProperty(matcher.group(2).replace("\"", ""), matcher.group(3).replace("\"", ""));
			// sometimes we need to set power level bought as it is missing on primary & secondary powers 
			if (primary.equalsIgnoreCase(p.getPowerSetName()) || secondary.equalsIgnoreCase(p.getPowerSetName())) {
				if (p.getPowerLevelBought() == null) {
					p.setPowerLevelBought("0");
				}
			}
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
			b.setProperty(matcher.group(2).replace("\"", ""), matcher.group(3).replace("\"", ""));
			boosts.put(num, b);
		}
		return boosts;
	}
	private static String findPower(int forLevel, 
			final Map<Integer, Power> powers, 
			final Map<Integer, Boost> boosts,
			final Properties iconsData) {
		String result = "";
		for (Map.Entry<Integer, Power> entry : powers.entrySet()) {
			Power p = entry.getValue();
			if (Integer.valueOf(p.extractPowerLevelBought()).compareTo(forLevel)==0) {
				if (p.isBuildOption()) {
					result = result + TB_POWER;
					result = result + String.format(TB_POWER_ROW, getPowerIcon(iconsData, p), 
							String.format("(%s) ", String.valueOf(forLevel))+outputPower(p));
					result = result + "<tr>\n";
					result = result + findBoostForPower(p, boosts, iconsData);
					result = result + "</tr>\n<table>\n";
				}
			}
		}
		return result;
	}
	private static String findBoostForPower(Power p, 
			final Map<Integer, Boost> boosts,
			final Properties iconsData) {
		String result = "";
		int c=0;
		for (Map.Entry<Integer, Boost> entry : boosts.entrySet()) {
			Boost b = entry.getValue();
			if (p.getPowerId().equals(b.getPowerId())) {
				result = result + (String.format(TB_ENHANCEMENT_CELL, getBoostIcon(iconsData, b))); //, outputBoost(b, p)
				c++;
			}
		}
		while (c<7) {
			result = result + "<td style=\"width: 32px, height: 32px;\">" + 
		        "<img src=\"images\\blank.png\" title=\"Kick\" width=\"32\" height=\"32\">" + 
			    "</td>";
			c++;
		}
		return result;
	}
	private static String outputPower(Power p) {
		return String.format("%s %s", p.getPowerSetName(), p.getPowerName());
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
	private static String findInherentPowers(final Map<Integer, Power> powers, final Map<Integer, Boost> boosts, final Properties iconsData) {
		String result = "";
		for (Map.Entry<Integer, Power> entry : powers.entrySet()) {
			Power p = entry.getValue();
			if ("inherent".equalsIgnoreCase(p.getCategoryName())) {
				if (isSlotted(p.getPowerId(), boosts)) {
					result = result + TB_POWER + "\n";
					result = result + String.format(TB_POWER_ROW,  getPowerIcon(iconsData, p), outputPower(p));
					result = result + "<tr>\n";
					result = result + findBoostForPower(p, boosts, iconsData);
					result = result + "</tr>\n</table>\n";
				}
			}
		}
		return result;
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
			  attribs.put("Archetype", line.split(" ")[1].replace("\"", ""));
		  } else if (line.startsWith("Origin ")) {
			  attribs.put("Origin", line.split(" ")[1].replace("\"", ""));
		  } else if (line.startsWith("Level ")) {
			  int lvl = Integer.valueOf(line.split(" ")[1]) + 1;
			  attribs.put("Level", String.valueOf(lvl));
		  } else if (line.startsWith("Ents2[0].originalPrimary ")) {
			  attribs.put("Primary", line.split(" ")[1].replace("\"", ""));
		  } else if (line.startsWith("Ents2[0].originalSecondary ")) {
			  attribs.put("Secondary", line.split(" ")[1].replace("\"", ""));
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
	private static String getAlignmentIcon(Properties i, final String a) {
		String key = String.format("alignment.%s", a.toLowerCase());
		String src = i.getProperty(key);
		return String.format(IMG_TAG, src, a);
	}
	private static String getArchitypeIcon(Properties i, final String a) {
		String key = String.format("archetype.%s", a.toLowerCase());
		String src = i.getProperty(key);
		return String.format(IMG_TAG, src, a);
	}
	private static String getOriginIcon(Properties i, final String origin) {
		String key = String.format("origin.%s", origin.toLowerCase());
		String src = i.getProperty(key);
		return String.format(IMG_TAG, src, origin);
	}
	private static String getPowerIcon(Properties i, final Power p) {
		String key = String.format("power.%s.%s", p.getPowerSetName().toLowerCase(), p.getPowerName().toLowerCase());
		String src = UNKNOWN_ICON;
		if (i.getProperty(key) != null) {
			src = i.getProperty(key);
		}
		return String.format(IMG_TAG, src, p.getPowerName());
	}
	private static String getBoostIcon(Properties i, final Boost b) {
		String key = getIconKey(b.getBoostName());
		String src = UNKNOWN_ICON;
		if (i.getProperty(key) != null) {
			src = i.getProperty(key);
		}
		String hoverText = b.getBoostName() + (b.getLevel() != null ? " LVL "+b.getLevel() : "");
		return String.format(IMG_TAG, src, hoverText);
	}
	private static String getIconKey(final String boostName) {
		String key = boostName.toLowerCase();
		if (key.length() > 3) {
			String end = key.substring(key.length() - 2);
			if (end.matches("_[a-f]")) {
				key = key.substring(0, key.length() - 2);
			}
		}
		return String.format("enhancement.%s", key);
	}
	private static String getFirstHit(final String regex, final String content) {
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
        	return matcher.group(1);
        }
		return null;
	}
	private static Properties getIcons() throws IOException {
		FileReader reader = new FileReader("icons.cfg");
		Properties p = new Properties();
		p.load(reader);
		if (p.getProperty("alignment.hero") != null) {
			return p;
		}
		throw new IOException("Invalid Config File: Missing Parameters");
	}
	private static String getHeaderHtml(String title) {
		return "<!DOCTYPE html>\n"
				+ "<html>\n"
				+ "<head>\n"
				+ "<link rel=\"stylesheet\" href=\"css\\build.css\" type=\"text/css\" />\n"
				+ "<title>" + title + "</title>\n"
				+ "</head>"
				+ "<body>";	
	}
	private static String getFooterHtml() {
		return "</div>\n"
				+ "</body>\n"
				+ "</html>\n"
				+ "";
	}
}

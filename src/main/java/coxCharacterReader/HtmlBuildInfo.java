package coxCharacterReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
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
	private static final String DOUBLE_IMG = "<div class=\"img-container\"><img class=\"bottom\" src=\"%s\" width=\"32\" height=\"32\"><img class=\"top\" src=\"%s\" title=\"%s\" width=\"32\" height=\"32\"></div>";
	private static final String CHAR_TITLE = "%s : lvl %s %s %s / %s %s %s";
	private static final String INCARNATE_TABLE = "<div class=\"incarnate\">"
			+ "<table style=\"width: 400px;\">"
			+ "<tr><td>%s</td><td style=\"width: 36px;\">%s</td></tr>"
			+ "</table></div>\n<p/>\n";
	private static final String INCARNATE_TEXT = "<b>%s:</b> %s <br/><b>TIER %s</b><br/>";
    //private static final String CHAR_PAGE_URL = "https://www.cityofheroesrebirth.com/public/api/character/raw?q=<character id here>";
	// Enigma Tick
	//private static final String CHAR_PAGE_URL = "https://www.cityofheroesrebirth.com/public/api/character/raw?q=BJGfUO9DTCFxoEE7okniNQ%3D%3D";
	// Maiden America
	//private static final String CHAR_PAGE_URL = "https://www.cityofheroesrebirth.com/public/api/character/raw?q=BYzK5AI%2B8UUygO4bER12GQ%3D%3D";
	// Murder Muse
	private static final String CHAR_PAGE_URL = "https://www.cityofheroesrebirth.com/public/api/character/raw?q=mQ2Wzt57EOHCQ1H55Eex0w%3D%3D";
	// Strife Spirit
	//private static final String CHAR_PAGE_URL = "https://www.cityofheroesrebirth.com/public/api/character/raw?q=RCrFx1%2FEVvwwftixCr75Vg%3D%3D";

    private Properties iconData;
    private Properties substitutionData;
    private List<String> resources;

	public static void main(String[] args) throws IOException {
    	System.out.println("START");
    	HtmlBuildInfo hbi = new HtmlBuildInfo();
    	hbi.extractExecute("C:\\Data\\Docs\\hero-id\\test", "test_char", CHAR_PAGE_URL);
    	System.out.println("END");
	}

    public CharacterProfile execute(String targetDir, String targetFilename, String charContent) throws IOException {
    	String filename = String.format("%s\\%s.html", targetDir, targetFilename);
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
				capitalizer(origin),
				capitalizer(primary),
				capitalizer(secondary),
				capitalizer(architype),
				alignment);
		
		Map<Integer, Power> powers = new HashMap<Integer, Power>();
		powers = parsePowers(charContent, primary, secondary);
		Map<Integer, Boost> boosts  = new HashMap<Integer, Boost>();
		boosts= parseEnhancements(charContent);

		File file = new File(filename);
		file.getParentFile().mkdirs();
		BufferedWriter writer = new BufferedWriter(new FileWriter(file,true));
		
		writer.write(getHeaderHtml(char_page_title));
		writer.write(String.format("<h1>%s %s %s</h1>",
				getAlignmentIcon(alignment),
				name,
				getOriginIcon(origin)));
		writer.write(String.format("<h2>%s LEVEL %s %s / %s %s %s</h2>",
				getArchitypeIcon(architype),
				characterLevel,
				capitalizer(primary),
				capitalizer(secondary),
				capitalizer(architype),
				alignment));
		writer.write("<div class=\"layout\">\n<h3>INHERENT POWERS</h3>");
		writer.write(findInherentPowers(powers, boosts));
		writer.write("<h3>POWERS</h3>");
		for (int buildLevel = 1; buildLevel < 50; buildLevel++) {
			if (buildLevel <= characterLevel) {
				writer.write(findPower(buildLevel,powers, boosts));
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
				writer.write("<h3>INCARNATE POWERS</h3>\n");
				if (alphaSlot != null) {
					String iText = String.format(INCARNATE_TEXT,
							alphaSlot.getPowerSetName().toUpperCase(),
							capitalizer(alphaSlot.getPowerName()),
							alphaSlot.getIncarnateTier());
					String iIcon = getIncarnateIcon(alphaSlot);
					writer.write(String.format(INCARNATE_TABLE, iText, iIcon));
				}
				if (judgementSlot != null) {
					String iText = String.format(INCARNATE_TEXT,
							judgementSlot.getPowerSetName().toUpperCase(),
							capitalizer(judgementSlot.getPowerName()),
							judgementSlot.getIncarnateTier());
					String iIcon = getIncarnateIcon(judgementSlot);
					writer.write(String.format(INCARNATE_TABLE, iText, iIcon));
				}
				if (interfaceSlot != null) {
					String iText = String.format(INCARNATE_TEXT,
							interfaceSlot.getPowerSetName().toUpperCase(),
							capitalizer(interfaceSlot.getPowerName()),
							interfaceSlot.getIncarnateTier());
					String iIcon = getIncarnateIcon(interfaceSlot);
					writer.write(String.format(INCARNATE_TABLE, iText, iIcon));
				}
				if (loreSlot != null) {
					String iText = String.format(INCARNATE_TEXT,
							loreSlot.getPowerSetName().toUpperCase(),
							capitalizer(loreSlot.getPowerName()),
							loreSlot.getIncarnateTier());
					String iIcon = getIncarnateIcon(loreSlot);
					writer.write(String.format(INCARNATE_TABLE, iText, iIcon));
				}
				if (destinySlot != null) {
					String iText = String.format(INCARNATE_TEXT,
							destinySlot.getPowerSetName().toUpperCase(),
							capitalizer(destinySlot.getPowerName()),
							destinySlot.getIncarnateTier());
					String iIcon = getIncarnateIcon(destinySlot);
					writer.write(String.format(INCARNATE_TABLE, iText, iIcon));
				}
				if (hybridSlot != null) {
					String iText = String.format(INCARNATE_TEXT,
							hybridSlot.getPowerSetName().toUpperCase(),
							capitalizer(hybridSlot.getPowerName()),
							hybridSlot.getIncarnateTier());
					String iIcon = getIncarnateIcon(hybridSlot);
					writer.write(String.format(INCARNATE_TABLE, iText, iIcon));
				}
				if (genesisSlot != null) {
					String iText = String.format(INCARNATE_TEXT,
							genesisSlot.getPowerSetName().toUpperCase(),
							capitalizer(genesisSlot.getPowerName()),
							genesisSlot.getIncarnateTier());
					String iIcon = getIncarnateIcon(genesisSlot);
					writer.write(String.format(INCARNATE_TABLE, iText, iIcon));
				}
			}
		}
		writer.write(getFooterHtml());
		writer.close();
		deployResources(getResources(), targetDir);
		return new CharacterProfile(name, getArchitypeIcon(architype),
				getAlignmentIcon(alignment),
				characterLevel,
				getOriginIcon(origin),
				primary,
				secondary,
				filename,
				char_page_title);
    }
 
    public CharacterProfile extractExecute(String targetDir, String targetFilename, String characterUrl) throws IOException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(characterUrl);
		request.addHeader("User-Agent", "Apache HTTPClient");
		HttpResponse response = client.execute(request);
		HttpEntity entity = response.getEntity();
		String charContent = EntityUtils.toString(entity);
		return execute(targetDir, targetFilename, charContent);
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
	private String findPower(int forLevel, 
			final Map<Integer, Power> powers, 
			final Map<Integer, Boost> boosts) {
		String result = "";
		for (Map.Entry<Integer, Power> entry : powers.entrySet()) {
			Power p = entry.getValue();
			if (Integer.valueOf(p.extractPowerLevelBought()).compareTo(forLevel)==0) {
				if (p.isBuildOption()) {
					result = result + TB_POWER;
					result = result + String.format(TB_POWER_ROW, getPowerIcon(p), 
							String.format("(%s) ", String.valueOf(forLevel))+outputPower(p));
					result = result + "<tr>\n";
					result = result + findBoostForPower(p, boosts);
					result = result + "</tr>\n</table>\n";
				}
			}
		}
		return result;
	}
	private String findBoostForPower(Power p, 
			final Map<Integer, Boost> boosts) {
		String result = "";
		int c=0;
		for (Map.Entry<Integer, Boost> entry : boosts.entrySet()) {
			Boost b = entry.getValue();
			if (p.getPowerId().equals(b.getPowerId())) {
				result = result + (String.format(TB_ENHANCEMENT_CELL, getBoostIcon(b))); //, outputBoost(b, p)
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
	private String outputPower(Power p) {
		return String.format("%s - %s", capitalizer(p.getPowerSetName()), capitalizer(p));
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
	private String findInherentPowers(final Map<Integer, Power> powers, final Map<Integer, Boost> boosts) {
		String result = "";
		for (Map.Entry<Integer, Power> entry : powers.entrySet()) {
			Power p = entry.getValue();
			if ("inherent".equalsIgnoreCase(p.getCategoryName())) {
				if (isSlotted(p.getPowerId(), boosts)) {
					result = result + TB_POWER + "\n";
					result = result + String.format(TB_POWER_ROW,  getPowerIcon(p), outputPower(p));
					result = result + "<tr>\n";
					result = result + findBoostForPower(p, boosts);
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
	/*
	private String getIncarnateIcon(final Power incarnate) {
		String key = String.format("enhancement.%s", incarnate.getPowerName().toLowerCase());
		String src = getIconProperty(key);
		return String.format(IMG_TAG, src, incarnate.getPowerName());
	}*/
	private String getAlignmentIcon(final String a) {
		String key = String.format("alignment.%s", a.toLowerCase());
		String src = getIconProperty(key);
		return String.format(IMG_TAG, src, a);
	}
	private String getArchitypeIcon(final String a) {
		String key = String.format("archetype.%s", a.toLowerCase());
		String src = getIconProperty(key);
		return String.format(IMG_TAG, src, a);
	}
	private String getOriginIcon(final String origin) {
		String key = String.format("origin.%s", origin.toLowerCase());
		String src = getIconProperty(key);
		return String.format(IMG_TAG, src, origin);
	}
	private String getPowerIcon(final Power p) {
		String src = getIconProperty(p);
		return String.format(IMG_TAG, src, capitalizer(p));
	}
	private String getIncarnateIcon(final Power incarnate) {
		String key = String.format("power.%s.%s.%s",
				incarnate.getPowerSetName().toLowerCase(),
				incarnate.getPowerName().split("_")[0].toLowerCase(),
				incarnate.getIncarnateTier());
		String src = getIconProperty(key);
		return String.format(IMG_TAG, src, capitalizer(incarnate));
	}
	private String getBoostIcon(final Boost b) {
		String key = getIconKey(b.getBoostName());
		if (getIconData().getProperty(key) != null) {
			String src =  getIconProperty(key);
			String hoverText = capitalizer(b.getBoostName()) + (b.getLevel() != null ? " LVL "+b.getLevel() : "");
			return String.format(IMG_TAG, src, hoverText);
		} else {
			return getCompositeIcon(b);
		}
	}
	private String getCompositeIcon(final Boost b) {
		List<String> valid = Arrays.asList("magic","natural","technology","science","mutation");
		String hoverText = b.getBoostName() + (b.getLevel() != null ? " LVL "+b.getLevel() : "");
		String boostName = b.getBoostName().toLowerCase();
		String [] element = boostName.split("_");
		if (valid.contains(element[0])) {
			if (valid.contains(element[1])) {
				String basekey = String.format("enhancement.generic_%s", String.join("_", Arrays.copyOfRange(element, 2, element.length)));
				String ringkey = String.format("enhancement.ring_%s_%s", element[0], element[1]);
				return String.format(DOUBLE_IMG, getIconProperty(basekey), getIconProperty(ringkey), hoverText);
			} else {
				String basekey = String.format("enhancement.generic_%s", String.join("_", Arrays.copyOfRange(element, 1, element.length)));
				String ringkey = String.format("enhancement.ring_%s", element[0]);
				return String.format(DOUBLE_IMG, getIconProperty(basekey), getIconProperty(ringkey), hoverText);
			}
		}
		System.out.println(String.format("ENHANCEMENT NOT FOUND: %s", b.getBoostName()));
		resourceAdd(UNKNOWN_ICON);
		return String.format(IMG_TAG, UNKNOWN_ICON, hoverText);
		
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
	/**
	 * This version of capitalizer takes a power and returns a proper form of the powername only.
	 * @param Power pow
	 * @return String of the proper power name.
	 */
	private String capitalizer(final Power pow) {
		if (getSubstitutionData() != null) {
			String key = String.format("%s.%s", pow.getPowerSetName().toLowerCase(), pow.getPowerName().toLowerCase());
			String result = getSubstitutionData().getProperty(key);
			if (result != null) {
				return result;
			}
		}
		return capitalizer(pow.getPowerName());
	}
	private String capitalizer(final String attrib) {
		if (getSubstitutionData() != null) {
			String result = getSubstitutionData().getProperty(attrib.toLowerCase());
			if (result != null) {
				return result;
			}
		}
		// some powers seem to have double underscores in their name
		String word = attrib.replace("__","_").replace("_", " ");
		if (word.startsWith("class ")) {
			word = word.substring(6);
		}
        String[] words = word.split(" ");
        StringBuilder sb = new StringBuilder();
        if (words[0].length() > 0) {
            sb.append(Character.toUpperCase(words[0].charAt(0)) + words[0].subSequence(1, words[0].length()).toString().toLowerCase());
            for (int i = 1; i < words.length; i++) {
                sb.append(" ");
                sb.append(Character.toUpperCase(words[i].charAt(0)) + words[i].subSequence(1, words[i].length()).toString().toLowerCase());
            }
        }
        return  sb.toString();
    }
	private Properties getIconData() {
		if (iconData == null) {
			try {
				InputStream inputStream = getClass().getResourceAsStream("/icons.cfg");
				Properties p = new Properties();
				p.load(inputStream);
				iconData = p;
			} catch (IOException e) {
				return null;
			}
		}
		return iconData;
	}
	
	/**
	 * Used in all cases to retrieve icon path to ensure file is copied to target directory
	 * @param attrib
	 * @return icon path as string
	 */
	private String getIconProperty(String attrib) {
		String result = getIconData().getProperty(attrib);
		if (result == null ) {
			result = UNKNOWN_ICON;
			System.out.println(String.format("ICON NOT FOUND: %s", attrib));
		}
		resourceAdd(result);
		return result;
	}
	private String getIconProperty(Power p) {
		String key = String.format("power.%s.%s", p.getPowerSetName().toLowerCase(), p.getPowerName().toLowerCase());
		String result = getIconData().getProperty(key);
		if (result == null ) {
			result = UNKNOWN_ICON;
			System.out.println(String.format("POWER NOT FOUND: %s - %s", p.getPowerSetName(), p.getPowerName()));
		}
		resourceAdd(result);
		return result;
	}
	private Properties getSubstitutionData() {
		if (substitutionData == null) {
			try {
				InputStream inputStream = getClass().getResourceAsStream("/textSubstitution.cfg");
				Properties p = new Properties();
				p.load(inputStream);
				substitutionData = p;
			} catch (IOException e) {
				return null;
			}
		}
		return substitutionData;
	}
	private static String getHeaderHtml(String title) {
		return "<!DOCTYPE html>\n"
				+ "<html>\n"
				+ "<head>\n"
				+ "<link rel=\"stylesheet\" href=\"css\\build.css\" type=\"text/css\" />\n"
				+ "<title>" + title + "</title>\n"
				+ "</head>\n"
				+ "<body>\n";	
	}
	private static String getFooterHtml() {
		return "</div>\n"
				+ "</body>\n"
				+ "</html>\n"
				+ "";
	}

	private List<String> getResources() {
		return resources;
	}
	
	private void resourceAdd(String resource) {
		if (resources == null) {
			resources = new ArrayList<String>();
			resources.add("css\\build.css");
			resources.add("images\\blank.png");
			resources.add("images\\power_bar.png");
		}
		if (!resources.contains(resource)) {
			resources.add(resource);
		}
	}
	private void deployResources(List<String> resources, String targetDir) {
		for (String resource : resources) {
			String toFilePath = String.format("%s\\%s", targetDir, resource);
			URL url = getClass().getResource(String.format("/%s", resource.replace("\\", "/")));
		    File sourceFile = new File(url.getPath());
		    File targetFile = new File(toFilePath);
		    if (!targetFile.exists()) {
		    	targetFile.getParentFile().mkdirs();
		    	try {
		    		FileUtils.copyFile(sourceFile, targetFile);
		    	} catch (IOException e) {
		    		// TODO Auto-generated catch block
		    		e.printStackTrace();
		    	}
		    }
		}
	}
}

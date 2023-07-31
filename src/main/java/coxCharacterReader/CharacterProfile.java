package coxCharacterReader;

public class CharacterProfile {
	private String name;
	private String architype;
	private String alignment;
	private String level;
	private String origin;
	private String primary;
	private String secondary;
	private String filename;
	private String title;
	public CharacterProfile(String name, String architype, String alignment, String level, String origin, String primary, String secondary, String filename, String title) {
		this.name = name;
		this.architype = architype;
		this.alignment = alignment;
		this.level = level;
		this.origin = origin;
		this.primary = primary;
		this.secondary = secondary;
		this.filename = filename;
		this.title = title;
	}
	public CharacterProfile(String name, String architype, String alignment, int level, String origin, String primary, String secondary, String filename, String title) {
		this.name = name;
		this.architype = architype;
		this.alignment = alignment;
		this.level = String.valueOf(level);
		this.origin = origin;
		this.primary = primary;
		this.secondary = secondary;
		this.filename = filename;
		this.title = title;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getArchitype() {
		return architype;
	}
	public void setArchitype(String architype) {
		this.architype = architype;
	}
	public String getAlignment() {
		return alignment;
	}
	public void setAlignment(String alignment) {
		this.alignment = alignment;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	public String getPrimary() {
		return primary;
	}
	public void setPrimary(String primary) {
		this.primary = primary;
	}
	public String getSecondary() {
		return secondary;
	}
	public void setSecondary(String secondary) {
		this.secondary = secondary;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}

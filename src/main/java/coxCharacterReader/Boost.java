package coxCharacterReader;

public class Boost {

	private String powerId;
	private String idx;
	private String categoryName;
	private String powerSetName;
	private String boostName;
	private String level;
	public String getPowerId() {
		return powerId;
	}
	public void setPowerId(String powerId) {
		this.powerId = powerId;
	}
	public String getIdx() {
		return idx;
	}
	/**
	 * Convert idx into the natural slot number.
	 * Return null as 1, add one to other values.
	 * @return String of slot number.
	 */
	public String extractSlotNumber() {
		if (idx != null) {
			int s = Integer.valueOf(idx);
			return String.valueOf(s+1);
		}
		return "1";
	}
	public void setIdx(String idx) {
		this.idx = idx;
	}
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	public String getPowerSetName() {
		return powerSetName;
	}
	public void setPowerSetName(String powerSetName) {
		this.powerSetName = powerSetName;
	}
	public String getBoostName() {
		return boostName;
	}
	public void setBoostName(String boostName) {
		this.boostName = boostName;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public void setProperty(String property, String value) {
		if ("PowerID".equalsIgnoreCase(property)) {
			this.powerId = value;
		} else if ("Idx".equalsIgnoreCase(property)) {
			this.idx = value;
		} else if ("CategoryName".equalsIgnoreCase(property)) {
			this.categoryName = value;
		} else if ("PowerSetName".equalsIgnoreCase(property)) {
			this.powerSetName = value;
		} else if ("BoostName".equalsIgnoreCase(property)) {
			this.boostName = value;
		} else if ("Level".equalsIgnoreCase(property)) {
			Integer lvl = Integer.valueOf(value);
			lvl = lvl + 1;
			this.level = String.valueOf(lvl);
		}
	}
	public String toString() {
		return String.format("Slot: level %s powerId %s idx %s categoryName %s powerSetName %s boostName %s",
				this.level, 
				this.powerId,
				this.idx,
				this.categoryName,
				this.powerSetName,
				this.boostName);
	}
}

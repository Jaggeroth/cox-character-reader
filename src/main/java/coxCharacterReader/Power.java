package coxCharacterReader;

public class Power {

	private String powerId;
	private String categoryName;
	private String powerLevelBought;
	private String powerSetName;
	private String powerName;
	private String powerSetLevelBought;
	private String uniqueId;

	public String getPowerId() {
		return powerId;
	}
	public void setPowerId(String powerId) {
		this.powerId = powerId;
	}
	public String getCategoryName() {
		return categoryName;
	}
	public String getPowerLevelBought() {
		return powerLevelBought != null ? powerLevelBought : getPowerSetLevelBought();
	}
	public void setPowerLevelBought(String powerLevelBought) {
		Integer level = Integer.valueOf(powerLevelBought);
		level = level + 1;
		this.powerLevelBought = level.toString();
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
	public String getPowerName() {
		return powerName;
	}
	public void setPowerName(String powerName) {
		this.powerName = powerName;
	}
	public String getPowerSetLevelBought() {
		return powerSetLevelBought != null ? powerSetLevelBought : "1";
	}
	public void setPowerSetLevelBought(String powerSetLevelBought) {
		Integer level = Integer.valueOf(powerSetLevelBought);
		level = level + 1;
		this.powerSetLevelBought =level.toString();
	}
	public String getUniqueId() {
		return uniqueId;
	}
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	public void setProperty(String property, String value) {
		if ("PowerId".equalsIgnoreCase(property)) {
			this.powerId = value;
		} else if ("CategoryName".equalsIgnoreCase(property)) {
			this.categoryName = value;
		} else if ("PowerLevelBought".equalsIgnoreCase(property)) {
			setPowerLevelBought(value);
		} else if ("PowerSetName".equalsIgnoreCase(property)) {
			this.powerSetName = value;
		} else if ("PowerName".equalsIgnoreCase(property)) {
			this.powerName = value;
		} else if ("PowerSetLevelBought".equalsIgnoreCase(property)) {
			setPowerSetLevelBought(value);
		} else if ("UniqueID".equalsIgnoreCase(property)) {
			this.uniqueId = value;
		}
	}
	public boolean isBuildOption() {
		if ("\"temporary_powers\"".equalsIgnoreCase(getCategoryName())) {
			return false;
		} else if ("\"inherent\"".equalsIgnoreCase(getCategoryName())) {
			return false;
		}
		return true;
	}
	/*
	 * Only return true for inherent powers that are slottable.
	 * Exclude powers like beast run etc
	 */
	public boolean isInherent() {
		if ("\"inherent\"".equalsIgnoreCase(getCategoryName())) {
			if ("\"brawl\"".equalsIgnoreCase(getPowerName())) {
				return true;
			} else if ("\"rest\"".equalsIgnoreCase(getPowerName())) {
				return true;
			} else if ("\"swift\"".equalsIgnoreCase(getPowerName())) {
				return true;
			} else if ("\"hurdle\"".equalsIgnoreCase(getPowerName())) {
				return true;
			} else if ("\"health\"".equalsIgnoreCase(getPowerName())) {
				return true;
			} else if ("\"stamina\"".equalsIgnoreCase(getPowerName())) {
				return true;
			}
		}
		return false;		
	}
	public String toString() {
		return String.format("Power: powerId %s categoryName %s powerSetName %s powerName %s powerLevelBought %s powerSetLevelBought %s uniqueId %s",
				this.powerId,
				this.categoryName,
				this.powerSetName,
				this.powerName,
				getPowerLevelBought(),
				getPowerSetLevelBought(),
				this.uniqueId);
	}
}

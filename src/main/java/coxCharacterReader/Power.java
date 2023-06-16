package coxCharacterReader;

public class Power {

	private String powerId;
	private String categoryName;
	private String powerLevelBought;
	private String powerSetName;
	private String powerName;
	private String powerSetLevelBought;
	private String uniqueId;
	private String disabled;

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
	public String getDisabled() {
		return disabled;
	}
	public void setDisabled(String disabled) {
		this.disabled = disabled;
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
		} else if ("Disabled".equalsIgnoreCase(property)) {
			this.disabled = value;
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
	public boolean isIncarnatePower() {
		if ("\"incarnate\"".equalsIgnoreCase(getCategoryName())) {
			return true;
		}
		return false;
	}
	public String getIncarnateTier() {
		if (isIncarnatePower()) {
			int l = getPowerName().split("_").length;
			if ("\"alpha\"".equalsIgnoreCase(getPowerSetName())) {
				if (getPowerName().endsWith("paragon\"")) {
					return "4";
				}
			} else if ("\"judgement\"".equalsIgnoreCase(getPowerSetName())) {
				if (getPowerName().endsWith("final_judgement\"")) {
					return "4";
				}
			} else if ("\"interface\"".equalsIgnoreCase(getPowerSetName())) {
				if (getPowerName().endsWith("flawless_interface\"")) {
					return "4";
				}
			} else if ("\"lore\"".equalsIgnoreCase(getPowerSetName())) {
				if (getPowerName().endsWith("superior_ally\"")) {
					return "4";
				} else if (getPowerName().endsWith("improved_ally\"")) {
					return "3";
				}
			} else if ("\"destiny\"".equalsIgnoreCase(getPowerSetName())) {
				if (getPowerName().endsWith("epiphany\"")) {
					return "4";
				}
			} else if ("\"hybrid\"".equalsIgnoreCase(getPowerSetName())) {
				if (getPowerName().startsWith("\"support_genome") || getPowerName().startsWith("\"melee_genome")) {
					int num = Integer.valueOf(getPowerName().substring(getPowerName().length() - 2,getPowerName().length() - 1));
					if (num > 7) {
						return "4";
					} else if (num > 3) {
						return "3";
					} else if (num > 1) {
						return "2";
					} else {
						return "1";
					}
				} else if (getPowerName().endsWith("embodiment\"")) {
					return "4";
				}
			}
			return String.valueOf(l-1);
		} else {
			return null;
		}
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

package me.rukon0621.guardians.data;

public enum ItemGrade {
    UNKNOWN(301, "알 수 없음", "&7", "&7알 수 없음", 0, "\uE01A"),
    NORMAL(301, "일반", "&f", "&f일반",1, "\uE01A"),
    UNCOMMON(302, "언커먼", "#94ef98", "#94ef98언커먼",2, "\uE01B"),
    UNIQUE(303, "유니크", "#ffd966", "#ffd966유니크",3, "\uE01C"),
    EPIC(304, "에픽", "#ba95e5", "#ba95e5에픽",4, "\uE01D"),
    LEGEND(305, "레전드", "#fd7c47", "#fd9961레#fd7c47전#fd5e2c드",5, "\uE01E"),
    ANCIENT(306, "에이션트", "#ff67af", "#ffaee8에#ff8acb이#ff67af션#ff4392트",6, "\uE01F");

    private final int blueprintCMD;
    private final String str;
    private final String color;
    private final String coloredStr;
    private final int level;
    private final String stoneUnicode;

    public static ItemGrade getValueByName(String name) {
        for(ItemGrade grade : ItemGrade.values()) {
            if(grade.getStr().equals(name)) return grade;
        }
        return ItemGrade.UNKNOWN;
    }

    ItemGrade(int customModelData, String str, String color , String coloredStr, int level, String stoneUnicode) {
        this.blueprintCMD = customModelData;
        this.str = str;
        this.color = color;
        this.coloredStr = coloredStr;
        this.level = level;
        this.stoneUnicode = stoneUnicode;
    }

    public int getLevel() {
        return level;
    }

    public int getBlueprintCMD() {
        return blueprintCMD;
    }

    public String getStr() {
        return str;
    }

    public String getColor() {
        return color;
    }

    public String getColoredStr() {
        return coloredStr;
    }

    public String getStoneUnicode() {
        return stoneUnicode;
    }
}

package me.rukon0621.guardians.helper;

public class StringComparator {

    public static boolean StringComparator(String s) {
        if(s.contains("==")) {
            String[] data = s.split("==");
            double value = Double.parseDouble(data[0].trim());
            double target = Double.parseDouble(data[1].trim());
            return value == target;
        }
        else if(s.contains("<=")) {
            String[] data = s.split("<=");
            double value = Double.parseDouble(data[0].trim());
            double target = Double.parseDouble(data[1].trim());
            return value <= target;
        }
        else if(s.contains(">=")) {
            String[] data = s.split(">=");
            double value = Double.parseDouble(data[0].trim());
            double target = Double.parseDouble(data[1].trim());
            return value >= target;
        }
        else if(s.contains("<")) {
            String[] data = s.split("<");
            double value = Double.parseDouble(data[0].trim());
            double target = Double.parseDouble(data[1].trim());
            return value < target;
        }
        else if(s.contains(">")) {
            String[] data = s.split(">");
            double value = Double.parseDouble(data[0].trim());
            double target = Double.parseDouble(data[1].trim());
            return value > target;
        }
        return false;
    }

}

package fr.insee.compas.util.sonar;

public class ComparatorUtils {

    private ComparatorUtils() {}

    public static double getMaxNumber(String value1, String value2) throws NumberFormatException {
        return Double.parseDouble(value1) + Double.parseDouble(value2);
    }

    public static String getMaxLetter(String value1, String value2) {
        return value1.compareToIgnoreCase(value2) >= 0 ? value1 : value2;
    }
}

package helpers;

import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtils {

    public static String ellipsis(String longSentence, Integer maxlength) {
        if (longSentence.length() > maxlength) {
            return longSentence.substring(0, maxlength) + "...";
        } else {
            return longSentence;
        }
    }

    public static Boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean compare(String str1, String str2) {
        return (str1 == null ? str2 == null : str1.equals(str2));
    }

    public static String normalize(Stream<String> strings) {
        return strings
            .filter(s -> s != null && !s.trim().isEmpty())
            .collect(Collectors.joining())
            // convert to lowercase and remove blanks
            .toLowerCase()
            .replaceAll("\\s+", "");
    }

    public static List<Integer> csvToListOfInteger(String csv) {
        return csvToListOfString(csv)
            .stream()
            .map(org.apache.commons.lang3.StringUtils::trim)
            .filter(Objects::nonNull)
            .filter(org.apache.commons.lang3.StringUtils::isNumeric)
            .mapToInt(Integer::parseInt)
            .boxed()
            .collect(Collectors.toList())
            ;
    }

    public static String converListToStringWithSeparator(List<String> stringList, String separator) {

        if (CollectionUtils.isEmpty(stringList)) {
            return "";
        }

        return stringList.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.joining(separator));
    }

    private static String[] csvToArray(String csv) {
        return org.apache.commons.lang3.StringUtils.split(csv, ",");
    }

    private static List<String> csvToListOfString(String csv) {
        if (csv == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(csvToArray(csv));
    }

}
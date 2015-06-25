package org.robotframework.ide.core.testData.text.lexer;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * Represents time words, which has special meaning in Robot Framework test
 * data.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see LowLevelTypesProvider
 */
public enum RobotTimeType implements RobotType {
    /**
     * add for do not return null value
     */
    UNKNOWN(null, null),
    /**
     * represents all possible combinations of word day
     */
    DAY("day", Arrays.asList("d", "day", "days")),
    /**
     * represents all possible combinations of word hour
     */
    HOUR("hour", Arrays.asList("h", "hour", "hours")),
    /**
     * represents all possible combinations of word minute
     */
    MINUTE("minute", Arrays.asList("m", "minute", "minutes")),
    /**
     * represents all possible combinations of word second
     */
    SECOND("second", Arrays.asList("s", "sec", "secs", "second", "seconds")),
    /**
     * represents all possible combinations of word millisecond
     */
    MILLISECOND("millisecond", Arrays.asList("ms", "millis", "millisecond",
            "milliseconds"));

    private final String toWriteText;
    private final List<String> types;


    @Override
    public String toWrite() {
        return toWriteText;
    }


    private RobotTimeType(final String toWriteText, final List<String> types) {
        this.toWriteText = toWriteText;
        if (types == null) {
            this.types = Collections.unmodifiableList(new LinkedList<String>());
        } else {
            this.types = Collections.unmodifiableList(types);
        }
    }


    public static RobotType getToken(String text) {
        RobotType type = RobotTimeType.UNKNOWN;
        if (text != null) {
            final RobotTimeType[] types = RobotTimeType.values();
            for (RobotTimeType cType : types) {
                if (cType.isMine(text)) {
                    type = cType;
                    break;
                }
            }
        }

        return type;
    }


    @Override
    public boolean isWriteable() {
        return (this.toWriteText != null);
    }


    public static RobotType getToken(StringBuilder text) {
        RobotType type = RobotTimeType.UNKNOWN;
        if (text != null) {
            type = getToken(text.toString());
        }

        return type;
    }


    @Override
    public RobotType getTokenType(StringBuilder text) {
        return getToken(text);
    }


    @Override
    public RobotType getTokenType(String text) {
        return getToken(text);
    }


    public boolean isMine(String text) {
        boolean result = false;
        if (text != null) {
            String lowerCase = text.toLowerCase();
            result = this.getPossibleRepresentations().contains(lowerCase);
        }

        return result;
    }


    /**
     * 
     * @return all possible representation of current time type, i.e. for DAY it
     *         will be: [d, day, days]
     */
    public List<String> getPossibleRepresentations() {
        return types;
    }
}

package org.robotframework.ide.core.testData.text.lexer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Represents time words, which has special meaning in Robot Framework test
 * data.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public enum RobotTimeType implements RobotType {
    /**
     * 
     */
    UNKNOWN(null, null),
    /**
     * 
     */
    DAYS("days", "days"),
    /**
     * 
     */
    DAY("day", "day"),
    /**
     * 
     */
    D_AS_DAY("d", "d"),
    /**
     * 
     */
    HOURS("hours", "hours"),
    /**
     * 
     */
    HOUR("hour", "hour"),
    /**
     * 
     */
    H_AS_HOUR("h", "h"),
    /**
     * 
     */
    MINUTES("minutes", "minutes"),
    /**
     * 
     */
    MINUTE("minute", "minute"),
    /**
     * 
     */
    M_AS_MINUTE("m", "m"),
    /**
     * 
     */
    SECONDS("seconds", "seconds"),
    /**
     * 
     */
    SECOND("second", "second"),
    /**
     * 
     */
    SECS("secs", "secs"),
    /**
     * 
     */
    SEC("sec", "sec"),
    /**
     * 
     */
    S_AS_SECOND("s", "s"),
    /**
     * 
     */
    MILLISECONDS("milliseconds", "milliseconds"),
    /**
     * 
     */
    MILLISECOND("millisecond", "millisecond"),
    /**
     * 
     */
    MILLIS("millis", "millis"),
    /**
     * 
     */
    MS_AS_MILLISECOND("ms", "ms");

    private final String aliases;
    private final String toWriteText;

    private static final Map<String, RobotTimeType> reservedWordTypes;

    static {
        Map<String, RobotTimeType> temp = new HashMap<>();
        RobotTimeType[] values = RobotTimeType.values();
        for (RobotTimeType type : values) {
            temp.put(type.aliases, type);
        }

        reservedWordTypes = Collections.unmodifiableMap(temp);
    }


    @Override
    public String toWrite() {
        return toWriteText;
    }


    public static RobotType getToken(String text) {
        RobotType type = RobotWordType.UNKNOWN_WORD;
        if (text != null) {
            RobotType foundType = reservedWordTypes.get(text.toLowerCase());
            if (foundType != null) {
                type = foundType;
            }
        }
        return type;
    }


    private RobotTimeType(final String aliases, final String toWriteText) {
        this.aliases = aliases;
        this.toWriteText = toWriteText;
    }


    @Override
    public boolean isWriteable() {
        return (this.toWriteText != null);
    }


    public static RobotType getToken(StringBuilder text) {
        RobotType type = RobotWordType.UNKNOWN_WORD;
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
}

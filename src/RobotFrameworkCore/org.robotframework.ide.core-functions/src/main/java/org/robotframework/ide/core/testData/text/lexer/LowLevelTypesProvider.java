package org.robotframework.ide.core.testData.text.lexer;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher;


/**
 * Handler of simple types like numbers or timestamp parts: seconds, minutes
 * etc. It is common place, where them should be put to be invoked in private
 * method {@link RobotTokenMatcher#convertToWordType(RobotToken)}
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see RobotTokenMatcher
 * @see RobotTimeType
 * @see NumberType
 */
public class LowLevelTypesProvider {

    private volatile static List<RobotType> lowLevelTypes = new LinkedList<>();

    static {
        lowLevelTypes.add(RobotTimeType.UNKNOWN);
        lowLevelTypes.add(NumberType.UNKNOWN);
    }


    /**
     * @param text
     * @return in case can not be recognized, its return
     *         {@link RobotWordType#UNKNOWN_WORD}
     */
    public static RobotType getTokenType(String text) {
        RobotType type = RobotWordType.UNKNOWN_WORD;
        for (RobotType rType : lowLevelTypes) {
            RobotType returnedType = rType.getTokenType(text);
            if (returnedType != rType) {
                type = returnedType;
                break;
            }
        }

        return type;
    }


    /**
     * @param text
     * @return in case can not be recognized or text is {@code null}, its return
     *         {@link RobotWordType#UNKNOWN_WORD}
     */
    public static RobotType getTokenType(StringBuilder text) {
        RobotType type = RobotWordType.UNKNOWN_WORD;
        if (text != null) {
            type = getTokenType(text.toString());
        }

        return type;
    }
}

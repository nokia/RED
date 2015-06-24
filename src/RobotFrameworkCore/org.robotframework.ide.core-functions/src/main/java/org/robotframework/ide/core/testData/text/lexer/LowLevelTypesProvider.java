package org.robotframework.ide.core.testData.text.lexer;

import java.util.LinkedList;
import java.util.List;


public class LowLevelTypesProvider {

    private static List<RobotType> lowLevelTypes = new LinkedList<>();

    static {
        lowLevelTypes.add(RobotTimeType.UNKNOWN);
        lowLevelTypes.add(NumberType.UNKNOWN);
    }


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


    public static RobotType getTokenType(StringBuilder text) {
        RobotType type = RobotWordType.UNKNOWN_WORD;
        if (text != null) {
            type = getTokenType(text.toString());
        }

        return type;
    }
}

package org.robotframework.ide.core.testData.text.lexer;

/**
 * Declares possible Robot Framework number types.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see LowLevelTypesProvider
 */
public enum NumberType implements IRobotTokenType {
    /**
     * default answer in case text is not number token
     */
    UNKNOWN {

        @Override
        protected boolean isMine(String text) {
            return false;
        }
    },
    /**
     * i.e. -1202002022
     */
    NUMBER_WITH_SIGN {

        @Override
        protected boolean isMine(String text) {
            boolean result = false;

            if (text != null && text.length() > 1) {
                char[] chars = text.toCharArray();
                if (chars[0] == '-') {
                    result = isCharsNumber(chars, 1);
                }
            }

            return result;
        }
    },
    /**
     * i.e. 1202002022
     */
    NUMBER_WITHOUT_SIGN {

        @Override
        protected boolean isMine(String text) {
            boolean result = false;

            if (text != null && text.length() > 0) {
                char[] chars = text.toCharArray();
                result = isCharsNumber(chars, 0);
            }

            return result;
        }
    };

    private static boolean isCharsNumber(char[] chars, int charBeginIndex) {
        boolean result = false;
        for (int charIndex = charBeginIndex; charIndex < chars.length; charIndex++) {
            char c = chars[charIndex];
            if (c >= '0' && c <= '9') {
                result = true;
            } else {
                result = false;
                break;
            }
        }

        return result;
    }


    @Override
    public boolean isWriteable() {
        return false;
    }


    @Override
    public String toWrite() {
        throw new UnsupportedOperationException(
                "Write should be performed from " + RobotToken.class
                        + "#getText(); method. Type: " + this);
    }


    @Override
    public IRobotTokenType getTokenType(StringBuilder text) {
        IRobotTokenType type = NumberType.UNKNOWN;
        if (text != null) {
            type = getTokenType(text.toString());
        }

        return type;
    }


    @Override
    public IRobotTokenType getTokenType(String text) {
        IRobotTokenType type = NumberType.UNKNOWN;
        NumberType[] values = NumberType.values();
        for (NumberType nType : values) {
            if (nType == NumberType.UNKNOWN) {
                continue;
            }

            if (nType.isMine(text)) {
                type = nType;
                break;
            }
        }

        return type;
    }


    protected abstract boolean isMine(String text);

}

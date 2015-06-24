package org.robotframework.ide.core.testData.text.lexer;

public enum NumberType implements RobotType {
    /**
     * 
     */
    UNKNOWN {

        @Override
        public boolean isMine(String text) {
            return false;
        }
    },
    /**
     * 
     */
    NEGATIVE_VALUE {

        @Override
        public boolean isMine(String text) {
            // TODO Auto-generated method stub
            return false;
        }
    };

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
    public RobotType getTokenType(StringBuilder text) {
        RobotType type = NumberType.UNKNOWN;
        if (text != null) {
            type = getTokenType(text.toString());
        }

        return type;
    }


    @Override
    public RobotType getTokenType(String text) {
        RobotType type = NumberType.UNKNOWN;
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

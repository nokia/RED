package org.robotframework.ide.core.testData.text.lexer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.robotframework.ide.core.testData.text.lexer.matcher.AsteriskMatcher;
import org.robotframework.ide.core.testData.text.lexer.matcher.DotSignMatcher;
import org.robotframework.ide.core.testData.text.lexer.matcher.HashCommentMatcher;


/**
 * Concatenation of the same special tokens like asterisk '*', which appears
 * together. The common information from this token type is that join them
 * together doesn't bring for them new meaning.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see AsteriskMatcher
 * @see HashCommentMatcher
 * @see DotSignMatcher
 */
public enum MultipleCharTokenType implements IRobotTokenType {
    /**
     * just for not return null in {@link #getToken(RobotToken)} method
     */
    UNKNOWN(RobotSingleCharTokenType.UNKNOWN) {

        @Override
        protected boolean isMine(String text) {
            return false;
        }
    },
    /**
     * in example: '***'
     */
    MANY_ASTERISKS(RobotSingleCharTokenType.SINGLE_ASTERISK) {

        @Override
        protected boolean isMine(String text) {
            boolean result = false;

            char required = getWrappedType().getThisTokenChar();
            if (text != null && text.length() >= 2) {
                char[] chars = text.toCharArray();
                result = true;
                for (char c : chars) {
                    if (c != required) {
                        result = false;
                        break;
                    }
                }
            }

            return result;
        }
    },
    /**
     * in example: '###'
     */
    MANY_COMMENT_HASHS(RobotSingleCharTokenType.SINGLE_COMMENT_HASH) {

        @Override
        protected boolean isMine(String text) {
            boolean result = false;

            char required = getWrappedType().getThisTokenChar();
            if (text != null && text.length() >= 2) {
                char[] chars = text.toCharArray();
                result = true;
                for (char c : chars) {
                    if (c != required) {
                        result = false;
                        break;
                    }
                }
            }

            return result;
        }
    },
    /**
     * more than 3 dot signs, in example: '....'
     */
    MORE_THAN_THREE_DOTS(RobotSingleCharTokenType.SINGLE_DOT) {

        @Override
        protected boolean isMine(String text) {
            boolean result = false;

            char required = getWrappedType().getThisTokenChar();
            if (text != null && text.length() > 3) {
                char[] chars = text.toCharArray();
                result = true;
                for (char c : chars) {
                    if (c != required) {
                        result = false;
                        break;
                    }
                }
            }

            return result;
        }
    };

    private final RobotSingleCharTokenType wrappedType;
    private static final Map<RobotSingleCharTokenType, MultipleCharTokenType> reservedWordTypes;

    static {
        Map<RobotSingleCharTokenType, MultipleCharTokenType> temp = new HashMap<>();
        MultipleCharTokenType[] values = MultipleCharTokenType.values();
        for (MultipleCharTokenType type : values) {
            temp.put(type.wrappedType, type);
        }

        reservedWordTypes = Collections.unmodifiableMap(temp);
    }


    public static MultipleCharTokenType getToken(final IRobotTokenType previousType) {
        MultipleCharTokenType groupedSameTokenType = reservedWordTypes
                .get(previousType);

        if (groupedSameTokenType == null) {
            groupedSameTokenType = MultipleCharTokenType.UNKNOWN;
        }

        return groupedSameTokenType;
    }


    public boolean isFromThisGroup(RobotToken token) {
        IRobotTokenType tokenType = token.getType();
        return (tokenType == this) || (tokenType == this.wrappedType);
    }


    private MultipleCharTokenType(final RobotSingleCharTokenType wrappedType) {
        this.wrappedType = wrappedType;
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


    public RobotSingleCharTokenType getWrappedType() {
        return this.wrappedType;
    }


    @Override
    public IRobotTokenType getTokenType(StringBuilder text) {
        IRobotTokenType type = UNKNOWN;
        if (text != null) {
            type = getTokenType(text.toString());
        }

        return type;
    }


    @Override
    public IRobotTokenType getTokenType(String text) {
        IRobotTokenType type = UNKNOWN;
        MultipleCharTokenType[] values = MultipleCharTokenType.values();
        for (MultipleCharTokenType tType : values) {
            if (tType == UNKNOWN) {
                continue;
            }

            if (tType.isMine(text)) {
                type = tType;
                break;
            }
        }

        return type;
    }


    protected abstract boolean isMine(String text);
}

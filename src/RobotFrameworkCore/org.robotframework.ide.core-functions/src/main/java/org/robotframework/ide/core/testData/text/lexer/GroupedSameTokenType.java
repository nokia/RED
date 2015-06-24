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
public enum GroupedSameTokenType implements RobotType {
    /**
     * just for not return null in {@link #getToken(RobotToken)} method
     */
    UNKNOWN(RobotTokenType.UNKNOWN) {

        @Override
        protected boolean isMine(String text) {
            return false;
        }
    },
    /**
     * in example: '***'
     */
    MANY_ASTERISKS(RobotTokenType.SINGLE_ASTERISK) {

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
    MANY_COMMENT_HASHS(RobotTokenType.SINGLE_COMMENT_HASH) {

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
    MORE_THAN_THREE_DOTS(RobotTokenType.SINGLE_DOT) {

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

    private final RobotTokenType wrappedType;
    private static final Map<RobotTokenType, GroupedSameTokenType> reservedWordTypes;

    static {
        Map<RobotTokenType, GroupedSameTokenType> temp = new HashMap<>();
        GroupedSameTokenType[] values = GroupedSameTokenType.values();
        for (GroupedSameTokenType type : values) {
            temp.put(type.wrappedType, type);
        }

        reservedWordTypes = Collections.unmodifiableMap(temp);
    }


    public static GroupedSameTokenType getToken(final RobotType previousType) {
        GroupedSameTokenType groupedSameTokenType = reservedWordTypes
                .get(previousType);

        if (groupedSameTokenType == null) {
            groupedSameTokenType = GroupedSameTokenType.UNKNOWN;
        }

        return groupedSameTokenType;
    }


    public boolean isFromThisGroup(RobotToken token) {
        RobotType tokenType = token.getType();
        return (tokenType == this) || (tokenType == this.wrappedType);
    }


    private GroupedSameTokenType(final RobotTokenType wrappedType) {
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


    public RobotTokenType getWrappedType() {
        return this.wrappedType;
    }


    @Override
    public RobotType getTokenType(StringBuilder text) {
        RobotType type = UNKNOWN;
        if (text != null) {
            type = getTokenType(text.toString());
        }

        return type;
    }


    @Override
    public RobotType getTokenType(String text) {
        RobotType type = UNKNOWN;
        GroupedSameTokenType[] values = GroupedSameTokenType.values();
        for (GroupedSameTokenType tType : values) {
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

package org.robotframework.ide.core.testData.text.lexer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType.HELPER;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see RobotSingleCharTokenType
 */
public class RobotTokenTypeTest {

    @Test
    public void test_getTokenType_zeroCharacters_shouldReturn_UNKNOWN() {
        assertThat(RobotSingleCharTokenType.UNKNOWN.getTokenType("")).isEqualTo(
                RobotSingleCharTokenType.UNKNOWN);
        assertThat(RobotSingleCharTokenType.UNKNOWN.getTokenType(new StringBuilder("")))
                .isEqualTo(RobotSingleCharTokenType.UNKNOWN);
    }


    @Test
    public void test_getTokenType_nullAsParam_shouldReturn_UNKNOWN() {
        String text = null;
        assertThat(RobotSingleCharTokenType.UNKNOWN.getTokenType(text)).isEqualTo(
                RobotSingleCharTokenType.UNKNOWN);
        StringBuilder text2 = null;
        assertThat(RobotSingleCharTokenType.UNKNOWN.getTokenType(text2)).isEqualTo(
                RobotSingleCharTokenType.UNKNOWN);
    }


    @Test
    public void test_getTokenType_moreThanOneChar_shouldReturn_UNKNOWN() {
        assertThat(RobotSingleCharTokenType.UNKNOWN.getTokenType("...")).isEqualTo(
                RobotSingleCharTokenType.UNKNOWN);
        assertThat(
                RobotSingleCharTokenType.UNKNOWN.getTokenType(new StringBuilder("...")))
                .isEqualTo(RobotSingleCharTokenType.UNKNOWN);
    }


    @Test
    public void test_typeSINGLE_QUOTE_MARK() {
        char tokenChar = '"';
        RobotSingleCharTokenType type = RobotSingleCharTokenType.SINGLE_QUOTE_MARK;

        assertThat(type.getThisTokenChar()).isEqualTo(tokenChar);
        assertThat(type.toWrite()).isEqualTo("" + tokenChar);
    }


    @Test
    public void test_typeSINGLE_DOT() {
        char tokenChar = '.';
        RobotSingleCharTokenType type = RobotSingleCharTokenType.SINGLE_DOT;

        assertThat(type.getThisTokenChar()).isEqualTo(tokenChar);
        assertThat(type.toWrite()).isEqualTo("" + tokenChar);
    }


    @Test
    public void test_typeSINGLE_COLON() {
        char tokenChar = ':';
        RobotSingleCharTokenType type = RobotSingleCharTokenType.SINGLE_COLON;

        assertThat(type.getThisTokenChar()).isEqualTo(tokenChar);
        assertThat(type.toWrite()).isEqualTo("" + tokenChar);
    }


    @Test
    public void test_typeSINGLE_POSSITION_INDEX_END_SQUARE_BRACKET() {
        char tokenChar = ']';
        RobotSingleCharTokenType type = RobotSingleCharTokenType.SINGLE_POSSITION_INDEX_END_SQUARE_BRACKET;

        assertThat(type.getThisTokenChar()).isEqualTo(tokenChar);
        assertThat(type.toWrite()).isEqualTo("" + tokenChar);
    }


    @Test
    public void test_typeSINGLE_POSSITION_INDEX_BEGIN_SQUARE_BRACKET() {
        char tokenChar = '[';
        RobotSingleCharTokenType type = RobotSingleCharTokenType.SINGLE_POSSITION_INDEX_BEGIN_SQUARE_BRACKET;

        assertThat(type.getThisTokenChar()).isEqualTo(tokenChar);
        assertThat(type.toWrite()).isEqualTo("" + tokenChar);
    }


    @Test
    public void test_typeSINGLE_VARIABLE_END_CURLY_BRACKET() {
        char tokenChar = '}';
        RobotSingleCharTokenType type = RobotSingleCharTokenType.SINGLE_VARIABLE_END_CURLY_BRACKET;

        assertThat(type.getThisTokenChar()).isEqualTo(tokenChar);
        assertThat(type.toWrite()).isEqualTo("" + tokenChar);
    }


    @Test
    public void test_typeSINGLE_VARIABLE_BEGIN_CURLY_BRACKET() {
        char tokenChar = '{';
        RobotSingleCharTokenType type = RobotSingleCharTokenType.SINGLE_VARIABLE_BEGIN_CURLY_BRACKET;

        assertThat(type.getThisTokenChar()).isEqualTo(tokenChar);
        assertThat(type.toWrite()).isEqualTo("" + tokenChar);
    }


    @Test
    public void test_typeSINGLE_EQUAL() {
        char tokenChar = '=';
        RobotSingleCharTokenType type = RobotSingleCharTokenType.SINGLE_EQUAL;

        assertThat(type.getThisTokenChar()).isEqualTo(tokenChar);
        assertThat(type.toWrite()).isEqualTo("" + tokenChar);
    }


    @Test
    public void test_typeSINGLE_DICTIONARY_BEGIN_AMPERSAND() {
        char tokenChar = '&';
        RobotSingleCharTokenType type = RobotSingleCharTokenType.SINGLE_DICTIONARY_BEGIN_AMPERSAND;

        assertThat(type.getThisTokenChar()).isEqualTo(tokenChar);
        assertThat(type.toWrite()).isEqualTo("" + tokenChar);
    }


    @Test
    public void test_typeSINGLE_ENVIRONMENT_BEGIN_PROCENT() {
        char tokenChar = '%';
        RobotSingleCharTokenType type = RobotSingleCharTokenType.SINGLE_ENVIRONMENT_BEGIN_PROCENT;

        assertThat(type.getThisTokenChar()).isEqualTo(tokenChar);
        assertThat(type.toWrite()).isEqualTo("" + tokenChar);
    }


    @Test
    public void test_typeSINGLE_LIST_BEGIN_AT() {
        char tokenChar = '@';
        RobotSingleCharTokenType type = RobotSingleCharTokenType.SINGLE_LIST_BEGIN_AT;

        assertThat(type.getThisTokenChar()).isEqualTo(tokenChar);
        assertThat(type.toWrite()).isEqualTo("" + tokenChar);
    }


    @Test
    public void test_typeSINGLE_SCALAR_BEGIN_DOLLAR() {
        char tokenChar = '$';
        RobotSingleCharTokenType type = RobotSingleCharTokenType.SINGLE_SCALAR_BEGIN_DOLLAR;

        assertThat(type.getThisTokenChar()).isEqualTo(tokenChar);
        assertThat(type.toWrite()).isEqualTo("" + tokenChar);
    }


    @Test
    public void test_typeSINGLE_ESCAPE_BACKSLASH() {
        char tokenChar = '\\';
        RobotSingleCharTokenType type = RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH;

        assertThat(type.getThisTokenChar()).isEqualTo(tokenChar);
        assertThat(type.toWrite()).isEqualTo("" + tokenChar);
    }


    @Test
    public void test_typeSINGLE_COMMENT_HASH() {
        char tokenChar = '#';
        RobotSingleCharTokenType type = RobotSingleCharTokenType.SINGLE_COMMENT_HASH;

        assertThat(type.getThisTokenChar()).isEqualTo(tokenChar);
        assertThat(type.toWrite()).isEqualTo("" + tokenChar);
    }


    @Test
    public void test_typeSINGLE_ASTERISK() {
        char tokenChar = '*';
        RobotSingleCharTokenType type = RobotSingleCharTokenType.SINGLE_ASTERISK;

        assertThat(type.getThisTokenChar()).isEqualTo(tokenChar);
        assertThat(type.toWrite()).isEqualTo("" + tokenChar);
    }


    @Test
    public void test_typeSINGLE_PIPE() {
        char tokenChar = '|';
        RobotSingleCharTokenType type = RobotSingleCharTokenType.SINGLE_PIPE;

        assertThat(type.getThisTokenChar()).isEqualTo(tokenChar);
        assertThat(type.toWrite()).isEqualTo("" + tokenChar);
    }


    @Test
    public void test_typeSINGLE_SPACE() {
        char tokenChar = ' ';
        RobotSingleCharTokenType type = RobotSingleCharTokenType.SINGLE_SPACE;

        assertThat(type.getThisTokenChar()).isEqualTo(tokenChar);
        assertThat(type.toWrite()).isEqualTo("" + tokenChar);
    }


    @Test
    public void test_typeSINGLE_TABULATOR() {
        char tokenChar = '\t';
        RobotSingleCharTokenType type = RobotSingleCharTokenType.SINGLE_TABULATOR;

        assertThat(type.getThisTokenChar()).isEqualTo(tokenChar);
        assertThat(type.toWrite()).isEqualTo("" + tokenChar);
    }


    @Test
    public void test_typeLINE_FEED() {
        char tokenChar = '\n';
        RobotSingleCharTokenType type = RobotSingleCharTokenType.LINE_FEED;

        assertThat(type.getThisTokenChar()).isEqualTo(tokenChar);
        assertThat(type.toWrite()).isEqualTo("" + tokenChar);
    }


    @Test
    public void test_typeCARRIAGE_RETURN() {
        char tokenChar = '\r';
        RobotSingleCharTokenType type = RobotSingleCharTokenType.CARRIAGE_RETURN;

        assertThat(type.getThisTokenChar()).isEqualTo(tokenChar);
        assertThat(type.toWrite()).isEqualTo("" + tokenChar);
    }


    @Test
    public void test_typeEND_OF_LINE() {
        RobotSingleCharTokenType type = RobotSingleCharTokenType.END_OF_LINE;

        assertThat(type.getThisTokenChar()).isEqualTo(HELPER.UNWRITABLE_CHARS);
        assertThat(type.toWrite()).isEmpty();
    }


    @Test
    public void test_typeUNKNOWN() {
        RobotSingleCharTokenType type = RobotSingleCharTokenType.UNKNOWN;

        assertThat(type.getThisTokenChar()).isEqualTo(HELPER.UNWRITABLE_CHARS);
        assertThat(type.toWrite()).isEmpty();
    }


    @Test
    public void test_getToken_checkIfMapOfRobotWordTypesIsCoherent() {
        // prepare
        RobotSingleCharTokenType[] tokenTypes = RobotSingleCharTokenType.values();

        // execute & verify
        assertThat(tokenTypes).isNotNull();
        assertThat(tokenTypes).hasSize(22);

        for (RobotSingleCharTokenType type : tokenTypes) {
            char thisTokenChar = type.getThisTokenChar();
            if (thisTokenChar == HELPER.UNWRITABLE_CHARS) {
                assertThat(RobotSingleCharTokenType.getToken(thisTokenChar)).isEqualTo(
                        RobotSingleCharTokenType.UNKNOWN);
                assertThat(((IRobotTokenType) type).isWriteable()).isFalse();
                assertThat(type.getTokenType("" + thisTokenChar)).isEqualTo(
                        RobotSingleCharTokenType.UNKNOWN);
                assertThat(
                        type.getTokenType(new StringBuilder("" + thisTokenChar)))
                        .isEqualTo(RobotSingleCharTokenType.UNKNOWN);
            } else {
                assertThat(RobotSingleCharTokenType.getToken(thisTokenChar)).isEqualTo(
                        type);
                assertThat(((IRobotTokenType) type).isWriteable()).isTrue();
                assertThat(type.getTokenType("" + thisTokenChar)).isEqualTo(
                        type);
                assertThat(
                        type.getTokenType(new StringBuilder("" + thisTokenChar)))
                        .isEqualTo(type);
            }
        }
    }
}

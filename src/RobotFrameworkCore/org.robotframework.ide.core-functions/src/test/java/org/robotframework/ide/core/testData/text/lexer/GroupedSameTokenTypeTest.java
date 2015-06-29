package org.robotframework.ide.core.testData.text.lexer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.junit.Test;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see MultipleCharTokenType
 */
public class GroupedSameTokenTypeTest {

    @Test
    public void test_getTokenType_fourhDotsAndSpaceInside_shouldReturn_UNKNOWN() {
        String text = "... .";
        IRobotTokenType expectedType = MultipleCharTokenType.UNKNOWN;
        assertGetTokenType(text, expectedType);
    }


    @Test
    public void test_getTokenType_fourDots_shouldReturn_MORE_THAN_THREE_DOTS() {
        String text = "....";
        IRobotTokenType expectedType = MultipleCharTokenType.MORE_THAN_THREE_DOTS;
        assertGetTokenType(text, expectedType);
    }


    @Test
    public void test_getTokenType_oneDot_shouldReturn_UNKNOWN() {
        String text = ".";
        IRobotTokenType expectedType = MultipleCharTokenType.UNKNOWN;
        assertGetTokenType(text, expectedType);
    }


    @Test
    public void test_getTokenType_threeHashAndSpaceInside_shouldReturn_UNKNOWN() {
        String text = "## #";
        IRobotTokenType expectedType = MultipleCharTokenType.UNKNOWN;
        assertGetTokenType(text, expectedType);
    }


    @Test
    public void test_getTokenType_twoHash_shouldReturn_MANY_COMMENT_HASHS() {
        String text = "##";
        IRobotTokenType expectedType = MultipleCharTokenType.MANY_COMMENT_HASHS;
        assertGetTokenType(text, expectedType);
    }


    @Test
    public void test_getTokenType_oneHash_shouldReturn_UNKNOWN() {
        String text = "#";
        IRobotTokenType expectedType = MultipleCharTokenType.UNKNOWN;
        assertGetTokenType(text, expectedType);
    }


    @Test
    public void test_getTokenType_threeAsteriskAndSpaceInside_shouldReturn_UNKNOWN() {
        String text = "** *";
        IRobotTokenType expectedType = MultipleCharTokenType.UNKNOWN;
        assertGetTokenType(text, expectedType);
    }


    @Test
    public void test_getTokenType_threeAsterisk_shouldReturn_MANY_ASTERISKS() {
        String text = "***";
        IRobotTokenType expectedType = MultipleCharTokenType.MANY_ASTERISKS;
        assertGetTokenType(text, expectedType);
    }


    @Test
    public void test_getTokenType_twoAsterisk_shouldReturn_MANY_ASTERISKS() {
        String text = "**";
        IRobotTokenType expectedType = MultipleCharTokenType.MANY_ASTERISKS;
        assertGetTokenType(text, expectedType);
    }


    @Test
    public void test_getTokenType_oneAsterisk_shouldReturn_UNKNOWN() {
        String text = "*";
        IRobotTokenType expectedType = MultipleCharTokenType.UNKNOWN;
        assertGetTokenType(text, expectedType);
    }


    private void assertGetTokenType(String text, IRobotTokenType expectedType) {
        // prepare
        StringBuilder str = new StringBuilder(text);

        // execute
        IRobotTokenType tokenType = MultipleCharTokenType.UNKNOWN.getTokenType(str);
        IRobotTokenType tokenTypeViaText = MultipleCharTokenType.UNKNOWN
                .getTokenType(text);

        // verify
        assertThat(tokenType).isNotNull();
        assertThat(tokenType).isEqualTo(expectedType);
        assertThat(tokenTypeViaText).isNotNull();
        assertThat(tokenTypeViaText).isEqualTo(expectedType);
    }


    @Test
    public void test_getTokenType_viaStringBuilder_NULL() {
        // prepare
        StringBuilder str = null;

        // execute
        IRobotTokenType tokenType = MultipleCharTokenType.UNKNOWN.getTokenType(str);

        // verify
        assertThat(tokenType).isNotNull();
        assertThat(tokenType).isEqualTo(MultipleCharTokenType.UNKNOWN);
    }


    @Test
    public void test_typeMORE_THAN_THREE_DOTS() {
        MultipleCharTokenType type = MultipleCharTokenType.MORE_THAN_THREE_DOTS;

        assertThat(type.getWrappedType()).isEqualTo(RobotSingleCharTokenType.SINGLE_DOT);
        assertThat(type.isWriteable()).isFalse();
        RobotToken token = new RobotToken(
                LinearPositionMarker.createMarkerForFirstLineAndColumn(), null,
                LinearPositionMarker.createMarkerForFirstLineAndColumn());
        token.setType(MultipleCharTokenType.MORE_THAN_THREE_DOTS);
        assertThat(type.isFromThisGroup(token));
        token.setType(RobotSingleCharTokenType.SINGLE_DOT);
        assertThat(type.isFromThisGroup(token));

        try {
            type.toWrite();
            fail("Exception should occurs");
        } catch (UnsupportedOperationException uoe) {
            assertThat(uoe.getMessage()).isEqualTo(
                    "Write should be performed from " + RobotToken.class
                            + "#getText(); method. Type: " + type);
        }
    }


    @Test
    public void test_typeMANY_COMMENT_HASHS() {
        MultipleCharTokenType type = MultipleCharTokenType.MANY_COMMENT_HASHS;

        assertThat(type.getWrappedType()).isEqualTo(
                RobotSingleCharTokenType.SINGLE_COMMENT_HASH);
        assertThat(type.isWriteable()).isFalse();
        RobotToken token = new RobotToken(
                LinearPositionMarker.createMarkerForFirstLineAndColumn(), null,
                LinearPositionMarker.createMarkerForFirstLineAndColumn());
        token.setType(MultipleCharTokenType.MANY_COMMENT_HASHS);
        assertThat(type.isFromThisGroup(token));
        token.setType(RobotSingleCharTokenType.SINGLE_COMMENT_HASH);
        assertThat(type.isFromThisGroup(token));

        try {
            type.toWrite();
            fail("Exception should occurs");
        } catch (UnsupportedOperationException uoe) {
            assertThat(uoe.getMessage()).isEqualTo(
                    "Write should be performed from " + RobotToken.class
                            + "#getText(); method. Type: " + type);
        }
    }


    @Test
    public void test_typeMANY_ASTERISKS() {
        MultipleCharTokenType type = MultipleCharTokenType.MANY_ASTERISKS;

        assertThat(type.getWrappedType()).isEqualTo(
                RobotSingleCharTokenType.SINGLE_ASTERISK);
        assertThat(type.isWriteable()).isFalse();
        RobotToken token = new RobotToken(
                LinearPositionMarker.createMarkerForFirstLineAndColumn(), null,
                LinearPositionMarker.createMarkerForFirstLineAndColumn());
        token.setType(MultipleCharTokenType.MANY_ASTERISKS);
        assertThat(type.isFromThisGroup(token));
        token.setType(RobotSingleCharTokenType.SINGLE_ASTERISK);
        assertThat(type.isFromThisGroup(token));

        try {
            type.toWrite();
            fail("Exception should occurs");
        } catch (UnsupportedOperationException uoe) {
            assertThat(uoe.getMessage()).isEqualTo(
                    "Write should be performed from " + RobotToken.class
                            + "#getText(); method. Type: " + type);
        }
    }


    @Test
    public void test_typeUNKNOWN() {
        MultipleCharTokenType type = MultipleCharTokenType.UNKNOWN;

        assertThat(type.getWrappedType()).isEqualTo(RobotSingleCharTokenType.UNKNOWN);
        assertThat(type.isWriteable()).isFalse();
        RobotToken token = new RobotToken(
                LinearPositionMarker.createMarkerForFirstLineAndColumn(), null,
                LinearPositionMarker.createMarkerForFirstLineAndColumn());
        token.setType(MultipleCharTokenType.UNKNOWN);
        assertThat(type.isFromThisGroup(token));
        try {
            type.toWrite();
            fail("Exception should occurs");
        } catch (UnsupportedOperationException uoe) {
            assertThat(uoe.getMessage()).isEqualTo(
                    "Write should be performed from " + RobotToken.class
                            + "#getText(); method. Type: " + type);
        }
    }


    @Test
    public void test_getToken_checkIfMapOfRobotWordTypesIsCoherent() {
        // prepare
        MultipleCharTokenType[] tokenTypes = MultipleCharTokenType.values();

        // execute & verify
        assertThat(tokenTypes).isNotNull();
        assertThat(tokenTypes).hasSize(4);
        for (MultipleCharTokenType type : tokenTypes) {
            assertThat(MultipleCharTokenType.getToken(type.getWrappedType()))
                    .isEqualTo(type);
        }
    }
}

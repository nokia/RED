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
 * @see GroupedSameTokenType
 */
public class GroupedSameTokenTypeTest {

    @Test
    public void test_getTokenType_fourhDotsAndSpaceInside_shouldReturn_UNKNOWN() {
        String text = "... .";
        RobotType expectedType = GroupedSameTokenType.UNKNOWN;
        assertGetTokenType(text, expectedType);
    }


    @Test
    public void test_getTokenType_fourDots_shouldReturn_MORE_THAN_THREE_DOTS() {
        String text = "....";
        RobotType expectedType = GroupedSameTokenType.MORE_THAN_THREE_DOTS;
        assertGetTokenType(text, expectedType);
    }


    @Test
    public void test_getTokenType_oneDot_shouldReturn_UNKNOWN() {
        String text = ".";
        RobotType expectedType = GroupedSameTokenType.UNKNOWN;
        assertGetTokenType(text, expectedType);
    }


    @Test
    public void test_getTokenType_threeHashAndSpaceInside_shouldReturn_UNKNOWN() {
        String text = "## #";
        RobotType expectedType = GroupedSameTokenType.UNKNOWN;
        assertGetTokenType(text, expectedType);
    }


    @Test
    public void test_getTokenType_twoHash_shouldReturn_MANY_COMMENT_HASHS() {
        String text = "##";
        RobotType expectedType = GroupedSameTokenType.MANY_COMMENT_HASHS;
        assertGetTokenType(text, expectedType);
    }


    @Test
    public void test_getTokenType_oneHash_shouldReturn_UNKNOWN() {
        String text = "#";
        RobotType expectedType = GroupedSameTokenType.UNKNOWN;
        assertGetTokenType(text, expectedType);
    }


    @Test
    public void test_getTokenType_threeAsteriskAndSpaceInside_shouldReturn_UNKNOWN() {
        String text = "** *";
        RobotType expectedType = GroupedSameTokenType.UNKNOWN;
        assertGetTokenType(text, expectedType);
    }


    @Test
    public void test_getTokenType_threeAsterisk_shouldReturn_MANY_ASTERISKS() {
        String text = "***";
        RobotType expectedType = GroupedSameTokenType.MANY_ASTERISKS;
        assertGetTokenType(text, expectedType);
    }


    @Test
    public void test_getTokenType_twoAsterisk_shouldReturn_MANY_ASTERISKS() {
        String text = "**";
        RobotType expectedType = GroupedSameTokenType.MANY_ASTERISKS;
        assertGetTokenType(text, expectedType);
    }


    @Test
    public void test_getTokenType_oneAsterisk_shouldReturn_UNKNOWN() {
        String text = "*";
        RobotType expectedType = GroupedSameTokenType.UNKNOWN;
        assertGetTokenType(text, expectedType);
    }


    private void assertGetTokenType(String text, RobotType expectedType) {
        // prepare
        StringBuilder str = new StringBuilder(text);

        // execute
        RobotType tokenType = GroupedSameTokenType.UNKNOWN.getTokenType(str);
        RobotType tokenTypeViaText = GroupedSameTokenType.UNKNOWN
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
        RobotType tokenType = GroupedSameTokenType.UNKNOWN.getTokenType(str);

        // verify
        assertThat(tokenType).isNotNull();
        assertThat(tokenType).isEqualTo(GroupedSameTokenType.UNKNOWN);
    }


    @Test
    public void test_typeMORE_THAN_THREE_DOTS() {
        GroupedSameTokenType type = GroupedSameTokenType.MORE_THAN_THREE_DOTS;

        assertThat(type.getWrappedType()).isEqualTo(RobotTokenType.SINGLE_DOT);
        assertThat(type.isWriteable()).isFalse();
        RobotToken token = new RobotToken(
                LinearPositionMarker.createMarkerForFirstLineAndColumn(), null,
                LinearPositionMarker.createMarkerForFirstLineAndColumn());
        token.setType(GroupedSameTokenType.MORE_THAN_THREE_DOTS);
        assertThat(type.isFromThisGroup(token));
        token.setType(RobotTokenType.SINGLE_DOT);
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
        GroupedSameTokenType type = GroupedSameTokenType.MANY_COMMENT_HASHS;

        assertThat(type.getWrappedType()).isEqualTo(
                RobotTokenType.SINGLE_COMMENT_HASH);
        assertThat(type.isWriteable()).isFalse();
        RobotToken token = new RobotToken(
                LinearPositionMarker.createMarkerForFirstLineAndColumn(), null,
                LinearPositionMarker.createMarkerForFirstLineAndColumn());
        token.setType(GroupedSameTokenType.MANY_COMMENT_HASHS);
        assertThat(type.isFromThisGroup(token));
        token.setType(RobotTokenType.SINGLE_COMMENT_HASH);
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
        GroupedSameTokenType type = GroupedSameTokenType.MANY_ASTERISKS;

        assertThat(type.getWrappedType()).isEqualTo(
                RobotTokenType.SINGLE_ASTERISK);
        assertThat(type.isWriteable()).isFalse();
        RobotToken token = new RobotToken(
                LinearPositionMarker.createMarkerForFirstLineAndColumn(), null,
                LinearPositionMarker.createMarkerForFirstLineAndColumn());
        token.setType(GroupedSameTokenType.MANY_ASTERISKS);
        assertThat(type.isFromThisGroup(token));
        token.setType(RobotTokenType.SINGLE_ASTERISK);
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
        GroupedSameTokenType type = GroupedSameTokenType.UNKNOWN;

        assertThat(type.getWrappedType()).isEqualTo(RobotTokenType.UNKNOWN);
        assertThat(type.isWriteable()).isFalse();
        RobotToken token = new RobotToken(
                LinearPositionMarker.createMarkerForFirstLineAndColumn(), null,
                LinearPositionMarker.createMarkerForFirstLineAndColumn());
        token.setType(GroupedSameTokenType.UNKNOWN);
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
        GroupedSameTokenType[] tokenTypes = GroupedSameTokenType.values();

        // execute & verify
        assertThat(tokenTypes).isNotNull();
        assertThat(tokenTypes).hasSize(4);
        for (GroupedSameTokenType type : tokenTypes) {
            assertThat(GroupedSameTokenType.getToken(type.getWrappedType()))
                    .isEqualTo(type);
        }
    }
}

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
        assertThat(tokenTypes).hasSize(3);
        for (GroupedSameTokenType type : tokenTypes) {
            assertThat(GroupedSameTokenType.getToken(type.getWrappedType()))
                    .isEqualTo(type);
        }
    }
}

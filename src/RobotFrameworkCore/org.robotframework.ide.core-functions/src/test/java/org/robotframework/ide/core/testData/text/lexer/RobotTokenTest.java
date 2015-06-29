package org.robotframework.ide.core.testData.text.lexer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see RobotToken
 */
public class RobotTokenTest {

    @Test
    public void test_construction_withEndsComputeConstructor_startPositionIsFirstColumnAndLine_textIsEmpty() {
        // prepare
        LinearPositionMarker start = LinearPositionMarker
                .createMarkerForFirstLineAndColumn();
        StringBuilder text = new StringBuilder();

        // execute
        RobotToken token = new RobotToken(start, text);

        // verify
        assertThat(token.getStartPosition()).isEqualTo(start);
        assertThat(token.getText()).isEqualTo(text);
        LinearPositionMarker endPosition = token.getEndPosition();
        assertThat(endPosition).isNotNull();
        assertThat(endPosition.getLine()).isEqualTo(start.getLine());
        assertThat(endPosition.getColumn()).isEqualTo(start.getColumn());
        assertThat(token.getType()).isEqualTo(RobotSingleCharTokenType.UNKNOWN);
    }


    @Test
    public void test_construction_withEndsComputeConstructor_startPositionIsFirstColumnAndLine_textIsFOOBAR() {
        // prepare
        LinearPositionMarker start = LinearPositionMarker
                .createMarkerForFirstLineAndColumn();
        StringBuilder text = new StringBuilder("foobar");

        // execute
        RobotToken token = new RobotToken(start, text);

        // verify
        assertThat(token.getStartPosition()).isEqualTo(start);
        assertThat(token.getText()).isEqualTo(text);
        LinearPositionMarker endPosition = token.getEndPosition();
        assertThat(endPosition).isNotNull();
        assertThat(endPosition.getLine()).isEqualTo(start.getLine());
        assertThat(endPosition.getColumn()).isEqualTo(
                start.getColumn() + text.length());
        assertThat(token.getType()).isEqualTo(RobotSingleCharTokenType.UNKNOWN);
    }


    @Test
    public void test_construction_withSetEndsOfToken_startPositionIsFirstColumnAndLine_textIsEmpty() {
        // prepare
        LinearPositionMarker start = LinearPositionMarker
                .createMarkerForFirstLineAndColumn();
        StringBuilder text = new StringBuilder();
        LinearPositionMarker end = LinearPositionMarker
                .createMarkerForFirstLine(6);

        // execute
        RobotToken token = new RobotToken(start, text, end);

        // verify
        assertThat(token.getStartPosition()).isEqualTo(start);
        assertThat(token.getText()).isEqualTo(text);
        assertThat(token.getEndPosition()).isEqualTo(end);
        assertThat(token.getType()).isEqualTo(RobotSingleCharTokenType.UNKNOWN);
    }


    @Test
    public void test_setTypeOfToken_textIsCarriageReturn() {
        // prepare
        LinearPositionMarker start = LinearPositionMarker
                .createMarkerForFirstLineAndColumn();
        StringBuilder text = new StringBuilder('\r');

        // execute
        RobotToken token = new RobotToken(start, text);
        token.setType(RobotSingleCharTokenType.END_OF_LINE);

        // verify
        assertThat(token.getStartPosition()).isEqualTo(start);
        assertThat(token.getText()).isEqualTo(text);
        LinearPositionMarker endPosition = token.getEndPosition();
        assertThat(endPosition).isNotNull();
        assertThat(endPosition.getLine()).isEqualTo(start.getLine());
        assertThat(endPosition.getColumn()).isEqualTo(
                start.getColumn() + text.length());
        assertThat(token.getType()).isEqualTo(RobotSingleCharTokenType.END_OF_LINE);
    }
}

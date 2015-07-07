package org.robotframework.ide.core.testData.text.lexer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see FilePosition
 */
public class LinearPositionMarkerTest {

    @Test
    public void test_construction_checkForCustomLineAndColumn() {
        // prepare & execute
        int line = 5;
        int column = 6;
        FilePosition lineMarker = new FilePosition(line, column);

        // verify
        assertThat(lineMarker.getLine()).isEqualTo(line);
        assertThat(lineMarker.getColumn()).isEqualTo(column);
    }


    @Test
    public void test_createMarkerForFirstLineAndColumn_shouldReturn_lineNumberONE_columnNumberONE() {
        // prepare & execute
        FilePosition lineMarker = FilePosition
                .createMarkerForFirstLineAndColumn();

        // verify
        int line = lineMarker.getLine();
        int column = lineMarker.getColumn();
        assertThat(line).isEqualTo(1);
        assertThat(line).isEqualTo(FilePosition.THE_FIRST_LINE);
        assertThat(column).isEqualTo(1);
        assertThat(column).isEqualTo(FilePosition.THE_FIRST_COLUMN);
    }


    @Test
    public void test_createMarkerForFirstLine_shouldReturn_lineNumberONE() {
        // prepare & execute
        int column = 3;
        FilePosition lineMarker = FilePosition
                .createMarkerForFirstLine(column);

        // verify
        assertThat(lineMarker.getLine()).isEqualTo(FilePosition.THE_FIRST_LINE);
        assertThat(lineMarker.getColumn()).isEqualTo(column);
    }


    @Test
    public void test_createMarkerForFirstColumn_shouldReturn_columnNumberONE() {
        // prepare & execute
        int line = 3;
        FilePosition lineMarker = FilePosition
                .createMarkerForFirstColumn(line);

        // verify
        assertThat(lineMarker.getLine()).isEqualTo(line);
        assertThat(lineMarker.getColumn()).isEqualTo(FilePosition.THE_FIRST_COLUMN);
    }
}

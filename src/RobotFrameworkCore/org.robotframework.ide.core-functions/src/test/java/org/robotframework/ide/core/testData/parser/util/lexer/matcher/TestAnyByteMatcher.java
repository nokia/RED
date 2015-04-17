package org.robotframework.ide.core.testData.parser.util.lexer.matcher;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.parser.util.lexer.DataMarked;
import org.robotframework.ide.core.testData.parser.util.lexer.IMatcher;
import org.robotframework.ide.core.testData.parser.util.lexer.MatchResult;
import org.robotframework.ide.core.testData.parser.util.lexer.MatchResult.MatchStatus;
import org.robotframework.ide.core.testData.parser.util.lexer.Position;


/**
 * 
 * @author wypych
 * @see AnyByteMatcher
 */
public class TestAnyByteMatcher {

    private IMatcher matcher;


    @Test
    public void test_checkOnAllPossibleBytes__shouldReturn__MatchResult_FOUND() {
        // prepare
        byte[] data = create256ElementsTable();

        int startPos = -1;
        int endPos = 255;

        for (int i = 0; i < 256; i++) {
            startPos++;
            Position position = new Position(startPos, endPos);
            DataMarked marker = new DataMarked(data, position);

            // execute
            MatchResult matchResult = matcher.match(marker);

            // verify
            assertThat(matchResult).isNotNull();
            assertThat(matchResult.getMatcher()).isEqualTo(matcher);
            assertThat(matchResult.getMessages()).isEmpty();
            assertThat(matchResult.getParent()).isNull();
            assertThat(matchResult.getPosition()).isEqualTo(new Position(i, i));
            assertThat(matchResult.getStatus()).isEqualTo(MatchStatus.FOUND);
            assertThat(matchResult.getSubResults()).isEmpty();
        }
    }


    private byte[] create256ElementsTable() {
        byte[] data = new byte[256];
        for (int b = 0; b < 256; b++) {
            data[b] = (byte) b;
        }

        return data;
    }


    @Test
    public void test_checkIfNotExpectedCharIsSupported__shouldReturn__MatchResult_FOUND() {
        // prepare
        byte[] data = new byte[] { 0, 1 };
        int startPos = 0;
        int endPos = 1;

        Position position = new Position(startPos, endPos);
        DataMarked marker = new DataMarked(data, position);

        // execute
        MatchResult matchResult = matcher.match(marker);

        // verify
        assertThat(matchResult).isNotNull();
        assertThat(matchResult.getMatcher()).isEqualTo(matcher);
        assertThat(matchResult.getMessages()).isEmpty();
        assertThat(matchResult.getParent()).isNull();
        assertThat(matchResult.getPosition()).isEqualTo(new Position(0, 0));
        assertThat(matchResult.getStatus()).isEqualTo(MatchStatus.FOUND);
        assertThat(matchResult.getSubResults()).isEmpty();
    }


    @Before
    public void setUp() {
        matcher = new AnyByteMatcher();
    }


    @After
    public void tearDown() {
        matcher = null;
    }
}

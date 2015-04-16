package org.robotframework.ide.core.testData.parser.util.lexer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.robotframework.ide.core.testData.parser.util.lexer.MatchResult.MatchStatus;


/**
 * 
 * @author wypych
 * @see MatchResult#addSubResult(IMatcher,
 *      org.robotframework.ide.core.testData.parser.util.lexer.MatchResult.MatchStatus)
 * 
 */
public class TestMatchResult {

    @Test
    public void test_hasParent_hasSubMatchResults_shouldReturn_TRUE() {
        // prepare
        MatchResult parentResult = new MatchResult(null, MatchStatus.FOUND);
        MatchResult childResult = new MatchResult(null, MatchStatus.FOUND);

        // execute
        parentResult.addSubResult(childResult);

        boolean hasGrandParent = parentResult.hasParent();
        MatchResult grandParent = parentResult.getParent();
        boolean hasParent = childResult.hasParent();
        MatchResult parent = childResult.getParent();

        // verify
        assertThat(hasGrandParent).isFalse();
        assertThat(grandParent).isNull();
        assertThat(hasParent).isTrue();
        assertThat(parent).isEqualTo(parentResult);
    }


    @Test
    public void test_hasParent_notSubMatchResult_shouldReturn_FALSE() {
        // prepare
        MatchResult matchResult = new MatchResult(null, MatchStatus.FOUND);

        // execute
        boolean isChild = matchResult.hasParent();
        MatchResult grandParentResult = matchResult.getParent();

        // verify
        assertThat(isChild).isFalse();
        assertThat(grandParentResult).isNull();
    }
}

/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;

public class RunningKeywordTest {

    @Test
    public void copyingConstructorTest_1() {
        final RunningKeyword keyword = new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL);
        final RunningKeyword copy = new RunningKeyword(keyword, KeywordCallType.SETUP);

        assertThat(copy).isNotSameAs(keyword);
        assertThat(copy.getName()).isEqualTo("kw");
        assertThat(copy.getSourceName()).isEqualTo("lib");
        assertThat(copy.getType()).isEqualTo(KeywordCallType.SETUP);
    }

    @Test
    public void copyingConstructorTest_2() {
        final RunningKeyword keyword = new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN);
        final RunningKeyword copy = new RunningKeyword(keyword);
        
        assertThat(copy).isNotSameAs(keyword);
        assertThat(copy.getName()).isEqualTo("kw");
        assertThat(copy.getSourceName()).isEqualTo("lib");
        assertThat(copy.getType()).isEqualTo(KeywordCallType.TEARDOWN);
    }

    @Test
    public void gettersTests() {
        final RunningKeyword keyword = new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL);

        assertThat(keyword.getName()).isEqualTo("kw");
        assertThat(keyword.getSourceName()).isEqualTo("lib");
        assertThat(keyword.getType()).isEqualTo(KeywordCallType.NORMAL_CALL);
        assertThat(keyword.asCall()).isEqualTo(QualifiedKeywordName.asCall("kw", "lib"));

        assertThat(new RunningKeyword("_", "_", KeywordCallType.NORMAL_CALL).isSetup()).isFalse();
        assertThat(new RunningKeyword("_", "_", KeywordCallType.NORMAL_CALL).isTeardown()).isFalse();
        assertThat(new RunningKeyword("_", "_", KeywordCallType.NORMAL_CALL).isForLoop()).isFalse();
        assertThat(new RunningKeyword("_", "_", KeywordCallType.NORMAL_CALL).isForIteration()).isFalse();

        assertThat(new RunningKeyword("_", "_", KeywordCallType.SETUP).isSetup()).isTrue();
        assertThat(new RunningKeyword("_", "_", KeywordCallType.SETUP).isTeardown()).isFalse();
        assertThat(new RunningKeyword("_", "_", KeywordCallType.SETUP).isForLoop()).isFalse();
        assertThat(new RunningKeyword("_", "_", KeywordCallType.SETUP).isForIteration()).isFalse();

        assertThat(new RunningKeyword("_", "_", KeywordCallType.TEARDOWN).isSetup()).isFalse();
        assertThat(new RunningKeyword("_", "_", KeywordCallType.TEARDOWN).isTeardown()).isTrue();
        assertThat(new RunningKeyword("_", "_", KeywordCallType.TEARDOWN).isForLoop()).isFalse();
        assertThat(new RunningKeyword("_", "_", KeywordCallType.TEARDOWN).isForIteration()).isFalse();

        assertThat(new RunningKeyword("_", "_", KeywordCallType.FOR).isSetup()).isFalse();
        assertThat(new RunningKeyword("_", "_", KeywordCallType.FOR).isTeardown()).isFalse();
        assertThat(new RunningKeyword("_", "_", KeywordCallType.FOR).isForLoop()).isTrue();
        assertThat(new RunningKeyword("_", "_", KeywordCallType.FOR).isForIteration()).isFalse();

        assertThat(new RunningKeyword("_", "_", KeywordCallType.FOR_ITERATION).isSetup()).isFalse();
        assertThat(new RunningKeyword("_", "_", KeywordCallType.FOR_ITERATION).isTeardown()).isFalse();
        assertThat(new RunningKeyword("_", "_", KeywordCallType.FOR_ITERATION).isForLoop()).isFalse();
        assertThat(new RunningKeyword("_", "_", KeywordCallType.FOR_ITERATION).isForIteration()).isTrue();
    }

    @Test
    public void equalsTests() {
        assertThat(new RunningKeyword("lib", "kw", KeywordCallType.SETUP))
                .isEqualTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP));

        assertThat(new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL))
                .isNotEqualTo(new RunningKeyword("lib1", "kw", KeywordCallType.NORMAL_CALL));
        assertThat(new RunningKeyword("lib1", "kw", KeywordCallType.NORMAL_CALL))
                .isNotEqualTo(new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL));
        assertThat(new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL))
                .isNotEqualTo(new RunningKeyword("lib", "kw1", KeywordCallType.NORMAL_CALL));
        assertThat(new RunningKeyword("lib", "kw1", KeywordCallType.NORMAL_CALL))
                .isNotEqualTo(new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL));
        assertThat(new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL))
                .isNotEqualTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP));
        assertThat(new RunningKeyword("lib", "kw", KeywordCallType.SETUP))
                .isNotEqualTo(new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL));

        assertThat(new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL)).isNotEqualTo(new Object());
        assertThat(new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL)).isNotEqualTo(null);
    }

    @Test
    public void hashCodeTests() {
        assertThat(new RunningKeyword("lib", "kw", KeywordCallType.SETUP).hashCode())
                .isEqualTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP).hashCode());
    }

    @Test
    public void stringRepresentationTests() {
        assertThat(new RunningKeyword("lib", "kw", KeywordCallType.NORMAL_CALL).toString())
                .isEqualTo("NORMAL_CALL: lib.kw");
        assertThat(new RunningKeyword("lib", "kw", KeywordCallType.FOR).toString())
                .isEqualTo("FOR: lib.kw");
        assertThat(new RunningKeyword("lib", "kw", KeywordCallType.FOR_ITERATION).toString())
                .isEqualTo("FOR_ITERATION: lib.kw");
        assertThat(new RunningKeyword("lib", "kw", KeywordCallType.SETUP).toString())
                .isEqualTo("SETUP: lib.kw");
        assertThat(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN).toString())
                .isEqualTo("TEARDOWN: lib.kw");

        assertThat(new RunningKeyword(null, "kw", KeywordCallType.NORMAL_CALL).toString())
                .isEqualTo("NORMAL_CALL: null.kw");
        assertThat(new RunningKeyword("lib", null, KeywordCallType.NORMAL_CALL).toString())
                .isEqualTo("NORMAL_CALL: lib.null");
        assertThat(new RunningKeyword(null, null, KeywordCallType.NORMAL_CALL).toString())
                .isEqualTo("NORMAL_CALL: null.null");
    }
}

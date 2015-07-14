package org.robotframework.ide.core.testData.model.table.settings.test;

import org.robotframework.ide.core.testData.model.table.settings.AKeywordBaseSetting;

public class TestTeardown extends AKeywordBaseSetting {

    private final TestTeardownDeclaration testTeardownWord;


    public TestTeardown(final TestTeardownDeclaration testTeardownWord) {
        this.testTeardownWord = testTeardownWord;
    }
}

package org.robotframework.ide.core.testData.model.table.settings.test;

import org.robotframework.ide.core.testData.model.table.settings.AKeywordBaseSetting;

public class TestPrecondition extends AKeywordBaseSetting {

    private final TestPreconditionDeclaration testPreconditionWord;


    public TestPrecondition(
            final TestPreconditionDeclaration testPreconditionWord) {
        this.testPreconditionWord = testPreconditionWord;
    }
}

package org.robotframework.ide.core.testData.model.table.settings.test;

import org.robotframework.ide.core.testData.model.table.settings.AKeywordBaseSetting;

public class TestPostcondition extends AKeywordBaseSetting {

    private final TestPostconditionDeclaration testPostconditionWord;


    public TestPostcondition(
            final TestPostconditionDeclaration testPostconditionWord) {
        this.testPostconditionWord = testPostconditionWord;
    }
}

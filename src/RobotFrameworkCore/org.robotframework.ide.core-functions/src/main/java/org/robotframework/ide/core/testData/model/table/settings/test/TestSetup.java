package org.robotframework.ide.core.testData.model.table.settings.test;

import org.robotframework.ide.core.testData.model.table.settings.AKeywordBaseSetting;

public class TestSetup extends AKeywordBaseSetting {

    private final TestSetupDeclaration testSetupWord;


    public TestSetup(final TestSetupDeclaration testSetupWord) {
        this.testSetupWord = testSetupWord;
    }
}

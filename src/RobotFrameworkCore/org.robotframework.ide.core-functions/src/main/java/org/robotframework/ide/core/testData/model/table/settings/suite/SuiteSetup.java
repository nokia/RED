package org.robotframework.ide.core.testData.model.table.settings.suite;

import org.robotframework.ide.core.testData.model.table.settings.AKeywordBaseSetting;

public class SuiteSetup extends AKeywordBaseSetting {

    private final SuiteSetupDeclaration suiteSetupWord;


    public SuiteSetup(final SuiteSetupDeclaration suiteSetupWord) {
        this.suiteSetupWord = suiteSetupWord;
    }
}

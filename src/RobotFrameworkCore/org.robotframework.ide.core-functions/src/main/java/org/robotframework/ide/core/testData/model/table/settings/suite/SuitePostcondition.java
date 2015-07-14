package org.robotframework.ide.core.testData.model.table.settings.suite;

import org.robotframework.ide.core.testData.model.table.settings.AKeywordBaseSetting;

public class SuitePostcondition extends AKeywordBaseSetting {

    private final SuitePostconditionDeclaration suitePostconditionWord;


    public SuitePostcondition(
            final SuitePostconditionDeclaration suitePostconditionWord) {
        this.suitePostconditionWord = suitePostconditionWord;
    }
}

package org.robotframework.ide.core.testData.model.table.settings.suite;

import org.robotframework.ide.core.testData.model.table.settings.AKeywordBaseSetting;

public class SuiteTeardown extends AKeywordBaseSetting {

    private final SuiteTeardownDeclaration suiteTeardownWord;


    public SuiteTeardown(final SuiteTeardownDeclaration suiteTeardownWord) {
        this.suiteTeardownWord = suiteTeardownWord;
    }
}

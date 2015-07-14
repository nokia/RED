package org.robotframework.ide.core.testData.model.table.settings.suite;

import org.robotframework.ide.core.testData.model.table.settings.AKeywordBaseSetting;

public class SuitePrecondition extends AKeywordBaseSetting {

    private final SuitePreconditionDeclaration suitePreconditionWord;


    public SuitePrecondition(
            final SuitePreconditionDeclaration suitePreconditionWord) {
        this.suitePreconditionWord = suitePreconditionWord;
    }
}

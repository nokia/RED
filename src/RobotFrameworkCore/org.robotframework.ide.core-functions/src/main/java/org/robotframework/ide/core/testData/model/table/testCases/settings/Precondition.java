package org.robotframework.ide.core.testData.model.table.testCases.settings;

import org.robotframework.ide.core.testData.model.table.settings.AKeywordBaseSetting;


public class Precondition extends AKeywordBaseSetting {

    private final PreconditionDeclaration preconditionWord;


    public Precondition(final PreconditionDeclaration preconditionWord) {
        this.preconditionWord = preconditionWord;
    }
}

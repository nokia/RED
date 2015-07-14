package org.robotframework.ide.core.testData.model.table.testCases.settings;

import org.robotframework.ide.core.testData.model.table.settings.AKeywordBaseSetting;


public class Postcondition extends AKeywordBaseSetting {

    private final PostconditionDeclaration postconditionWord;


    public Postcondition(final PostconditionDeclaration postconditionWord) {
        this.postconditionWord = postconditionWord;
    }
}

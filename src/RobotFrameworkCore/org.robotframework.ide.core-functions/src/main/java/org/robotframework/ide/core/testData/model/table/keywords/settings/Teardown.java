package org.robotframework.ide.core.testData.model.table.keywords.settings;

import org.robotframework.ide.core.testData.model.table.settings.AKeywordBaseSetting;

public class Teardown extends AKeywordBaseSetting {

    private final TeardownDeclaration teardownWord;


    public Teardown(final TeardownDeclaration teardownWord) {
        this.teardownWord = teardownWord;
    }
}

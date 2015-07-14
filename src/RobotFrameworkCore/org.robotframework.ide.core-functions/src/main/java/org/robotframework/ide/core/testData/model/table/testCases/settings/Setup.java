package org.robotframework.ide.core.testData.model.table.testCases.settings;

import org.robotframework.ide.core.testData.model.table.settings.AKeywordBaseSetting;


public class Setup extends AKeywordBaseSetting {

    private final SetupDeclaration setupWord;


    public Setup(final SetupDeclaration setupWord) {
        this.setupWord = setupWord;
    }
}

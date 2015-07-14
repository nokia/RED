package org.robotframework.ide.core.testData.model.table.settings.test;

import org.robotframework.ide.core.testData.model.common.Comment;
import org.robotframework.ide.core.testData.model.common.KeywordProvider;
import org.robotframework.ide.core.testData.model.common.KeywordUsage;


public class TestTemplate {

    private final TestTemplateDeclaration testTemplateWord;
    private KeywordProvider libraryName;
    private KeywordUsage keyword;
    private Comment comment;


    public TestTemplate(final TestTemplateDeclaration testTemplateWord) {
        this.testTemplateWord = testTemplateWord;
    }
}

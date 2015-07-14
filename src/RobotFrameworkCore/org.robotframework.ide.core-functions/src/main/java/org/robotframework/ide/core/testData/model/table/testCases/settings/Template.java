package org.robotframework.ide.core.testData.model.table.testCases.settings;

import org.robotframework.ide.core.testData.model.common.Comment;
import org.robotframework.ide.core.testData.model.common.KeywordProvider;
import org.robotframework.ide.core.testData.model.common.KeywordUsage;


public class Template {

    private final TemplateDeclaration templateWord;
    private KeywordProvider libraryName;
    private KeywordUsage keyword;
    private Comment comment;


    public Template(final TemplateDeclaration templateWord) {
        this.templateWord = templateWord;
    }
}

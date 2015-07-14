package org.robotframework.ide.core.testData.model.table.testCases.settings;

import org.robotframework.ide.core.testData.model.common.Text;
import org.robotframework.ide.core.testData.model.table.settings.doc.DocumentationDeclaration;


public class Documentation {

    private final DocumentationDeclaration documentationOrContinoueWord;
    private Text text;


    public Documentation(final DocumentationDeclaration docOrContinueWord) {
        this.documentationOrContinoueWord = docOrContinueWord;
    }
}

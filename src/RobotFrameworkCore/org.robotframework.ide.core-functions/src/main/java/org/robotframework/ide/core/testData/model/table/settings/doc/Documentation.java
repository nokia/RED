package org.robotframework.ide.core.testData.model.table.settings.doc;

import org.robotframework.ide.core.testData.model.common.Text;

public class Documentation {

    private final DocumentationDeclaration documentationOrContinoueWord;
    private Text text;


    public Documentation(final DocumentationDeclaration docOrContinueWord) {
        this.documentationOrContinoueWord = docOrContinueWord;
    }
}

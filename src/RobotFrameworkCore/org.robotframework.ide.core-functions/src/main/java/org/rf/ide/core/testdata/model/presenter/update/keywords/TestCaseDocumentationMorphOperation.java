/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.keywords;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ExecutablesStepsHolderMorphOperation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordDocumentation;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestDocumentation;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public class TestCaseDocumentationMorphOperation extends ExecutablesStepsHolderMorphOperation<UserKeyword> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.TEST_CASE_DOCUMENTATION;
    }

    @Override
    public AModelElement<?> insert(final UserKeyword keyword, final int index,
            final AModelElement<?> modelElement) {
        final TestDocumentation caseDocumentation = (TestDocumentation) modelElement;

        final KeywordDocumentation kwDocumentation = keyword.newDocumentation();
        kwDocumentation.getDeclaration().setText(caseDocumentation.getDeclaration().getText());
        
        for (final RobotToken txt : caseDocumentation.getDocumentationText()) {
            kwDocumentation.addDocumentationText(txt);
        }
        for (final RobotToken comment : caseDocumentation.getComment()) {
            kwDocumentation.addCommentPart(comment);
        }
        return kwDocumentation;
    }

}

/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.keywords;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.keywords.KeywordDocumentation;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ADocumentDeprecatedDeclarationValidator;

public class DocumentationUserKeywordDeclarationSettingValidator extends ADocumentDeprecatedDeclarationValidator {

    private final RobotKeywordsSection section;

    public DocumentationUserKeywordDeclarationSettingValidator(final IFile file, final RobotKeywordsSection section,
            final ValidationReportingStrategy reporter) {
        super(file, reporter);
        this.section = section;
    }

    @Override
    public IProblemCause getSettingProblemId() {
        return KeywordsProblem.DOCUMENT_SYNONYM;
    }

    @Override
    public List<RobotToken> getDocumentationDeclaration() {
        final List<RobotToken> documentationDec = new ArrayList<>(0);
        final KeywordTable keywordTable = section.getLinkedElement();
        if (keywordTable.isPresent()) {
            final List<UserKeyword> keywords = keywordTable.getKeywords();
            for (final UserKeyword keyword : keywords) {
                final List<KeywordDocumentation> documentation = keyword.getDocumentation();
                for (final KeywordDocumentation keywordDocumentation : documentation) {
                    documentationDec.add(keywordDocumentation.getDeclaration());
                }
            }
        }

        return documentationDec;
    }
}

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
import org.rf.ide.core.testdata.model.table.keywords.KeywordTeardown;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.KeywordsProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ADeprecatedSettingElement;

public class PostconditionDeclarationExistanceValidator extends ADeprecatedSettingElement {

    private final RobotKeywordsSection section;

    public PostconditionDeclarationExistanceValidator(final IFile file, final ProblemsReportingStrategy reporter,
            final RobotKeywordsSection section) {
        super(file, reporter, "Postcondition");
        this.section = section;
    }

    @Override
    public IProblemCause getProblemId() {
        return KeywordsProblem.POSTCONDITION_SYNONIM;
    }

    @Override
    public List<RobotToken> getDeclaration() {
        final List<RobotToken> declarations = new ArrayList<>(0);
        final KeywordTable keywordTable = (KeywordTable) section.getLinkedElement();
        if (keywordTable.isPresent()) {
            for (final UserKeyword keyword : keywordTable.getKeywords()) {
                for (final KeywordTeardown teardown : keyword.getTeardowns()) {
                    declarations.add(teardown.getDeclaration());
                }
            }
        }

        return declarations;
    }
}

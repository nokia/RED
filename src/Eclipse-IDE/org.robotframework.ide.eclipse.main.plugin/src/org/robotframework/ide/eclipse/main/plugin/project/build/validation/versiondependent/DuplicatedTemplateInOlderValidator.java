/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import static com.google.common.collect.Iterables.transform;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Range;

/**
 * @author Michal Anglart
 *
 */
public class DuplicatedTemplateInOlderValidator extends VersionDependentModelUnitValidator {

    private final IFile file;
    private final RobotSettingsSection section;
    private final ProblemsReportingStrategy reporter;

    public DuplicatedTemplateInOlderValidator(final IFile file, final RobotSettingsSection section) {
        this.file = file;
        this.section = section;
        this.reporter = new ProblemsReportingStrategy();
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.lessThan(new RobotVersion(3, 0));
    }

    @Override
    public void validate(final IProgressMonitor monitor) throws CoreException {
        final SettingTable table = (SettingTable) section.getLinkedElement();
        final List<TestTemplate> templates = table.getTestTemplates();

        final String keywordinUse = Joiner.on(' ')
                .skipNulls()
                .join(transform(templates, new Function<TestTemplate, String>() {

                    @Override
                    public String apply(final TestTemplate template) {
                        return template.getKeywordName() == null ? null : template.getKeywordName().getText();
                    }
                }));

        if (templates.size() > 1) {
            for (int i = 1; i < templates.size(); i++) {
                final RobotToken token = templates.get(i).getDeclaration();
                reporter.handleProblem(RobotProblem.causedBy(GeneralSettingsProblem.DUPLICATED_TEMPLATE_28)
                        .formatMessageWith(token.getText(), keywordinUse), file, token);
            }
        }
    }
}

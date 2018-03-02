/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.setting;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;

import com.google.common.base.Function;
import com.google.common.collect.Range;

public class DuplicatedTemplateInOlderValidator extends ADuplicatedInOldValidator<TestTemplate> {

    public DuplicatedTemplateInOlderValidator(final IFile file, final RobotSettingsSection section,
            final ValidationReportingStrategy reporter) {
        super(file, section, reporter);
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.lessThan(new RobotVersion(3, 0));
    }

    @Override
    protected List<TestTemplate> getElements() {
        return section.getLinkedElement().getTestTemplates();
    }

    @Override
    protected Function<TestTemplate, String> getImportantElement() {
        return new Function<TestTemplate, String>() {

            @Override
            public String apply(final TestTemplate template) {
                return template.getKeywordName() == null ? null : template.getKeywordName().getText();
            }
        };
    }

    @Override
    protected GeneralSettingsProblem getSettingProblemId() {
        return GeneralSettingsProblem.DUPLICATED_TEMPLATE_28;
    }
}

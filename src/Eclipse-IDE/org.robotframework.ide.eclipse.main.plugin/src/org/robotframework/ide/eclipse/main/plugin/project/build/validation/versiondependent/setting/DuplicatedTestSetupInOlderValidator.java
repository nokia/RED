/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.setting;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.setting.TestSetup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;

import com.google.common.base.Function;
import com.google.common.collect.Range;

public class DuplicatedTestSetupInOlderValidator extends ADuplicatedInOldValidator<TestSetup> {

    public DuplicatedTestSetupInOlderValidator(final IFile file, final RobotSettingsSection section,
            final ValidationReportingStrategy reporter) {
        super(file, section, reporter);
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.lessThan(new RobotVersion(3, 0));
    }

    @Override
    protected List<TestSetup> getElements() {
        return section.getLinkedElement().getTestSetups();
    }

    @Override
    protected Function<TestSetup, String> getImportantElement() {
        return new Function<TestSetup, String>() {

            @Override
            public String apply(final TestSetup setup) {
                return setup.getKeywordName() == null ? null : setup.getKeywordName().getText();
            }
        };
    }

    @Override
    protected GeneralSettingsProblem getSettingProblemId() {
        return GeneralSettingsProblem.DUPLICATED_TEST_SETUP_28;
    }
}

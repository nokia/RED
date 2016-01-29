/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;

import com.google.common.base.Function;
import com.google.common.collect.Range;

public class DuplicatedTestTimeoutInOlderValidator extends ADuplicatedInOldValidator<TestTimeout> {

    public DuplicatedTestTimeoutInOlderValidator(final IFile file, final RobotSettingsSection section,
            final ProblemsReportingStrategy reporter) {
        super(file, section, reporter);
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.lessThan(new RobotVersion(3, 0));
    }

    @Override
    protected List<TestTimeout> getElements() {
        final SettingTable table = (SettingTable) section.getLinkedElement();
        return table.getTestTimeouts();
    }

    @Override
    protected Function<TestTimeout, String> getImportantElement() {
        return new Function<TestTimeout, String>() {

            @Override
            public String apply(final TestTimeout timeout) {
                return timeout.getTimeout() == null ? null : timeout.getTimeout().getText();
            }
        };
    }

    @Override
    protected GeneralSettingsProblem getSettingProblemId() {
        return GeneralSettingsProblem.DUPLICATED_SUITE_TEARDOWN_28;
    }
}

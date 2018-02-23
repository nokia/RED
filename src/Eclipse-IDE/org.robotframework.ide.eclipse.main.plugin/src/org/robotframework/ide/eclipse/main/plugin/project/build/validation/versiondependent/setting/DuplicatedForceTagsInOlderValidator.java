/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.versiondependent.setting;

import static com.google.common.collect.Lists.transform;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.setting.ForceTags;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.GeneralSettingsProblem;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Range;

public class DuplicatedForceTagsInOlderValidator extends ADuplicatedInOldValidator<ForceTags> {

    public DuplicatedForceTagsInOlderValidator(final IFile file, final RobotSettingsSection section,
            final ValidationReportingStrategy reporter) {
        super(file, section, reporter);
    }

    @Override
    protected Range<RobotVersion> getApplicableVersionRange() {
        return Range.lessThan(new RobotVersion(3, 0));
    }

    @Override
    protected List<ForceTags> getElements() {
        return section.getLinkedElement().getForceTags();
    }

    @Override
    protected Function<ForceTags, String> getImportantElement() {
        return new Function<ForceTags, String>() {

            @Override
            public String apply(final ForceTags tags) {
                return tags.getTags() == null ? null
                        : Joiner.on(' ').skipNulls().join(transform(tags.getTags(), new Function<RobotToken, String>() {

                    @Override
                    public String apply(final RobotToken tag) {
                        return tag.getText() == null ? null : tag.getText();
                    }

                }));
            }
        };
    }

    @Override
    protected GeneralSettingsProblem getSettingProblemId() {
        return GeneralSettingsProblem.DUPLICATED_FORCE_TAGS_28;
    }
}

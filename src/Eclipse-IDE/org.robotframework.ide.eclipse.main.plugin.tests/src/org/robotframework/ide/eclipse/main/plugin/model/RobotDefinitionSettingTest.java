/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;

public class RobotDefinitionSettingTest {

    @Test
    public void definitionSettingShouldBeCommented_whenNotCommented_andViceVersa() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("t1")
                .appendLine("  [Documentation]")
                .build();
        final List<RobotDefinitionSetting> settings = model.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .stream()
                .filter(RobotDefinitionSetting.class::isInstance)
                .map(RobotDefinitionSetting.class::cast)
                .collect(Collectors.toList());
        assertThat(settings).hasSize(1);
        assertThat(settings.get(0).getName()).isEqualTo("Documentation");
        assertThat(settings.get(0).getComment()).isEmpty();
        assertThat(settings.get(0).shouldAddCommentMark()).isTrue();
    }

}

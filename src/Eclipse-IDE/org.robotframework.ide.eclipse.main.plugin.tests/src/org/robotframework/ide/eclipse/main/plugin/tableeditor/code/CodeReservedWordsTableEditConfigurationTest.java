/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.assist.RedSettingProposals.SettingTarget;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableConfigurationLabels;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;
import org.robotframework.red.nattable.edit.RedTextCellEditor;


@ExtendWith(ProjectExtension.class)
public class CodeReservedWordsTableEditConfigurationTest {

    @Project
    static IProject project;

    private RobotModel robotModel;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        createFile(project, "suite.robot", "*** Test Cases ***");
    }

    @BeforeEach
    public void beforeTest() {
        robotModel = new RobotModel();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void configurationCheck() {
        final IConfigRegistry configRegistry = mock(IConfigRegistry.class);

        final RobotSuiteFile model = robotModel.createSuiteFile(getFile(project, "suite.robot"));

        final CodeReservedWordsTableEditConfiguration configuration = new CodeReservedWordsTableEditConfiguration(model,
                mock(IRowDataProvider.class), SettingTarget.TEST_CASE, true);
        configuration.configureRegistry(configRegistry);

        verify(configRegistry, times(1)).registerConfigAttribute(isA(ConfigAttribute.class),
                isA(RedTextCellEditor.class), eq(DisplayMode.NORMAL), eq(TableConfigurationLabels.ASSIST_REQUIRED));
    }
}

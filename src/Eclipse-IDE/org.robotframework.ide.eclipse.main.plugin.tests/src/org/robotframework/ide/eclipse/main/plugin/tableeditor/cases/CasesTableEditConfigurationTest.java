/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableConfigurationLabels;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.nattable.edit.RedTextCellEditor;

public class CasesTableEditConfigurationTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(CasesTableEditConfigurationTest.class);

    private static RobotModel robotModel;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        robotModel = new RobotModel();

        projectProvider.createFile("suite.robot",
                "*** Test Cases ***");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void configurationCheck() {
        final IConfigRegistry configRegistry = mock(IConfigRegistry.class);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));

        final CasesTableEditConfiguration configuration = new CasesTableEditConfiguration(
                model, mock(IRowDataProvider.class), true);
        configuration.configureRegistry(configRegistry);

        verify(configRegistry, times(1)).registerConfigAttribute(isA(ConfigAttribute.class),
                isA(RedTextCellEditor.class), eq(DisplayMode.NORMAL),
                eq(TableConfigurationLabels.ASSIST_REQUIRED));
    }
}

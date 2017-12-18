/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.library.Libraries;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.ImportsProposalsProvider.LibrariesProposalsProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.ImportsProposalsProvider.ResourceFileLocationsProposalsProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.ImportsProposalsProvider.VariableFileLocationsProposalsProvider;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ShellProvider;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

public class ImportsProposalsProviderTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(
            ImportsProposalsProviderTest.class);

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("suite.robot", "*** Test Cases ***");
        projectProvider.createFile("a_res.robot");
        projectProvider.createFile("b_res.robot");
        projectProvider.createFile("a_vars.py");
        projectProvider.createFile("b_vars.py");
    }

    @Test
    public void thereAreNoResourcesProposalsProvided_whenNothingMatchesToCurrentInput() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(projectProvider.getFile("suite.robot"));

        final RobotSetting setting = new RobotSetting(null, SettingsGroup.RESOURCES,
                new ResourceImport(RobotToken.create("Resource")));

        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(setting);

        final ResourceFileLocationsProposalsProvider provider = new ResourceFileLocationsProposalsProvider(model,
                dataProvider);

        final AssistantContext context = new NatTableAssistantContext(1, 0);
        final RedContentProposal[] proposals = provider.getProposals("anything", 2, context);
        assertThat(proposals).isEmpty();
    }

    @Test
    public void thereAreNoResourcesProposalsProvided_whenInAnyColumnNotAResourceSetting() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(projectProvider.getFile("suite.robot"));

        final RobotSetting setting = new RobotSetting(null, SettingsGroup.VARIABLES,
                new VariablesImport(RobotToken.create("Variables")));

        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(setting);

        final ResourceFileLocationsProposalsProvider provider = new ResourceFileLocationsProposalsProvider(model,
                dataProvider);

        for (int column = 0; column < 10; column++) {
            final AssistantContext context = new NatTableAssistantContext(column, 0);
            final RedContentProposal[] proposals = provider.getProposals("a_bc", 2, context);
            assertThat(proposals).isEmpty();
        }
    }

    @Test
    public void thereAreNoResourcesProposalsProvided_whenInColumnOtherThanFirstOfResourceSetting() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(projectProvider.getFile("suite.robot"));

        final RobotSetting setting = new RobotSetting(null, SettingsGroup.RESOURCES,
                new ResourceImport(RobotToken.create("Resource")));

        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(setting);

        final ResourceFileLocationsProposalsProvider provider = new ResourceFileLocationsProposalsProvider(model,
                dataProvider);

        for (int column = 0; column < 10; column++) {
            if (column == 1) {
                continue;
            }
            final AssistantContext context = new NatTableAssistantContext(column, 0);
            final RedContentProposal[] proposals = provider.getProposals("a_bc", 2, context);
            assertThat(proposals).isEmpty();
        }
    }

    @Test
    public void thereAreResourcesProposalsProvided_whenInFirstColumnOfResourceSetting() {
        final Text text = new Text(shellProvider.getShell(), SWT.SINGLE);
        text.setText("a_bc");

        final RobotSuiteFile model = new RobotModel().createSuiteFile(projectProvider.getFile("suite.robot"));

        final RobotSetting setting = new RobotSetting(null, SettingsGroup.RESOURCES,
                new ResourceImport(RobotToken.create("Resource")));

        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(setting);

        final ResourceFileLocationsProposalsProvider provider = new ResourceFileLocationsProposalsProvider(model, dataProvider);

        final AssistantContext context = new NatTableAssistantContext(1, 0);
        final RedContentProposal[] proposals = provider.getProposals(text.getText(), 2, context);
        assertThat(proposals).hasSize(1);

        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("a_res.robot");
    }

    @Test
    public void thereAreNoVariablesProposalsProvided_whenNothingMatchesToCurrentInput() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(projectProvider.getFile("suite.robot"));

        final RobotSetting setting = new RobotSetting(null, SettingsGroup.VARIABLES,
                new VariablesImport(RobotToken.create("Variables")));

        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(setting);

        final VariableFileLocationsProposalsProvider provider = new VariableFileLocationsProposalsProvider(model,
                dataProvider);

        final AssistantContext context = new NatTableAssistantContext(1, 0);
        final RedContentProposal[] proposals = provider.getProposals("anything", 2, context);
        assertThat(proposals).isEmpty();
    }

    @Test
    public void thereAreNoVariablesProposalsProvided_whenInAnyColumnNotAVariablesSetting() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(projectProvider.getFile("suite.robot"));

        final RobotSetting setting = new RobotSetting(null, SettingsGroup.LIBRARIES,
                new LibraryImport(RobotToken.create("Library")));

        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(setting);

        final VariableFileLocationsProposalsProvider provider = new VariableFileLocationsProposalsProvider(model,
                dataProvider);

        for (int column = 0; column < 10; column++) {
            final AssistantContext context = new NatTableAssistantContext(column, 0);
            final RedContentProposal[] proposals = provider.getProposals("a_bc", 2, context);
            assertThat(proposals).isEmpty();
        }
    }

    @Test
    public void thereAreNoVariablesProposalsProvided_whenInColumnOtherThanFirstOfVariablesSetting() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(projectProvider.getFile("suite.robot"));

        final RobotSetting setting = new RobotSetting(null, SettingsGroup.VARIABLES,
                new VariablesImport(RobotToken.create("Variables")));

        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(setting);

        final VariableFileLocationsProposalsProvider provider = new VariableFileLocationsProposalsProvider(model,
                dataProvider);

        for (int column = 0; column < 10; column++) {
            if (column == 1) {
                continue;
            }
            final AssistantContext context = new NatTableAssistantContext(column, 0);
            final RedContentProposal[] proposals = provider.getProposals("a_bc", 2, context);
            assertThat(proposals).isEmpty();
        }
    }

    @Test
    public void thereAreVariablesProposalsProvided_whenInFirstColumnOfVariablesSetting() {
        final Text text = new Text(shellProvider.getShell(), SWT.SINGLE);
        text.setText("a_bc");

        final RobotSuiteFile model = new RobotModel().createSuiteFile(projectProvider.getFile("suite.robot"));

        final RobotSetting setting = new RobotSetting(null, SettingsGroup.VARIABLES,
                new VariablesImport(RobotToken.create("Variables")));

        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(setting);

        final VariableFileLocationsProposalsProvider provider = new VariableFileLocationsProposalsProvider(model,
                dataProvider);

        final AssistantContext context = new NatTableAssistantContext(1, 0);
        final RedContentProposal[] proposals = provider.getProposals(text.getText(), 2, context);
        assertThat(proposals).hasSize(1);

        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("a_vars.py");
    }

    @Test
    public void thereAreNoLibrariesProposalsProvided_whenNothingMatchesCurrentInput() {
        final RobotModel robotModel = new RobotModel();
        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());

        robotProject.setStandardLibraries(Libraries.createStdLibs("aLib", "bLib"));

        final RobotSetting setting = new RobotSetting(null, SettingsGroup.LIBRARIES,
                new LibraryImport(RobotToken.create("Library")));

        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(setting);

        final LibrariesProposalsProvider provider = new LibrariesProposalsProvider(model, dataProvider);

        final AssistantContext context = new NatTableAssistantContext(1, 0);
        final RedContentProposal[] proposals = provider.getProposals("xyz", 1, context);
        assertThat(proposals).isEmpty();
    }

    @Test
    public void thereAreNoLibrariesProposalsProvided_whenInFirstOfNonLibrarySetting() {
        final RobotModel robotModel = new RobotModel();
        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());

        robotProject.setStandardLibraries(Libraries.createStdLibs("aLib", "bLib"));

        final RobotSetting setting = new RobotSetting(null, SettingsGroup.RESOURCES,
                new ResourceImport(RobotToken.create("Resource")));

        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(setting);

        final LibrariesProposalsProvider provider = new LibrariesProposalsProvider(model, dataProvider);

        final AssistantContext context = new NatTableAssistantContext(1, 0);
        final RedContentProposal[] proposals = provider.getProposals("abc", 1, context);
        assertThat(proposals).isEmpty();
    }

    @Test
    public void thereAreNoLibrariesProposalsProvided_whenInColumnOtherThanFirstOfLibrarySetting() {
        final RobotModel robotModel = new RobotModel();
        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());

        robotProject.setStandardLibraries(Libraries.createStdLibs("aLib", "bLib"));

        final RobotSetting setting = new RobotSetting(null, SettingsGroup.LIBRARIES,
                new LibraryImport(RobotToken.create("Library")));

        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(setting);

        final LibrariesProposalsProvider provider = new LibrariesProposalsProvider(model, dataProvider);

        for (int column = 0; column < 10; column++) {
            if (column == 1) {
                continue;
            }
            final AssistantContext context = new NatTableAssistantContext(column, 0);
            final RedContentProposal[] proposals = provider.getProposals("abc", 1, context);
            assertThat(proposals).isEmpty();
        }
    }

    @Test
    public void thereAreLibrariesAsWellAsFilesProposalsProvided_whenInFirstColumnOfLibrarySetting() {
        final Text text = new Text(shellProvider.getShell(), SWT.SINGLE);
        text.setText("abc");

        final RobotModel robotModel = new RobotModel();
        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());

        robotProject.setStandardLibraries(Libraries.createStdLibs("aLib", "bLib"));

        final RobotSetting setting = new RobotSetting(null, SettingsGroup.LIBRARIES,
                new LibraryImport(RobotToken.create("Library")));

        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(setting);

        final LibrariesProposalsProvider provider = new LibrariesProposalsProvider(model,
                dataProvider);

        final AssistantContext context = new NatTableAssistantContext(1, 0);
        final RedContentProposal[] proposals = provider.getProposals(text.getText(), 1, context);
        assertThat(proposals).hasSize(3);

        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("aLib");

        proposals[1].getModificationStrategy().insert(text, proposals[1]);
        assertThat(text.getText()).isEqualTo("a_vars.py");

        proposals[2].getModificationStrategy().insert(text, proposals[2]);
        assertThat(text.getText()).isEqualTo("b_vars.py");
    }
}

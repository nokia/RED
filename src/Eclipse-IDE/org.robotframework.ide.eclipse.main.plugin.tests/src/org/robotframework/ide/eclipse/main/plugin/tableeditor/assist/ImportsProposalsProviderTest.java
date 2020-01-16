/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.ImportsProposalsProvider.LibrariesProposalsProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.ImportsProposalsProvider.ResourceFileLocationsProposalsProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.ImportsProposalsProvider.VariableFileLocationsProposalsProvider;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.junit.jupiter.FreshShell;
import org.robotframework.red.junit.jupiter.FreshShellExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

@ExtendWith({ ProjectExtension.class, FreshShellExtension.class })
public class ImportsProposalsProviderTest {

    @Project(files = { "a_res.robot", "b_res.robot", "a_vars.py", "b_vars.py" })
    static IProject project;

    @FreshShell
    Shell shell;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        createFile(project, "suite.robot", "*** Test Cases ***");
    }

    @Test
    public void thereAreNoResourcesProposalsProvided_whenNothingMatchesToCurrentInput() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(getFile(project, "suite.robot"));

        final RobotSetting setting = new RobotSetting(null, SettingsGroup.RESOURCES,
                new ResourceImport(RobotToken.create("Resource")));

        final IRowDataProvider<Object> dataProvider = createDataProvider(setting);
        final ResourceFileLocationsProposalsProvider provider = new ResourceFileLocationsProposalsProvider(model,
                dataProvider);

        final AssistantContext context = new NatTableAssistantContext(1, 0);
        final RedContentProposal[] proposals = provider.computeProposals("anything", 2, context);
        assertThat(proposals).isEmpty();
    }

    @Test
    public void thereAreNoResourcesProposalsProvided_whenInAnyColumnNotAResourceSetting() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(getFile(project, "suite.robot"));

        final RobotSetting setting = new RobotSetting(null, SettingsGroup.VARIABLES,
                new VariablesImport(RobotToken.create("Variables")));

        final IRowDataProvider<Object> dataProvider = createDataProvider(setting);
        final ResourceFileLocationsProposalsProvider provider = new ResourceFileLocationsProposalsProvider(model,
                dataProvider);

        for (int column = 0; column < 10; column++) {
            final AssistantContext context = new NatTableAssistantContext(column, 0);
            assertThat(provider.computeProposals("foo", 0, context)).isNull();
        }
    }

    @Test
    public void thereAreNoResourcesProposalsProvided_whenInColumnOtherThanFirstOfResourceSetting() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(getFile(project, "suite.robot"));

        final RobotSetting setting = new RobotSetting(null, SettingsGroup.RESOURCES,
                new ResourceImport(RobotToken.create("Resource")));

        final IRowDataProvider<Object> dataProvider = createDataProvider(setting);
        final ResourceFileLocationsProposalsProvider provider = new ResourceFileLocationsProposalsProvider(model,
                dataProvider);

        for (int column = 0; column < 10; column++) {
            final AssistantContext context = new NatTableAssistantContext(column, 0);
            if (column == 1) {
                assertThat(provider.computeProposals("foo", 0, context)).isNotNull();
            } else {
                assertThat(provider.computeProposals("foo", 0, context)).isNull();
            }
        }
    }

    @Test
    public void thereAreResourcesProposalsProvided_whenInFirstColumnOfResourceSetting() {
        final Text text = new Text(shell, SWT.SINGLE);
        text.setText("a_bc");

        final RobotSuiteFile model = new RobotModel().createSuiteFile(getFile(project, "suite.robot"));

        final RobotSetting setting = new RobotSetting(null, SettingsGroup.RESOURCES,
                new ResourceImport(RobotToken.create("Resource")));

        final IRowDataProvider<Object> dataProvider = createDataProvider(setting);
        final ResourceFileLocationsProposalsProvider provider = new ResourceFileLocationsProposalsProvider(model, dataProvider);

        final AssistantContext context = new NatTableAssistantContext(1, 0);
        final RedContentProposal[] proposals = provider.computeProposals(text.getText(), 2, context);
        assertThat(proposals).hasSize(1);

        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("a_res.robot");
    }

    @Test
    public void thereAreNoVariablesProposalsProvided_whenNothingMatchesToCurrentInput() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(getFile(project, "suite.robot"));

        final RobotSetting setting = new RobotSetting(null, SettingsGroup.VARIABLES,
                new VariablesImport(RobotToken.create("Variables")));

        final IRowDataProvider<Object> dataProvider = createDataProvider(setting);
        final VariableFileLocationsProposalsProvider provider = new VariableFileLocationsProposalsProvider(model,
                dataProvider);

        final AssistantContext context = new NatTableAssistantContext(1, 0);
        final RedContentProposal[] proposals = provider.computeProposals("anything", 2, context);
        assertThat(proposals).isEmpty();
    }

    @Test
    public void thereAreNoVariablesProposalsProvided_whenInAnyColumnNotAVariablesSetting() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(getFile(project, "suite.robot"));

        final RobotSetting setting = new RobotSetting(null, SettingsGroup.LIBRARIES,
                new LibraryImport(RobotToken.create("Library")));

        final IRowDataProvider<Object> dataProvider = createDataProvider(setting);
        final VariableFileLocationsProposalsProvider provider = new VariableFileLocationsProposalsProvider(model,
                dataProvider);

        for (int column = 0; column < 10; column++) {
            final AssistantContext context = new NatTableAssistantContext(column, 0);
            assertThat(provider.computeProposals("foo", 0, context)).isNull();
        }
    }

    @Test
    public void thereAreNoVariablesProposalsProvided_whenInColumnOtherThanFirstOfVariablesSetting() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(getFile(project, "suite.robot"));

        final RobotSetting setting = new RobotSetting(null, SettingsGroup.VARIABLES,
                new VariablesImport(RobotToken.create("Variables")));

        final IRowDataProvider<Object> dataProvider = createDataProvider(setting);
        final VariableFileLocationsProposalsProvider provider = new VariableFileLocationsProposalsProvider(model,
                dataProvider);

        for (int column = 0; column < 10; column++) {
            final AssistantContext context = new NatTableAssistantContext(column, 0);
            if (column == 1) {
                assertThat(provider.computeProposals("foo", 0, context)).isNotNull();
            } else {
                assertThat(provider.computeProposals("foo", 0, context)).isNull();
            }
        }
    }

    @Test
    public void thereAreVariablesProposalsProvided_whenInFirstColumnOfVariablesSetting() {
        final Text text = new Text(shell, SWT.SINGLE);
        text.setText("a_bc");

        final RobotSuiteFile model = new RobotModel().createSuiteFile(getFile(project, "suite.robot"));

        final RobotSetting setting = new RobotSetting(null, SettingsGroup.VARIABLES,
                new VariablesImport(RobotToken.create("Variables")));

        final IRowDataProvider<Object> dataProvider = createDataProvider(setting);
        final VariableFileLocationsProposalsProvider provider = new VariableFileLocationsProposalsProvider(model,
                dataProvider);

        final AssistantContext context = new NatTableAssistantContext(1, 0);
        final RedContentProposal[] proposals = provider.computeProposals(text.getText(), 2, context);
        assertThat(proposals).hasSize(1);

        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("a_vars.py");
    }

    @Test
    public void thereAreNoLibrariesProposalsProvided_whenNothingMatchesCurrentInput() {
        final RobotModel robotModel = new RobotModel();
        final RobotSuiteFile model = robotModel.createSuiteFile(getFile(project, "suite.robot"));
        final RobotProject robotProject = robotModel.createRobotProject(project);

        robotProject.setStandardLibraries(Libraries.createStdLibs("aLib", "bLib"));

        final RobotSetting setting = new RobotSetting(null, SettingsGroup.LIBRARIES,
                new LibraryImport(RobotToken.create("Library")));

        final IRowDataProvider<Object> dataProvider = createDataProvider(setting);
        final LibrariesProposalsProvider provider = new LibrariesProposalsProvider(model, dataProvider);

        final AssistantContext context = new NatTableAssistantContext(1, 0);
        final RedContentProposal[] proposals = provider.computeProposals("%^&", 1, context);
        assertThat(proposals).isEmpty();
    }

    @Test
    public void thereAreNoLibrariesAsWellAsFilesProposalsProvided_whenInFirstOfNonLibrarySetting() {
        final RobotModel robotModel = new RobotModel();
        final RobotSuiteFile model = robotModel.createSuiteFile(getFile(project, "suite.robot"));
        final RobotProject robotProject = robotModel.createRobotProject(project);

        robotProject.setStandardLibraries(Libraries.createStdLibs("aLib", "bLib"));

        final RobotSetting setting = new RobotSetting(null, SettingsGroup.RESOURCES,
                new ResourceImport(RobotToken.create("Resource")));

        final IRowDataProvider<Object> dataProvider = createDataProvider(setting);
        final LibrariesProposalsProvider provider = new LibrariesProposalsProvider(model, dataProvider);

        final AssistantContext context = new NatTableAssistantContext(1, 0);
        assertThat(provider.computeProposals("foo", 0, context)).isNull();
    }

    @Test
    public void thereAreNoLibrariesAsWellAsFilesProposalsProvided_whenInColumnOtherThanFirstOfLibrarySetting() {
        final RobotModel robotModel = new RobotModel();
        final RobotSuiteFile model = robotModel.createSuiteFile(getFile(project, "suite.robot"));
        final RobotProject robotProject = robotModel.createRobotProject(project);

        robotProject.setStandardLibraries(Libraries.createStdLibs("aLib", "bLib"));

        final RobotSetting setting = new RobotSetting(null, SettingsGroup.LIBRARIES,
                new LibraryImport(RobotToken.create("Library")));

        final IRowDataProvider<Object> dataProvider = createDataProvider(setting);
        final LibrariesProposalsProvider provider = new LibrariesProposalsProvider(model, dataProvider);

        for (int column = 0; column < 10; column++) {
            final AssistantContext context = new NatTableAssistantContext(column, 0);
            if (column == 1) {
                assertThat(provider.computeProposals("foo", 0, context)).isNotNull();
            } else {
                assertThat(provider.computeProposals("foo", 0, context)).isNull();
            }
        }
    }

    @Test
    public void thereAreLibrariesAsWellAsFilesProposalsProvided_whenInFirstColumnOfLibrarySetting() {
        final Text text = new Text(shell, SWT.SINGLE);
        text.setText("abc");

        final RobotModel robotModel = new RobotModel();
        final RobotSuiteFile model = robotModel.createSuiteFile(getFile(project, "suite.robot"));
        final RobotProject robotProject = robotModel.createRobotProject(project);

        robotProject.setStandardLibraries(Libraries.createStdLibs("aLib", "bLib"));

        final RobotSetting setting = new RobotSetting(null, SettingsGroup.LIBRARIES,
                new LibraryImport(RobotToken.create("Library")));

        final IRowDataProvider<Object> dataProvider = createDataProvider(setting);
        final LibrariesProposalsProvider provider = new LibrariesProposalsProvider(model, dataProvider);

        final AssistantContext context = new NatTableAssistantContext(1, 0);
        final RedContentProposal[] proposals = provider.computeProposals(text.getText(), 1, context);
        assertThat(proposals.length).isGreaterThanOrEqualTo(3);

        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("aLib");

        proposals[1].getModificationStrategy().insert(text, proposals[1]);
        assertThat(text.getText()).isEqualTo("a_vars.py");

        proposals[2].getModificationStrategy().insert(text, proposals[2]);
        assertThat(text.getText()).isEqualTo("b_vars.py");
    }

    private IRowDataProvider<Object> createDataProvider(final RobotSetting setting) {
        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(setting);
        return dataProvider;
    }
}

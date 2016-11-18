/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors.HyperlinksToFilesDetectorTest.objectsOfClass;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.FileHyperlink;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.ProjectProvider;

public class TableHyperlinksToFilesDetectorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(TableHyperlinksToFilesDetectorTest.class);

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("file.robot", "");
        projectProvider.createFile("vars.py", "");
        projectProvider.createFile("lib.class", "");
        projectProvider.createFile("unhandled.txt", "");
        projectProvider.createDir("directory");
        projectProvider.createFile("directory/file.robot", "");
    }

    @Test
    public void noHyperlinksAreProvided_whenLibraryIsImportedUsingName() throws Exception {
        assertThat(detect("f1.robot", "Library", "lib_name")).isEmpty();
    }

    @Test
    public void noHyperlinksAreProvided_whenUsingAbsolutePathWhichIsOutsideWorkspace() throws Exception {
        assertThat(detect("f2.robot", "Library", "/abs_path/to_file.robot")).isEmpty();
    }

    @Test
    public void noHyperlinksAreProvided_whenRelativePathPointsToDirectory() throws Exception {
        assertThat(detect("f3.robot", "Library", "directory/")).isEmpty();
    }

    @Test
    public void noHyperlinksAreProvided_whenAbsolutePathPointsToDirectory() throws Exception {
        final String absPath = projectProvider.getProject().getLocation().append("directory/").toString();
        assertThat(detect("f4.robot", "Library", absPath)).isEmpty();
    }

    @Test
    public void noHyperlinksAreProvided_whenRelativePathPointsNonExistingFile() throws Exception {
        assertThat(detect("f5.robot", "Resource", "directory/non_existing.robot")).isEmpty();
    }

    @Test
    public void noHyperlinksAreProvided_whenAbsolutePathPointsNonExistingFile() throws Exception {
        final String absPath = projectProvider.getProject().getLocation()
                .append("directory/non_existing.robot").toString();
        assertThat(detect("f6.robot", "Resource", absPath)).isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void noHyperlinksAreProvided_whenNonFirstColumnIsChosen() throws Exception {
        final IFile file = projectProvider.createFile("f7.robot",
                "*** Settings ***",
                "Resource  file.robot  arg1  arg2");
        final RobotSuiteFile suiteFile = new RobotModel().createSuiteFile(file);

        final RobotSetting setting = (RobotSetting) suiteFile
                .findSection(RobotSettingsSection.class).get()
                .getChildren().get(0);

        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(setting);

        final TableHyperlinksToFilesDetector detector = new TableHyperlinksToFilesDetector(dataProvider);
        assertThat(detector.detectHyperlinks(0, 0, "Resource", 0)).isEmpty();
        assertThat(detector.detectHyperlinks(0, 2, "arg1", 0)).isEmpty();
        assertThat(detector.detectHyperlinks(0, 3, "arg2", 0)).isEmpty();
    }

    @Test
    public void noHyperlinksAreProvided_forNonImportSetting() throws Exception {
        assertThat(detect("f8.robot", "Metadata", "file.robot")).isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void noHyperlinksAreProvided_forOtherElements() throws Exception {
        final IFile file = projectProvider.createFile("f9.robot",
                "*** Keywords ***",
                "kw",
                "  Log  file.robot");
        final RobotSuiteFile suiteFile = new RobotModel().createSuiteFile(file);

        final RobotKeywordCall logCall = suiteFile
                .findSection(RobotKeywordsSection.class).get()
                .getChildren().get(0).getChildren().get(0);

        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(logCall);

        final TableHyperlinksToFilesDetector detector = new TableHyperlinksToFilesDetector(dataProvider);
        assertThat(detector.detectHyperlinks(0, 1, "file.robot", 0)).isEmpty();
    }

    @Test
    public void fileHyperlinkIsProvided_whenPathPointsToExistingFile_1() throws Exception {
        final List<IHyperlink> hyperlinks = detect("f10.robot", "Resource", "file.robot");
        assertThat(hyperlinks).hasSize(1).have(objectsOfClass(FileHyperlink.class));
    }

    @Test
    public void fileHyperlinkIsProvided_whenPathPointsToExistingFile_2() throws Exception {
        final List<IHyperlink> hyperlinks = detect("f11.robot", "Variables", "vars.py");
        assertThat(hyperlinks).hasSize(1).have(objectsOfClass(FileHyperlink.class));
    }

    @Test
    public void fileHyperlinkIsProvided_whenPathPointsToExistingFile_3() throws Exception {
        final List<IHyperlink> hyperlinks = detect("f12.robot", "Library", "lib.class");
        assertThat(hyperlinks).hasSize(1).have(objectsOfClass(FileHyperlink.class));
    }

    @SuppressWarnings("unchecked")
    private static List<IHyperlink> detect(final String file, final String settingName, final String path)
            throws Exception {
        final IFile f = projectProvider.createFile(file, "*** Settings ***", settingName + "  " + path);
        final RobotSuiteFile suiteFile = new RobotModel().createSuiteFile(f);
        final RobotSetting setting = (RobotSetting) suiteFile.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(0);

        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(setting);

        final TableHyperlinksToFilesDetector detector = new TableHyperlinksToFilesDetector(dataProvider);
        return detector.detectHyperlinks(0, 1, path, 0);
    }
}

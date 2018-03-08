/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.CompoundHyperlink;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.KeywordDocumentationHyperlink;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.KeywordInLibrarySourceHyperlink;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.SuiteFileTableElementHyperlink;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.UserKeywordDocumentationHyperlink;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.library.Libraries;
import org.robotframework.red.junit.ProjectProvider;

@SuppressWarnings("unchecked")
public class TableHyperlinksToKeywordsDetectorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(TableHyperlinksToKeywordsDetectorTest.class);

    private static LibrarySpecification libSpec;

    private static Map<LibraryDescriptor, LibrarySpecification> refLibs;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "res_kw",
                "  log  10");

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedLibrary(
                ReferencedLibrary.create(LibraryType.PYTHON, "testlib", projectProvider.getProject().getName()));

        projectProvider.createFile("testlib.py");
        projectProvider.configure(config);

        refLibs = Libraries.createRefLib("testlib", "lib_kw");
        libSpec = refLibs.values().iterator().next();
    }

    @AfterClass
    public static void afterSuite() {
        libSpec = null;
    }

    @Test
    public void noHyperlinksAreProvided_whenGivenElementIsArbitraryObject() throws Exception {
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(new Object()).thenReturn("something");
        when(dataProvider.getRowObject(1)).thenReturn(new Object()).thenReturn("something");

        final TableHyperlinksToKeywordsDetector detector = new TableHyperlinksToKeywordsDetector(dataProvider);
        assertThat(detector.detectHyperlinks(0, 0, "Log", 0)).isEmpty();
        assertThat(detector.detectHyperlinks(1, 1, "Log", 0)).isEmpty();
    }

    @Test
    public void noHyperlinksAreProvided_whenKeywordIsNotLocatedInKeywordsTable() throws Exception {
        final String labelWithKeyword = "kw1";
        final IFile file = projectProvider.createFile("f0.robot",
                "*** Test Cases ***",
                "case",
                "  " + labelWithKeyword + "  ${x}");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final RobotKeywordCall element = suiteFile.findSection(RobotCasesSection.class).get()
                .getChildren().get(0).getChildren().get(0);

        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(1)).thenReturn(element);

        final TableHyperlinksToKeywordsDetector detector = new TableHyperlinksToKeywordsDetector(model, dataProvider);
        assertThat(detector.detectHyperlinks(1, 0, labelWithKeyword, 0)).isEmpty();
    }

    @Test
    public void noHyperlinksAreProvided_whenKeywordIsNotLocatedInResourceFile() throws Exception {
        final String labelWithKeyword = "kw1";
        final IFile file = projectProvider.createFile("f1.robot",
                "*** Test Cases ***",
                "case",
                "  " + labelWithKeyword + "  ${x}",
                "*** Settings ***",
                "Resource  file.robot");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final RobotKeywordCall element = suiteFile.findSection(RobotCasesSection.class).get()
                .getChildren().get(0).getChildren().get(0);

        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(1)).thenReturn(element);

        final TableHyperlinksToKeywordsDetector detector = new TableHyperlinksToKeywordsDetector(model, dataProvider);
        assertThat(detector.detectHyperlinks(1, 0, labelWithKeyword, 0)).isEmpty();
    }

    @Test
    public void noHyperlinksAreProvided_whenKeywordIsNotLocatedInLibrary() throws Exception {
        final String labelWithKeyword = "some_kw";
        final IFile file = projectProvider.createFile("f2.robot",
                "*** Test Cases ***",
                "case",
                "  " + labelWithKeyword + "  ${x}",
                "*** Settings ***",
                "Library  testlib");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final RobotKeywordCall element = suiteFile.findSection(RobotCasesSection.class).get()
                .getChildren().get(0).getChildren().get(0);

        final RobotProject project = suiteFile.getProject();
        project.setStandardLibraries(new HashMap<>());
        project.setReferencedLibraries(refLibs);

        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(1)).thenReturn(element);

        final TableHyperlinksToKeywordsDetector detector = new TableHyperlinksToKeywordsDetector(model, dataProvider);
        assertThat(detector.detectHyperlinks(1, 0, labelWithKeyword, 0)).isEmpty();
    }

    @Test
    public void hyperlinksAreProvided_whenKeywordIsLocatedInKeywordsTable() throws Exception {
        final String labelWithKeyword = "kw1";
        final IFile file = projectProvider.createFile("f3.robot",
                "*** Test Cases ***",
                "case",
                "  " + labelWithKeyword + "  ${x}",
                "*** Keywords ***",
                "kw1");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final RobotKeywordCall element = suiteFile.findSection(RobotCasesSection.class).get()
                .getChildren().get(0).getChildren().get(0);
        final RobotKeywordDefinition kw1 = suiteFile.findSection(RobotKeywordsSection.class).get().getChildren().get(0);

        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(1)).thenReturn(element);

        final TableHyperlinksToKeywordsDetector detector = new TableHyperlinksToKeywordsDetector(model, dataProvider);
        final List<IHyperlink> hyperlinks = detector.detectHyperlinks(1, 0, labelWithKeyword, 0);

        assertThat(hyperlinks).hasSize(2);
        assertThat(hyperlinks.get(0)).isInstanceOf(SuiteFileTableElementHyperlink.class);
        assertThat(((SuiteFileTableElementHyperlink) hyperlinks.get(0)).getDestinationFile()).isSameAs(suiteFile);
        assertThat(((SuiteFileTableElementHyperlink) hyperlinks.get(0)).getDestinationElement()).isSameAs(kw1);
        assertThat(hyperlinks.get(1)).isInstanceOf(UserKeywordDocumentationHyperlink.class);
        assertThat(((UserKeywordDocumentationHyperlink) hyperlinks.get(1)).getDestinationKeyword()).isSameAs(kw1);
    }

    @Test
    public void hyperlinksAreProvided_whenKeywordIsLocatedInResourceFile() throws Exception {
        final String labelWithKeyword = "res_kw";
        final IFile file = projectProvider.createFile("f4.robot",
                "*** Test Cases ***",
                "case",
                "  " + labelWithKeyword + "  ${x}",
                "*** Settings ***",
                "Resource  file.robot");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final RobotSuiteFile resSuiteFile = model.createSuiteFile(projectProvider.getFile("file.robot"));
        final RobotKeywordCall element = suiteFile.findSection(RobotCasesSection.class).get()
                .getChildren().get(0).getChildren().get(0);
        final RobotKeywordDefinition resKw = resSuiteFile.findSection(RobotKeywordsSection.class).get()
                .getChildren().get(0);

        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(1)).thenReturn(element);

        final TableHyperlinksToKeywordsDetector detector = new TableHyperlinksToKeywordsDetector(model, dataProvider);
        final List<IHyperlink> hyperlinks = detector.detectHyperlinks(1, 0, labelWithKeyword, 0);

        assertThat(hyperlinks).hasSize(2);
        assertThat(hyperlinks.get(0)).isInstanceOf(SuiteFileTableElementHyperlink.class);
        assertThat(((SuiteFileTableElementHyperlink) hyperlinks.get(0)).getDestinationFile()).isSameAs(resSuiteFile);
        assertThat(((SuiteFileTableElementHyperlink) hyperlinks.get(0)).getDestinationElement()).isSameAs(resKw);
        assertThat(hyperlinks.get(1)).isInstanceOf(UserKeywordDocumentationHyperlink.class);
        assertThat(((UserKeywordDocumentationHyperlink) hyperlinks.get(1)).getDestinationKeyword()).isSameAs(resKw);
    }

    @Test
    public void hyperlinksAreProvided_whenKeywordIsLocatedInLibrary() throws Exception {
        final String labelWithKeyword = "lib_kw";
        final IFile file = projectProvider.createFile("f5.robot",
                "*** Test Cases ***",
                "case",
                "  " + labelWithKeyword + "  ${x}",
                "*** Settings ***",
                "Library  testlib");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final RobotKeywordCall element = suiteFile.findSection(RobotCasesSection.class).get()
                .getChildren().get(0).getChildren().get(0);

        final RobotProject project = suiteFile.getProject();
        project.setStandardLibraries(new HashMap<>());
        project.setReferencedLibraries(refLibs);

        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(1)).thenReturn(element);

        final TableHyperlinksToKeywordsDetector detector = new TableHyperlinksToKeywordsDetector(model, dataProvider);
        final List<IHyperlink> hyperlinks = detector.detectHyperlinks(1, 0, labelWithKeyword, 0);

        assertThat(hyperlinks).hasSize(2);
        assertThat(hyperlinks.get(0)).isInstanceOf(KeywordInLibrarySourceHyperlink.class);
        assertThat(((KeywordInLibrarySourceHyperlink) hyperlinks.get(0)).getDestinationSpecification())
                .isSameAs(libSpec);
        assertThat(hyperlinks.get(1)).isInstanceOf(KeywordDocumentationHyperlink.class);
        assertThat(((KeywordDocumentationHyperlink) hyperlinks.get(1)).getDestinationLibrarySpecification())
                .isSameAs(libSpec);
        assertThat(((KeywordDocumentationHyperlink) hyperlinks.get(1)).getDestinationKeywordSpecification())
                .isSameAs(libSpec.getKeywords().get(0));
    }

    @Test
    public void hyperlinksAreProvided_whenKeywordIsUsedInGherkinStyle() throws Exception {
        final IFile file = projectProvider.createFile("f6.robot",
                "*** Test Cases ***",
                "case",
                "  given kw",
                "  and kw",
                "  but kw",
                "  when kw",
                "  then kw",
                "*** Keywords ***",
                "kw");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final List<RobotKeywordCall> calls = suiteFile.findSection(RobotCasesSection.class)
                .get().getChildren().get(0).getChildren();
        final RobotKeywordDefinition kw = suiteFile.findSection(RobotKeywordsSection.class).get().getChildren().get(0);

        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);

        for (int i = 1; i <= calls.size(); i++) {
            when(dataProvider.getRowObject(i)).thenReturn(calls.get(i - 1));
        }

        final TableHyperlinksToKeywordsDetector detector = new TableHyperlinksToKeywordsDetector(model, dataProvider);

        int i = 1;
        for (final RobotKeywordCall call : calls) {
            final List<IHyperlink> hyperlinks = detector.detectHyperlinks(i, 0, call.getName(), 0);

            assertThat(hyperlinks.get(0)).isInstanceOf(SuiteFileTableElementHyperlink.class);
            assertThat(((SuiteFileTableElementHyperlink) hyperlinks.get(0)).getDestinationFile()).isSameAs(suiteFile);
            assertThat(((SuiteFileTableElementHyperlink) hyperlinks.get(0)).getDestinationElement()).isSameAs(kw);

            i++;
        }
    }

    @Test
    public void multipleHyperlinksAreProvided_whenKeywordIsDefinedMultipleTimes() throws Exception {
        final String labelWithKeyword = "res_kw";
        final IFile file = projectProvider.createFile("f7.robot",
                "*** Test Cases ***",
                "case",
                "  " + labelWithKeyword + "  ${x}",
                "*** Keywords ***",
                "res_kw",
                "*** Settings ***",
                "Resource  file.robot");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final RobotKeywordCall element = suiteFile.findSection(RobotCasesSection.class)
                .get().getChildren().get(0).getChildren().get(0);
        final RobotKeywordDefinition kw = suiteFile.findSection(RobotKeywordsSection.class)
                .get().getChildren().get(0);

        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(1)).thenReturn(element);

        final TableHyperlinksToKeywordsDetector detector = new TableHyperlinksToKeywordsDetector(model, dataProvider);
        final List<IHyperlink> hyperlinks = detector.detectHyperlinks(1, 0, labelWithKeyword, 0);

        assertThat(hyperlinks).hasSize(4);

        assertThat(hyperlinks.get(0)).isInstanceOf(SuiteFileTableElementHyperlink.class);
        assertThat(((SuiteFileTableElementHyperlink) hyperlinks.get(0)).getDestinationFile()).isSameAs(suiteFile);
        assertThat(((SuiteFileTableElementHyperlink) hyperlinks.get(0)).getDestinationElement()).isSameAs(kw);

        assertThat(hyperlinks.get(1)).isInstanceOf(UserKeywordDocumentationHyperlink.class);
        assertThat(((UserKeywordDocumentationHyperlink) hyperlinks.get(1)).getDestinationKeyword()).isSameAs(kw);

        assertThat(hyperlinks.get(2)).isInstanceOf(CompoundHyperlink.class);
        assertThat(((CompoundHyperlink) hyperlinks.get(2)).getHyperlinks()).hasSize(2);
        assertThat(((CompoundHyperlink) hyperlinks.get(2)).getHyperlinks().get(0)).isInstanceOf(SuiteFileTableElementHyperlink.class);
        assertThat(((CompoundHyperlink) hyperlinks.get(2)).getHyperlinks().get(1)).isInstanceOf(SuiteFileTableElementHyperlink.class);

        assertThat(hyperlinks.get(3)).isInstanceOf(CompoundHyperlink.class);
        assertThat(((CompoundHyperlink) hyperlinks.get(3)).getHyperlinks()).hasSize(2);
        assertThat(((CompoundHyperlink) hyperlinks.get(3)).getHyperlinks().get(0)).isInstanceOf(UserKeywordDocumentationHyperlink.class);
        assertThat(((CompoundHyperlink) hyperlinks.get(3)).getHyperlinks().get(1)).isInstanceOf(UserKeywordDocumentationHyperlink.class);
    }
}

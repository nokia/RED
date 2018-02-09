/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.CompoundHyperlink;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.KeywordDocumentationHyperlink;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.KeywordInLibrarySourceHyperlink;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.RegionsHyperlink;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.SuiteFileSourceRegionHyperlink;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.UserKeywordDocumentationHyperlink;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public class SourceHyperlinksToKeywordsDetectorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(SourceHyperlinksToKeywordsDetectorTest.class);

    private static ReferencedLibrary lib;
    private static LibrarySpecification libSpec;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("file.robot",
                "*** Keywords ***",
                "res_kw",
                "  log  10");

        lib = ReferencedLibrary.create(LibraryType.PYTHON, "testlib", projectProvider.getProject().getName());

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedLibrary(lib);

        projectProvider.createFile("testlib.py");
        projectProvider.configure(config);

        final KeywordSpecification kwSpec = new KeywordSpecification();
        kwSpec.setFormat("ROBOT");
        kwSpec.setName("lib_kw");
        kwSpec.setArguments(new ArrayList<String>());
        kwSpec.setDocumentation("");

        libSpec = new LibrarySpecification();
        libSpec.setName("testlib");
        libSpec.getKeywords().add(kwSpec);
    }

    @AfterClass
    public static void afterSuite() {
        lib = null;
        libSpec = null;
    }

    @Test
    public void noHyperlinksAreProvided_whenRegionsIsOutsideOfFile() throws Exception {
        final IFile file = projectProvider.createFile("f0.robot",
                "*** Test Cases ***",
                "case",
                "  Log  10");
        final RobotSuiteFile suiteFile = new RobotModel().createSuiteFile(file);
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final SourceHyperlinksToKeywordsDetector detector = new SourceHyperlinksToKeywordsDetector(suiteFile);
        assertThat(detector.detectHyperlinks(textViewer, new Region(-100, 1), true)).isNull();
        assertThat(detector.detectHyperlinks(textViewer, new Region(100, 1), true)).isNull();
    }

    @Test
    public void noHyperlinksAreProvided_whenGivenLocationIsNotOverKeyword() throws Exception {
        final IFile file = projectProvider.createFile("f1.robot",
                "*** Test Cases ***",
                "case",
                "  kw1  ${x}",
                "  kw2  ${y}",
                "*** Keywords ***",
                "kw1",
                "kw2");
        final RobotSuiteFile suiteFile = new RobotModel().createSuiteFile(file);
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final int begin1 = 26;
        final int begin2 = 38;
        final int begin3 = 65;
        final int begin4 = 69;

        assertThat(document.get(begin1, 3)).isEqualTo("kw1");
        assertThat(document.get(begin2, 3)).isEqualTo("kw2");
        assertThat(document.get(begin3, 3)).isEqualTo("kw1");
        assertThat(document.get(begin4, 3)).isEqualTo("kw2");
        final RangeSet<Integer> varsPositions = TreeRangeSet.create();
        varsPositions.add(Range.closed(begin1, begin1 + 3));
        varsPositions.add(Range.closed(begin2, begin2 + 3));
        varsPositions.add(Range.closed(begin3, begin3 + 3));
        varsPositions.add(Range.closed(begin4, begin4 + 3));

        final SourceHyperlinksToKeywordsDetector detector = new SourceHyperlinksToKeywordsDetector(suiteFile);
        for (int i = 0; i < document.getLength(); i++) {
            if (!varsPositions.contains(i)) {
                assertThat(detector.detectHyperlinks(textViewer, new Region(i, 1), true)).isNull();
            }
        }
    }

    @Test
    public void noHyperlinksAreProvided_whenKeywordIsNotLocatedInKeywordsTable() throws Exception {
        final IFile file = projectProvider.createFile("f2.robot",
                "*** Test Cases ***",
                "case",
                "  kw1  ${x}");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final int begin = 26;
        assertThat(document.get(begin, 3)).isEqualTo("kw1");

        final SourceHyperlinksToKeywordsDetector detector = new SourceHyperlinksToKeywordsDetector(model, suiteFile);
        assertThat(detector.detectHyperlinks(textViewer, new Region(begin + 1, 1), true)).isNull();
    }

    @Test
    public void noHyperlinksAreProvided_whenKeywordIsNotLocatedInResourceFile() throws Exception {
        final IFile file = projectProvider.createFile("f3.robot",
                "*** Test Cases ***",
                "case",
                "  kw1  ${x}",
                "*** Settings ***",
                "Resource  file.robot");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final int begin = 26;
        assertThat(document.get(begin, 3)).isEqualTo("kw1");

        final SourceHyperlinksToKeywordsDetector detector = new SourceHyperlinksToKeywordsDetector(model, suiteFile);
        assertThat(detector.detectHyperlinks(textViewer, new Region(begin + 1, 1), true)).isNull();
    }

    @Test
    public void noHyperlinksAreProvided_whenKeywordIsNotLocatedInLibrary() throws Exception {
        final IFile file = projectProvider.createFile("f4.robot",
                "*** Test Cases ***",
                "case",
                "  some_kw  ${x}",
                "*** Settings ***",
                "Library  testlib");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final Document document = new Document(getContent(file));

        final RobotProject project = suiteFile.getProject();
        project.setStandardLibraries(ImmutableMap.<String, LibrarySpecification> of());
        project.setReferencedLibraries(ImmutableMap.of(lib, libSpec));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final int begin = 26;
        assertThat(document.get(begin, 7)).isEqualTo("some_kw");

        final SourceHyperlinksToKeywordsDetector detector = new SourceHyperlinksToKeywordsDetector(model, suiteFile);
        assertThat(detector.detectHyperlinks(textViewer, new Region(begin + 1, 1), true)).isNull();
    }

    @Test
    public void hyperlinksAreProvided_whenKeywordIsLocatedInKeywordsTable() throws Exception {
        final IFile file = projectProvider.createFile("f5.robot",
                "*** Test Cases ***",
                "case",
                "  kw1  ${x}",
                "*** Keywords ***",
                "kw1");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final RobotKeywordDefinition kw1 = suiteFile.findSection(RobotKeywordsSection.class).get().getChildren().get(0);
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final int begin = 26;
        assertThat(document.get(begin, 3)).isEqualTo("kw1");

        final SourceHyperlinksToKeywordsDetector detector = new SourceHyperlinksToKeywordsDetector(model, suiteFile);
        final IHyperlink[] hyperlinks = detector.detectHyperlinks(textViewer, new Region(begin + 1, 1), true);

        assertThat(hyperlinks).hasSize(2);
        assertThat(hyperlinks[0]).isInstanceOf(RegionsHyperlink.class);
        assertThat(((RegionsHyperlink) hyperlinks[0]).getDestinationRegion()).isEqualTo(new Region(53, 3));
        assertThat(hyperlinks[1]).isInstanceOf(UserKeywordDocumentationHyperlink.class);
        assertThat(((UserKeywordDocumentationHyperlink) hyperlinks[1]).getDestinationKeyword()).isSameAs(kw1);
    }

    @Test
    public void hyperlinksAreProvided_whenKeywordIsLocatedInResourceFile() throws Exception {
        final IFile file = projectProvider.createFile("f6.robot",
                "*** Test Cases ***",
                "case",
                "  res_kw  ${x}",
                "*** Settings ***",
                "Resource  file.robot");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final RobotSuiteFile resSuiteFile = model.createSuiteFile(projectProvider.getFile("file.robot"));
        final RobotKeywordDefinition resKw = resSuiteFile.findSection(RobotKeywordsSection.class).get()
                .getChildren().get(0);
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final int begin = 26;
        assertThat(document.get(begin, 6)).isEqualTo("res_kw");

        final SourceHyperlinksToKeywordsDetector detector = new SourceHyperlinksToKeywordsDetector(model, suiteFile);
        final IHyperlink[] hyperlinks = detector.detectHyperlinks(textViewer, new Region(begin + 1, 1), true);

        assertThat(hyperlinks).hasSize(2);
        assertThat(hyperlinks[0]).isInstanceOf(SuiteFileSourceRegionHyperlink.class);
        assertThat(((SuiteFileSourceRegionHyperlink) hyperlinks[0]).getDestinationFile()).isSameAs(resSuiteFile);
        assertThat(((SuiteFileSourceRegionHyperlink) hyperlinks[0]).getDestinationRegion())
                .isEqualTo(new Region(17, 6));
        assertThat(hyperlinks[1]).isInstanceOf(UserKeywordDocumentationHyperlink.class);
        assertThat(((UserKeywordDocumentationHyperlink) hyperlinks[1]).getDestinationKeyword()).isSameAs(resKw);
    }

    @Test
    public void hyperlinksAreProvided_whenKeywordIsLocatedInLibrary() throws Exception {
        final IFile file = projectProvider.createFile("f7.robot",
                "*** Test Cases ***",
                "case",
                "  lib_kw  ${x}",
                "*** Settings ***",
                "Library  testlib");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final Document document = new Document(getContent(file));

        final RobotProject project = suiteFile.getProject();
        project.setStandardLibraries(ImmutableMap.<String, LibrarySpecification> of());
        project.setReferencedLibraries(ImmutableMap.of(lib, libSpec));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final int begin = 26;
        assertThat(document.get(begin, 6)).isEqualTo("lib_kw");

        final SourceHyperlinksToKeywordsDetector detector = new SourceHyperlinksToKeywordsDetector(model, suiteFile);
        final IHyperlink[] hyperlinks = detector.detectHyperlinks(textViewer, new Region(begin + 1, 1), true);

        assertThat(hyperlinks).hasSize(2);
        assertThat(hyperlinks[0]).isInstanceOf(KeywordInLibrarySourceHyperlink.class);
        assertThat(((KeywordInLibrarySourceHyperlink) hyperlinks[0]).getDestinationSpecification()).isSameAs(libSpec);
        assertThat(hyperlinks[1]).isInstanceOf(KeywordDocumentationHyperlink.class);
        assertThat(((KeywordDocumentationHyperlink) hyperlinks[1]).getDestinationLibrarySpecification())
                .isSameAs(libSpec);
        assertThat(((KeywordDocumentationHyperlink) hyperlinks[1]).getDestinationKeywordSpecification())
                .isSameAs(libSpec.getKeywords().get(0));
    }

    @Test
    public void hyperlinksAreProvided_whenKeywordIsUsedInGherkinStyle() throws Exception {
        final IFile file = projectProvider.createFile("f8.robot",
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
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final int[] begins = new int[] { 26, 37, 46, 55, 65 };
        final int[] prefixLengths = new int[] { 6, 4, 4, 5, 5 };
        assertThat(document.get(begins[0], 8)).isEqualTo("given kw");
        assertThat(document.get(begins[1], 6)).isEqualTo("and kw");
        assertThat(document.get(begins[2], 6)).isEqualTo("but kw");
        assertThat(document.get(begins[3], 7)).isEqualTo("when kw");
        assertThat(document.get(begins[4], 7)).isEqualTo("then kw");

        final SourceHyperlinksToKeywordsDetector detector = new SourceHyperlinksToKeywordsDetector(model, suiteFile);

        for (int i = 0; i < begins.length; i++) {
            final IHyperlink[] hyperlinks = detector.detectHyperlinks(textViewer, new Region(begins[i] + 1, 1), true);
            assertThat(((RegionsHyperlink) hyperlinks[0]).getHyperlinkRegion())
                    .isEqualTo(new Region(begins[i] + prefixLengths[i], 2));
            assertThat(((RegionsHyperlink) hyperlinks[0]).getDestinationRegion()).isEqualTo(new Region(90, 2));
        }
    }

    @Test
    public void multipleHyperlinksAreProvided_whenKeywordIsDefinedMultipleTimes() throws Exception {
        final IFile file = projectProvider.createFile("f9.robot",
                "*** Test Cases ***",
                "case",
                "  res_kw  ${x}",
                "*** Keywords ***",
                "res_kw",
                "*** Settings ***",
                "Resource  file.robot");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final RobotKeywordDefinition kw = suiteFile.findSection(RobotKeywordsSection.class)
                .get().getChildren().get(0);
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final int begin = 26;
        assertThat(document.get(begin, 6)).isEqualTo("res_kw");

        final SourceHyperlinksToKeywordsDetector detector = new SourceHyperlinksToKeywordsDetector(model, suiteFile);
        final IHyperlink[] hyperlinks = detector.detectHyperlinks(textViewer, new Region(begin + 1, 1), true);

        assertThat(hyperlinks).hasSize(4);
        assertThat(hyperlinks[0]).isInstanceOf(RegionsHyperlink.class);
        assertThat(((RegionsHyperlink) hyperlinks[0]).getDestinationRegion()).isEqualTo(new Region(56, 6));

        assertThat(hyperlinks[1]).isInstanceOf(UserKeywordDocumentationHyperlink.class);
        assertThat(((UserKeywordDocumentationHyperlink) hyperlinks[1]).getDestinationKeyword()).isSameAs(kw);

        assertThat(hyperlinks[2]).isInstanceOf(CompoundHyperlink.class);
        assertThat(((CompoundHyperlink) hyperlinks[2]).getHyperlinks()).hasSize(2);
        assertThat(((CompoundHyperlink) hyperlinks[2]).getHyperlinks().get(0)).isInstanceOf(RegionsHyperlink.class);
        assertThat(((CompoundHyperlink) hyperlinks[2]).getHyperlinks().get(1)).isInstanceOf(SuiteFileSourceRegionHyperlink.class);

        assertThat(hyperlinks[3]).isInstanceOf(CompoundHyperlink.class);
        assertThat(((CompoundHyperlink) hyperlinks[3]).getHyperlinks()).hasSize(2);
        assertThat(((CompoundHyperlink) hyperlinks[3]).getHyperlinks().get(0)).isInstanceOf(UserKeywordDocumentationHyperlink.class);
        assertThat(((CompoundHyperlink) hyperlinks[3]).getHyperlinks().get(1)).isInstanceOf(UserKeywordDocumentationHyperlink.class);
    }

    private static List<String> getContent(final IFile file) {
        try (InputStream stream = file.getContents()) {
            return Splitter.on('\n').splitToList(projectProvider.getFileContent(file));
        } catch (IOException | CoreException e) {
            return new ArrayList<>();
        }
    }
}

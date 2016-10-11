/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors.HyperlinksToFilesDetectorTest.objectsOfClass;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.testdata.imported.ScalarRobotInternalVariable;
import org.rf.ide.core.testdata.importer.VariablesFileImportReference;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.RegionsHyperlink;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.SuiteFileSourceRegionHyperlink;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public class SourceHyperlinksToVariablesDetectorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(SourceHyperlinksToVariablesDetectorTest.class);

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("file.robot", "*** Variables ***", "${res_var}  20");
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

        final SourceHyperlinksToVariablesDetector detector = new SourceHyperlinksToVariablesDetector(suiteFile);
        assertThat(detector.detectHyperlinks(textViewer, new Region(-100, 1), true)).isNull();
        assertThat(detector.detectHyperlinks(textViewer, new Region(100, 1), true)).isNull();
    }

    @Test
    public void noHyperlinksAreProvided_whenGivenLocationIsNotOverVariable() throws Exception {
        final IFile file = projectProvider.createFile("f1.robot",
                "*** Test Cases ***",
                "case",
                "  Log  ${var}",
                "*** Variables ***",
                "${var}  1");
        final RobotSuiteFile suiteFile = new RobotModel().createSuiteFile(file);
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final int begin1 = 31;
        final int begin2 = 56;

        assertThat(document.get(begin1, 6)).isEqualTo("${var}");
        assertThat(document.get(begin2, 6)).isEqualTo("${var}");
        final RangeSet<Integer> varsPositions = TreeRangeSet.create();
        varsPositions.add(Range.closed(begin1, begin1 + 6));
        varsPositions.add(Range.closed(begin2, begin2 + 6));

        final SourceHyperlinksToVariablesDetector detector = new SourceHyperlinksToVariablesDetector(suiteFile);
        for (int i = 0; i < document.getLength(); i++) {
            if (!varsPositions.contains(i)) {
                assertThat(detector.detectHyperlinks(textViewer, new Region(i, 1), true)).isNull();
            }
        }
    }

    @Test
    public void noHyperlinksAreProvided_whenVariableIsLocallyDefinedInOtherCodeEntity() throws Exception {
        final IFile file = projectProvider.createFile("f2.robot",
                "*** Test Cases ***",
                "case",
                "  Log  ${var}",
                "case2",
                "  ${var}=  kw");
        final RobotSuiteFile suiteFile = new RobotModel().createSuiteFile(file);
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final int begin = 31;
        assertThat(document.get(begin, 6)).isEqualTo("${var}");
        final SourceHyperlinksToVariablesDetector detector = new SourceHyperlinksToVariablesDetector(suiteFile);
        assertThat(detector.detectHyperlinks(textViewer, new Region(begin + 3, 1), true)).isNull();
    }

    @Test
    public void noHyperlinksAreProvided_whenVariableIsLocallyDefinedAfterGivenLocation() throws Exception {
        final IFile file = projectProvider.createFile("f3.robot",
                "*** Test Cases ***",
                "case",
                "  Log  ${var}",
                "  ${var}=  kw");
        final RobotSuiteFile suiteFile = new RobotModel().createSuiteFile(file);
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final int begin = 31;
        assertThat(document.get(begin, 6)).isEqualTo("${var}");
        final SourceHyperlinksToVariablesDetector detector = new SourceHyperlinksToVariablesDetector(suiteFile);
        assertThat(detector.detectHyperlinks(textViewer, new Region(begin + 3, 1), true)).isNull();
    }

    @Test
    public void noHyperlinksAreProvided_whenVariableIsNotLocatedInVariablesTable() throws Exception {
        final IFile file = projectProvider.createFile("f4.robot",
                "*** Test Cases ***",
                "case",
                "  Log  ${var}",
                "*** Variables ***",
                "${v}  1");
        final RobotSuiteFile suiteFile = new RobotModel().createSuiteFile(file);
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final int begin = 31;
        assertThat(document.get(begin, 6)).isEqualTo("${var}");
        final SourceHyperlinksToVariablesDetector detector = new SourceHyperlinksToVariablesDetector(suiteFile);
        assertThat(detector.detectHyperlinks(textViewer, new Region(begin + 3, 1), true)).isNull();
    }

    @Test
    public void noHyperlinksAreProvided_whenVariableIsNotLocatedInResourceImport() throws Exception {
        final IFile file = projectProvider.createFile("f5.robot",
                "*** Test Cases ***",
                "case",
                "  Log  ${var}",
                "*** Settings ***",
                "Resource  file.robot");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final int begin = 31;
        assertThat(document.get(begin, 6)).isEqualTo("${var}");
        final SourceHyperlinksToVariablesDetector detector = new SourceHyperlinksToVariablesDetector(model, suiteFile);
        assertThat(detector.detectHyperlinks(textViewer, new Region(begin + 3, 1), true)).isNull();
    }

    // TODO : subject of change? maybe we want to open variable files?
    @Test
    public void noHyperlinksAreProvided_whenVariableIsDefinedInVariablesImport() throws Exception {
        final IFile file = projectProvider.createFile("f6.robot",
                "*** Test Cases ***",
                "tc",
                "  Log  ${var}",
                "*** Settings ***",
                "Variables  vars.py");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);

        final RobotSetting varSetting = (RobotSetting) suiteFile.findSection(RobotSettingsSection.class)
                .get().getChildren().get(0);
        final VariablesImport varsImport = (VariablesImport) varSetting.getLinkedElement();
        final VariablesFileImportReference varsImportRef = new VariablesFileImportReference(varsImport);
        varsImportRef.map(ImmutableMap.<String, Object> of("x", 100, "var", 42, "z", 1729));
        final RobotFileOutput output = suiteFile.getLinkedElement().getParent();
        output.setVariablesImportReferences(newArrayList(varsImportRef));
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final int begin = 29;
        assertThat(document.get(begin, 6)).isEqualTo("${var}");

        final SourceHyperlinksToVariablesDetector detector = new SourceHyperlinksToVariablesDetector(model, suiteFile);
        assertThat(detector.detectHyperlinks(textViewer, new Region(begin + 3, 1), true)).isNull();
    }

    // TODO : subject of change? maybe we want to open variable files?
    @Test
    public void noHyperlinksAreProbided_whenVariableIsDefinedInProjectVarFiles() throws Exception {
        final IFile file = projectProvider.createFile("f7.robot",
                "*** Test Cases ***",
                "tc",
                "  Log  ${var}");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final RobotProject robotProject = suiteFile.getProject();
        final ReferencedVariableFile varsImportRef = new ReferencedVariableFile();
        varsImportRef.setVariables(ImmutableMap.<String, Object> of("x", 100, "var", 42, "z", 1729));
        robotProject.setReferencedVariablesFiles(newArrayList(varsImportRef));
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final int begin = 29;
        assertThat(document.get(begin, 6)).isEqualTo("${var}");
        
        final SourceHyperlinksToVariablesDetector detector = new SourceHyperlinksToVariablesDetector(model, suiteFile);
        assertThat(detector.detectHyperlinks(textViewer, new Region(begin + 3, 1), true)).isNull();
    }

    @Test
    public void noHyperlinkIsProvided_whenVariableIsGlobal() throws Exception {
        final IFile file = projectProvider.createFile("f8.robot",
                "*** Test Cases ***",
                "tc",
                "  Log  ${var}");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final RobotProjectHolder projectHolder = suiteFile.getProject().getRobotProjectHolder();
        projectHolder.getGlobalVariables().clear();
        projectHolder.getGlobalVariables().addAll(newArrayList(new ScalarRobotInternalVariable("var", "val")));
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final int begin = 29;
        assertThat(document.get(begin, 6)).isEqualTo("${var}");

        final SourceHyperlinksToVariablesDetector detector = new SourceHyperlinksToVariablesDetector(model, suiteFile);
        assertThat(detector.detectHyperlinks(textViewer, new Region(begin + 3, 1), true)).isNull();
    }

    @Test
    public void hyperlinksAreProvided_forLocallyDefinedVariableInTestCase() throws Exception {
        final IFile file = projectProvider.createFile("f9.robot",
                "*** Test Cases ***",
                "tc",
                "  ${var}=  call",
                "  ${x}=  call2",
                "  Log  ${var}");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final int begin = 60;
        assertThat(document.get(begin, 6)).isEqualTo("${var}");

        final SourceHyperlinksToVariablesDetector detector = new SourceHyperlinksToVariablesDetector(model, suiteFile);
        final IHyperlink[] hyperlinks = detector.detectHyperlinks(textViewer, new Region(begin + 3, 1), true);
        assertThat(hyperlinks).hasSize(1).have(objectsOfClass(RegionsHyperlink.class));
        assertThat(((RegionsHyperlink) hyperlinks[0]).getDestinationRegion()).isEqualTo(new Region(24, 6));
    }

    @Test
    public void hyperlinksAreProvided_forLocallyDefinedVariableInKeyword() throws Exception {
        final IFile file = projectProvider.createFile("f10.robot",
                "*** Keywords ***",
                "kw",
                "  ${var}=  call",
                "  ${x}=  call2",
                "  Log  ${var}");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final int begin = 58;
        assertThat(document.get(begin, 6)).isEqualTo("${var}");

        final SourceHyperlinksToVariablesDetector detector = new SourceHyperlinksToVariablesDetector(model, suiteFile);
        final IHyperlink[] hyperlinks = detector.detectHyperlinks(textViewer, new Region(begin + 3, 1), true);
        assertThat(hyperlinks).hasSize(1).have(objectsOfClass(RegionsHyperlink.class));
        assertThat(((RegionsHyperlink) hyperlinks[0]).getDestinationRegion()).isEqualTo(new Region(22, 6));
    }

    @Test
    public void hyperlinksAreProvided_forLocallyDefinedVariableInKeywordArguments() throws Exception {
        final IFile file = projectProvider.createFile("f11.robot",
                "*** Keywords ***",
                "kw",
                "  [Arguments]  ${arg}",
                "  Log  ${arg}");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final int begin = 49;
        assertThat(document.get(begin, 6)).isEqualTo("${arg}");

        final SourceHyperlinksToVariablesDetector detector = new SourceHyperlinksToVariablesDetector(model, suiteFile);
        final IHyperlink[] hyperlinks = detector.detectHyperlinks(textViewer, new Region(begin + 3, 1), true);
        assertThat(hyperlinks).hasSize(1).have(objectsOfClass(RegionsHyperlink.class));
        assertThat(((RegionsHyperlink) hyperlinks[0]).getDestinationRegion()).isEqualTo(new Region(35, 6));
    }

    @Test
    public void hyperlinksAreProvided_forLocallyDefinedVariableInKeywordEmbeddedArguments() throws Exception {
        final IFile file = projectProvider.createFile("f12.robot",
                "*** Keywords ***",
                "kw ${arg} name",
                "  Log  ${arg}");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final int begin = 39;
        assertThat(document.get(begin, 6)).isEqualTo("${arg}");

        final SourceHyperlinksToVariablesDetector detector = new SourceHyperlinksToVariablesDetector(model, suiteFile);
        final IHyperlink[] hyperlinks = detector.detectHyperlinks(textViewer, new Region(begin + 3, 1), true);
        assertThat(hyperlinks).hasSize(1).have(objectsOfClass(RegionsHyperlink.class));
        assertThat(((RegionsHyperlink) hyperlinks[0]).getDestinationRegion()).isEqualTo(new Region(20, 6));
    }

    @Test
    public void hyperlinksAreProvided_forVariablesDefinedInVariableTable() throws Exception {
        final IFile file = projectProvider.createFile("f13.robot",
                "*** Test Cases ***",
                "case",
                "  Log  ${var}",
                "*** Variables ***",
                "${list}  a  b",
                "${var}  1");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final int begin = 31;
        assertThat(document.get(begin, 6)).isEqualTo("${var}");

        final SourceHyperlinksToVariablesDetector detector = new SourceHyperlinksToVariablesDetector(model, suiteFile);
        final IHyperlink[] hyperlinks = detector.detectHyperlinks(textViewer, new Region(begin + 3, 1), true);
        assertThat(hyperlinks).hasSize(1).have(objectsOfClass(RegionsHyperlink.class));
        assertThat(((RegionsHyperlink) hyperlinks[0]).getDestinationRegion()).isEqualTo(new Region(70, 6));
    }

    @Test
    public void hyperlinksAreProvided_forVariablesDefinedInResourcesImport() throws Exception {
        final IFile file = projectProvider.createFile("f14.robot",
                "*** Test Cases ***",
                "case",
                "  Log  ${res_var}",
                "*** Settings ***",
                "Resource  file.robot");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final Document document = new Document(getContent(file));

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final int begin = 31;
        assertThat(document.get(begin, 10)).isEqualTo("${res_var}");

        final SourceHyperlinksToVariablesDetector detector = new SourceHyperlinksToVariablesDetector(model, suiteFile);
        final IHyperlink[] hyperlinks = detector.detectHyperlinks(textViewer, new Region(begin + 3, 1), true);
        assertThat(hyperlinks).hasSize(1).have(objectsOfClass(SuiteFileSourceRegionHyperlink.class));
        assertThat(((SuiteFileSourceRegionHyperlink) hyperlinks[0]).getDestinationFile().getFile())
                .isEqualTo(projectProvider.getFile("file.robot"));
        assertThat(((SuiteFileSourceRegionHyperlink) hyperlinks[0]).getDestinationRegion())
                .isEqualTo(new Region(18, 10));
    }

    private static List<String> getContent(final IFile file) {
        try (InputStream stream = file.getContents()) {
            return Splitter.on('\n').splitToList(projectProvider.getFileContent(file));
        } catch (IOException | CoreException e) {
            return new ArrayList<>();
        }
    }
}

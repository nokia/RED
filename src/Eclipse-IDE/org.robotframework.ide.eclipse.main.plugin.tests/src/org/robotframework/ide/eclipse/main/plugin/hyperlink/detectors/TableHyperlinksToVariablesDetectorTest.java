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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.testdata.imported.ScalarRobotInternalVariable;
import org.rf.ide.core.testdata.importer.VariablesFileImportReference;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.SuiteFileTableElementHyperlink;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

@SuppressWarnings("unchecked")
public class TableHyperlinksToVariablesDetectorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(TableHyperlinksToVariablesDetectorTest.class);

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("file.robot", "*** Variables ***", "${res_var}  20");
    }

    @Test
    public void noHyperlinksAreProvided_whenGivenElementIsArbitraryObject() throws Exception {
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(new Object()).thenReturn("something");

        final TableHyperlinksToVariablesDetector detector = new TableHyperlinksToVariablesDetector(dataProvider);
        assertThat(detector.detectHyperlinks(0, 0, "label", 0)).isEmpty();
        assertThat(detector.detectHyperlinks(1, 1, "label", 0)).isEmpty();
    }

    @Test
    public void noHyperlinksAreProvided_whenGivenLocationIsNotOverVariable() throws Exception {
        final String labelWithVar = "abc${var}def";
        final IFile file = projectProvider.createFile("f1.robot",
                "*** Test Cases ***",
                "case",
                "  Log  " + labelWithVar,
                "*** Variables ***",
                "${var}  1");
        final RobotSuiteFile suiteFile = new RobotModel().createSuiteFile(file);
        final RobotKeywordCall element = suiteFile.findSection(RobotCasesSection.class).get()
                .getChildren().get(0)
                .getChildren().get(0);

        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(element);

        final Range<Integer> varRange = Range.closed(3, 8);

        final TableHyperlinksToVariablesDetector detector = new TableHyperlinksToVariablesDetector(dataProvider);
        for (int i = 0; i < labelWithVar.length(); i++) {
            if (!varRange.contains(i)) {
                assertThat(detector.detectHyperlinks(0, 0, labelWithVar, i)).isEmpty();
            }
        }
    }

    @Test
    public void noHyperlinksAreProvided_whenVariableIsLocallyDefinedInOtherCodeEntity() throws Exception {
        final String labelWithVar = "${var}";
        final IFile file = projectProvider.createFile("f2.robot",
                "*** Test Cases ***",
                "case",
                "  Log  " + labelWithVar,
                "case2",
                "  ${var}=  kw");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final RobotKeywordCall element = suiteFile.findSection(RobotCasesSection.class).get()
                .getChildren().get(0)
                .getChildren().get(0);

        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(element);

        final TableHyperlinksToVariablesDetector detector = new TableHyperlinksToVariablesDetector(model, dataProvider);
        assertThat(detector.detectHyperlinks(0, 0, labelWithVar, 0)).isEmpty();
    }

    @Test
    public void noHyperlinksAreProvided_whenVariableIsLocallyDefinedAfterGivenLocation() throws Exception {
        final String labelWithVar = "${var}";
        final IFile file = projectProvider.createFile("f3.robot",
                "*** Test Cases ***",
                "case",
                "  Log  " + labelWithVar,
                "  ${var}=  kw");
        final RobotSuiteFile suiteFile = new RobotModel().createSuiteFile(file);
        final RobotKeywordCall element = suiteFile.findSection(RobotCasesSection.class).get()
                .getChildren().get(0)
                .getChildren().get(0);

        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(element);

        final TableHyperlinksToVariablesDetector detector = new TableHyperlinksToVariablesDetector(dataProvider);
        assertThat(detector.detectHyperlinks(0, 0, labelWithVar, 0)).isEmpty();
    }

    @Test
    public void noHyperlinksAreProvided_whenVariableIsNotLocatedInVariablesTable() throws Exception {
        final String labelWithVar = "${var}";
        final IFile file = projectProvider.createFile("f4.robot",
                "*** Test Cases ***",
                "case",
                "  Log  " + labelWithVar,
                "*** Variables ***",
                "${v}  1");
        final RobotSuiteFile suiteFile = new RobotModel().createSuiteFile(file);
        final RobotKeywordCall element = suiteFile.findSection(RobotCasesSection.class).get()
                .getChildren().get(0)
                .getChildren().get(0);

        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(element);

        final TableHyperlinksToVariablesDetector detector = new TableHyperlinksToVariablesDetector(dataProvider);
        assertThat(detector.detectHyperlinks(0, 0, labelWithVar, 0)).isEmpty();
    }

    @Test
    public void noHyperlinksAreProvided_whenVariableIsNotLocatedInResourceImport() throws Exception {
        final String labelWithVar = "${var}";
        final IFile file = projectProvider.createFile("f5.robot",
                "*** Test Cases ***",
                "case",
                "  Log  " + labelWithVar,
                "*** Settings ***",
                "Resource  file.robot");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final RobotKeywordCall element = suiteFile.findSection(RobotCasesSection.class).get()
                .getChildren().get(0)
                .getChildren().get(0);

        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(element);
        
        final TableHyperlinksToVariablesDetector detector = new TableHyperlinksToVariablesDetector(model, dataProvider);
        assertThat(detector.detectHyperlinks(0, 0, labelWithVar, 0)).isEmpty();
    }

    // TODO : subject of change? maybe we want to open variable files?
    @Test
    public void noHyperlinksAreProvided_whenVariableIsDefinedInVariablesImport() throws Exception {
        final String labelWithVar = "${var}";
        final IFile file = projectProvider.createFile("f6.robot",
                "*** Test Cases ***",
                "tc",
                "  Log  " + labelWithVar,
                "*** Settings ***",
                "Variables  vars.py");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final RobotKeywordCall element = suiteFile.findSection(RobotCasesSection.class).get()
                .getChildren().get(0)
                .getChildren().get(0);

        final RobotSetting varSetting = (RobotSetting) suiteFile.findSection(RobotSettingsSection.class)
                .get().getChildren().get(0);
        final VariablesImport varsImport = (VariablesImport) varSetting.getLinkedElement();
        final VariablesFileImportReference varsImportRef = new VariablesFileImportReference(varsImport);
        varsImportRef.map(ImmutableMap.<String, Object> of("x", 100, "var", 42, "z", 1729));
        final RobotFileOutput output = suiteFile.getLinkedElement().getParent();
        output.setVariablesImportReferences(newArrayList(varsImportRef));
        
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(element);
        
        final TableHyperlinksToVariablesDetector detector = new TableHyperlinksToVariablesDetector(model, dataProvider);
        assertThat(detector.detectHyperlinks(0, 0, labelWithVar, 0)).isEmpty();
    }

    // TODO : subject of change? maybe we want to open variable files?
    @Test
    public void noHyperlinksAreProbided_whenVariableIsDefinedInProjectVarFiles() throws Exception {
        final String labelWithVar = "${var}";
        final IFile file = projectProvider.createFile("f7.robot",
                "*** Test Cases ***",
                "tc",
                "  Log  " + labelWithVar);
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final RobotProject robotProject = suiteFile.getProject();
        final ReferencedVariableFile varsImportRef = new ReferencedVariableFile();
        varsImportRef.setVariables(ImmutableMap.<String, Object> of("x", 100, "var", 42, "z", 1729));
        robotProject.setReferencedVariablesFiles(newArrayList(varsImportRef));

        final RobotKeywordCall element = suiteFile.findSection(RobotCasesSection.class).get()
                .getChildren().get(0)
                .getChildren().get(0);

        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(element);
        
        final TableHyperlinksToVariablesDetector detector = new TableHyperlinksToVariablesDetector(model, dataProvider);
        assertThat(detector.detectHyperlinks(0, 0, labelWithVar, 0)).isEmpty();
    }

    @Test
    public void noHyperlinkIsProvided_whenVariableIsGlobal() throws Exception {
        final String labelWithVar = "${var}";
        final IFile file = projectProvider.createFile("f8.robot",
                "*** Test Cases ***",
                "tc",
                "  Log  " + labelWithVar);
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final RobotProjectHolder projectHolder = suiteFile.getProject().getRobotProjectHolder();
        projectHolder.getGlobalVariables().clear();
        projectHolder.getGlobalVariables().addAll(newArrayList(new ScalarRobotInternalVariable("var", "val")));

        final RobotKeywordCall element = suiteFile.findSection(RobotCasesSection.class).get()
                .getChildren().get(0)
                .getChildren().get(0);

        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(element);
        
        final TableHyperlinksToVariablesDetector detector = new TableHyperlinksToVariablesDetector(model, dataProvider);
        assertThat(detector.detectHyperlinks(0, 0, labelWithVar, 0)).isEmpty();
    }

    @Test
    public void hyperlinksAreProvided_forLocallyDefinedVariableInTestCase() throws Exception {
        final String labelWithVar = "${var}";
        final IFile file = projectProvider.createFile("f9.robot",
                "*** Test Cases ***",
                "tc",
                "  ${var}=  call",
                "  ${x}=  call2",
                "  Log  " + labelWithVar);
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final List<RobotKeywordCall> calls = suiteFile.findSection(RobotCasesSection.class).get()
                .getChildren().get(0)
                .getChildren();

        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(calls.get(2));
        
        final TableHyperlinksToVariablesDetector detector = new TableHyperlinksToVariablesDetector(model, dataProvider);
        final List<IHyperlink> hyperlinks = detector.detectHyperlinks(0, 0, labelWithVar, 0);

        assertThat(hyperlinks).hasSize(1).have(objectsOfClass(SuiteFileTableElementHyperlink.class));
        assertThat(((SuiteFileTableElementHyperlink) hyperlinks.get(0)).getDestinationFile().getFile())
                .isEqualTo(projectProvider.getFile("f9.robot"));
        assertThat(((SuiteFileTableElementHyperlink) hyperlinks.get(0)).getDestinationElement())
                .isSameAs(calls.get(0));
    }

    @Test
    public void hyperlinksAreProvided_forLocallyDefinedVariableInKeyword() throws Exception {
        final String labelWithVar = "${var}";
        final IFile file = projectProvider.createFile("f10.robot",
                "*** Keywords ***",
                "kw",
                "  ${var}=  call",
                "  ${x}=  call2",
                "  Log  " + labelWithVar);
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final List<RobotKeywordCall> calls = suiteFile.findSection(RobotKeywordsSection.class).get()
                .getChildren().get(0)
                .getChildren();

        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(calls.get(2));
        
        final TableHyperlinksToVariablesDetector detector = new TableHyperlinksToVariablesDetector(model, dataProvider);
        final List<IHyperlink> hyperlinks = detector.detectHyperlinks(0, 0, labelWithVar, 0);

        assertThat(hyperlinks).hasSize(1).have(objectsOfClass(SuiteFileTableElementHyperlink.class));
        assertThat(((SuiteFileTableElementHyperlink) hyperlinks.get(0)).getDestinationFile().getFile())
                .isEqualTo(projectProvider.getFile("f10.robot"));
        assertThat(((SuiteFileTableElementHyperlink) hyperlinks.get(0)).getDestinationElement())
                .isSameAs(calls.get(0));
    }

    @Test
    public void hyperlinksAreProvided_forLocallyDefinedVariableInKeywordArguments() throws Exception {
        final String labelWithVar = "${arg}";
        final IFile file = projectProvider.createFile("f11.robot",
                "*** Keywords ***",
                "kw",
                "  [Arguments]  ${var}  ${arg}",
                "  Log  " + labelWithVar);
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final List<RobotKeywordDefinition> keywords = suiteFile.findSection(RobotKeywordsSection.class)
                .get().getChildren();
        final List<RobotKeywordCall> calls = keywords.get(0).getChildren();

        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(calls.get(1));

        final TableHyperlinksToVariablesDetector detector = new TableHyperlinksToVariablesDetector(model, dataProvider);
        final List<IHyperlink> hyperlinks = detector.detectHyperlinks(0, 0, labelWithVar, 0);

        assertThat(hyperlinks).hasSize(1).have(objectsOfClass(SuiteFileTableElementHyperlink.class));
        assertThat(((SuiteFileTableElementHyperlink) hyperlinks.get(0)).getDestinationFile().getFile())
                .isEqualTo(projectProvider.getFile("f11.robot"));
        assertThat(((SuiteFileTableElementHyperlink) hyperlinks.get(0)).getDestinationElement())
                .isSameAs(keywords.get(0));
    }

    @Test
    public void hyperlinksAreProvided_forLocallyDefinedVariableInKeywordEmbeddedArguments() throws Exception {
        final String labelWithVar = "${arg}";
        final IFile file = projectProvider.createFile("f12.robot",
                "*** Keywords ***",
                "kw ${arg} name",
                "  Log  " + labelWithVar);
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final List<RobotKeywordDefinition> keywords = suiteFile.findSection(RobotKeywordsSection.class)
                .get().getChildren();
        final List<RobotKeywordCall> calls = keywords.get(0).getChildren();

        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(calls.get(0));

        final TableHyperlinksToVariablesDetector detector = new TableHyperlinksToVariablesDetector(model, dataProvider);
        final List<IHyperlink> hyperlinks = detector.detectHyperlinks(0, 0, labelWithVar, 0);

        assertThat(hyperlinks).hasSize(1).have(objectsOfClass(SuiteFileTableElementHyperlink.class));
        assertThat(((SuiteFileTableElementHyperlink) hyperlinks.get(0)).getDestinationFile().getFile())
                .isEqualTo(projectProvider.getFile("f12.robot"));
        assertThat(((SuiteFileTableElementHyperlink) hyperlinks.get(0)).getDestinationElement())
                .isSameAs(keywords.get(0));
    }

    @Test
    public void hyperlinksAreProvided_forVariablesDefinedInVariableTable() throws Exception {
        final String labelWithVar = "${var}";
        final IFile file = projectProvider.createFile("f13.robot",
                "*** Test Cases ***",
                "case",
                "  Log  " + labelWithVar,
                "*** Variables ***",
                "${list}  a  b",
                "${var}  1");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final RobotKeywordCall element = suiteFile.findSection(RobotCasesSection.class).get()
                .getChildren().get(0)
                .getChildren().get(0);
        final RobotVariable destinationVariable = suiteFile.findSection(RobotVariablesSection.class).get()
                .getChildren().get(1);

        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(element);
        
        final TableHyperlinksToVariablesDetector detector = new TableHyperlinksToVariablesDetector(model, dataProvider);
        final List<IHyperlink> hyperlinks = detector.detectHyperlinks(0, 0, labelWithVar, 0);

        assertThat(hyperlinks).hasSize(1).have(objectsOfClass(SuiteFileTableElementHyperlink.class));
        assertThat(((SuiteFileTableElementHyperlink) hyperlinks.get(0)).getDestinationFile().getFile())
                .isEqualTo(projectProvider.getFile("f13.robot"));
        assertThat(((SuiteFileTableElementHyperlink) hyperlinks.get(0)).getDestinationElement())
                .isSameAs(destinationVariable);
    }

    @Test
    public void hyperlinksAreProvided_forVariablesDefinedInResourcesImport() throws Exception {
        final String labelWithVar = "${res_var}";
        final IFile file = projectProvider.createFile("f14.robot",
                "*** Test Cases ***",
                "case",
                "  Log  " + labelWithVar,
                "*** Settings ***",
                "Resource  file.robot");
        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(file);
        final RobotKeywordCall element = suiteFile.findSection(RobotCasesSection.class).get()
                .getChildren().get(0)
                .getChildren().get(0);
        final RobotSuiteFile destSuiteFile = model.createSuiteFile(projectProvider.getFile("file.robot"));
        final RobotVariable destinationVariable = destSuiteFile.findSection(RobotVariablesSection.class).get()
                .getChildren().get(0);

        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(element);
        
        final TableHyperlinksToVariablesDetector detector = new TableHyperlinksToVariablesDetector(model, dataProvider);
        final List<IHyperlink> hyperlinks = detector.detectHyperlinks(0, 0, labelWithVar, 0);

        assertThat(hyperlinks).hasSize(1).have(objectsOfClass(SuiteFileTableElementHyperlink.class));
        assertThat(((SuiteFileTableElementHyperlink) hyperlinks.get(0)).getDestinationFile().getFile())
                .isEqualTo(projectProvider.getFile("file.robot"));
        assertThat(((SuiteFileTableElementHyperlink) hyperlinks.get(0)).getDestinationElement())
                .isSameAs(destinationVariable);
    }
}

/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.locators;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.testdata.imported.DictionaryRobotInternalVariable;
import org.rf.ide.core.testdata.imported.ListRobotInternalVariable;
import org.rf.ide.core.testdata.imported.ScalarRobotInternalVariable;
import org.rf.ide.core.testdata.importer.VariablesFileImportReference;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.locators.VariableDefinitionLocator.VariableDetector;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ObjectArrays;

public class VariableDefinitionLocatorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(
            VariableDefinitionLocatorTest.class.getSimpleName());

    @Before
    public void beforeTest() throws CoreException {
        projectProvider.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
    }

    @Test
    public void variablesDefinedInPreviousCallsAreLocatedByOffset_1() throws Exception {
        final IFile file = projectProvider.createFile("resource.robot", createTestCasesSection());

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(file, new RobotModel());
        locator.locateVariableDefinitionWithLocalScope(localVariableDetector(visitedVars), 48);
        assertThat(visitedVars).containsOnly("${x}");
    }

    @Test
    public void variablesDefinedInPreviousCallsAreLocatedByOffset_2() throws Exception {
        final IFile file = projectProvider.createFile("resource.robot", createTestCasesSection());

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(file, new RobotModel());
        locator.locateVariableDefinitionWithLocalScope(localVariableDetector(visitedVars), 74);
        assertThat(visitedVars).containsOnly("${x}", "${y}");
    }

    @Test
    public void variablesDefinedInPreviousCallsAreLocatedByOffset_onlyUntilDetectorWantsToContinue() throws Exception {
        final IFile file = projectProvider.createFile("resource.robot", createTestCasesSection());

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(file, new RobotModel());
        locator.locateVariableDefinitionWithLocalScope(limitedLocalVariableDetector(visitedVars), 74);
        assertThat(visitedVars).containsOnly("${x}");
    }

    @Test
    public void variablesDefinedInKeywordArgumentsSettingAreLocatedByOffset() throws Exception {
        final IFile file = projectProvider.createFile("resource.robot", createKeywordsSection());

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(file, new RobotModel());
        locator.locateVariableDefinitionWithLocalScope(localVariableDetector(visitedVars), 55);
        assertThat(visitedVars).containsOnly("${x}", "${y}");
    }

    @Test
    public void variablesDefinedInKeywordArgumentsSettingAreLocatedByOffset_onlyUntilDetectorWantsToContinue()
            throws Exception {
        final IFile file = projectProvider.createFile("resource.robot", createKeywordsSection());

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(file, new RobotModel());
        locator.locateVariableDefinitionWithLocalScope(limitedLocalVariableDetector(visitedVars), 55);
        assertThat(visitedVars).containsOnly("${x}");
    }

    @Test
    public void variablesDefinedInEmbeddedKeywordNameAreLocatedByOffset() throws Exception {
        final IFile file = projectProvider.createFile("resource.robot", createKeywordsSection());

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(file, new RobotModel());
        locator.locateVariableDefinitionWithLocalScope(localVariableDetector(visitedVars), 90);
        assertThat(visitedVars).containsOnly("${e}", "${f}");
    }

    @Test
    public void variablesDefinedInEmbeddedKeywordNameAreLocatedByOffset_onlyUntilDetectorWantsToContinue()
            throws Exception {
        final IFile file = projectProvider.createFile("resource.robot", createKeywordsSection());

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(file, new RobotModel());
        locator.locateVariableDefinitionWithLocalScope(limitedLocalVariableDetector(visitedVars), 90);
        assertThat(visitedVars).containsOnly("${e}");
    }

    @Test
    public void variablesDefinedInPreviousCallsAreLocatedByElement_1() throws Exception {
        final IFile file = projectProvider.createFile("resource.robot", createTestCasesSection());
        
        final RobotModel model = new RobotModel();
        final RobotFileInternalElement startingElement = model.createSuiteFile(file)
                .findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0) // test case
                .getChildren()
                .get(1); // second call in case

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(file, model);
        locator.locateVariableDefinitionWithLocalScope(localVariableDetector(visitedVars), startingElement);
        assertThat(visitedVars).containsOnly("${x}");
    }

    @Test
    public void variablesDefinedInPreviousCallsAreLocatedByElement_2() throws Exception {
        final IFile file = projectProvider.createFile("resource.robot", createTestCasesSection());

        final RobotModel model = new RobotModel();
        final RobotFileInternalElement startingElement = model.createSuiteFile(file)
                .findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0) // test case
                .getChildren()
                .get(3); // fourth call in case

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(file, model);
        locator.locateVariableDefinitionWithLocalScope(localVariableDetector(visitedVars), startingElement);
        assertThat(visitedVars).containsOnly("${x}", "${y}");
    }

    @Test
    public void variablesDefinedInPreviousCallsAreLocatedByElement_onlyUntilDetectorWantsToContinue() throws Exception {
        final IFile file = projectProvider.createFile("resource.robot", createTestCasesSection());

        final RobotModel model = new RobotModel();
        final RobotFileInternalElement startingElement = model.createSuiteFile(file)
                .findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0) // test case
                .getChildren()
                .get(3); // fourth call in case

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(file, model);
        locator.locateVariableDefinitionWithLocalScope(limitedLocalVariableDetector(visitedVars), startingElement);
        assertThat(visitedVars).containsOnly("${x}");
    }

    @Test
    public void variablesDefinedInKeywordArgumentsSettingAreLocatedByElement() throws Exception {
        final IFile file = projectProvider.createFile("resource.robot", createKeywordsSection());

        final RobotModel model = new RobotModel();
        final RobotFileInternalElement startingElement = model.createSuiteFile(file)
                .findSection(RobotKeywordsSection.class)
                .get()
                .getChildren()
                .get(0) // keyword
                .getChildren()
                .get(1); // first call after args

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(file, model);
        locator.locateVariableDefinitionWithLocalScope(localVariableDetector(visitedVars), startingElement);
        assertThat(visitedVars).containsOnly("${x}", "${y}");
    }

    @Test
    public void variablesDefinedInKeywordArgumentsSettingAreLocatedByElement_onlyUntilDetectorWantsToContinue()
            throws Exception {
        final IFile file = projectProvider.createFile("resource.robot", createKeywordsSection());

        final RobotModel model = new RobotModel();
        final RobotFileInternalElement startingElement = model.createSuiteFile(file)
                .findSection(RobotKeywordsSection.class)
                .get()
                .getChildren()
                .get(0) // keyword
                .getChildren()
                .get(1); // first call after args

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(file, model);
        locator.locateVariableDefinitionWithLocalScope(limitedLocalVariableDetector(visitedVars), startingElement);
        assertThat(visitedVars).containsOnly("${x}");
    }

    @Test
    public void variablesDefinedInEmbeddedKeywordNameAreLocatedByElement() throws Exception {
        final IFile file = projectProvider.createFile("resource.robot", createKeywordsSection());

        final RobotModel model = new RobotModel();
        final RobotFileInternalElement startingElement = model.createSuiteFile(file)
                .findSection(RobotKeywordsSection.class)
                .get()
                .getChildren()
                .get(1) // keyword
                .getChildren()
                .get(0); // first call after args

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(file, model);
        locator.locateVariableDefinitionWithLocalScope(localVariableDetector(visitedVars), startingElement);
        assertThat(visitedVars).containsOnly("${e}", "${f}");
    }

    @Test
    public void variablesDefinedInEmbeddedKeywordNameAreLocatedByElement_onlyUntilDetectorWantsToContinue()
            throws Exception {
        final IFile file = projectProvider.createFile("resource.robot", createKeywordsSection());

        final RobotModel model = new RobotModel();
        final RobotFileInternalElement startingElement = model.createSuiteFile(file)
                .findSection(RobotKeywordsSection.class)
                .get()
                .getChildren()
                .get(1) // keyword
                .getChildren()
                .get(0); // first call after args

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(file, model);
        locator.locateVariableDefinitionWithLocalScope(limitedLocalVariableDetector(visitedVars), startingElement);
        assertThat(visitedVars).containsOnly("${e}");
    }

    @Test
    public void locallyDefinedVariablesAreLocated() throws Exception {
        final IFile file = projectProvider.createFile("resource.robot", createVariablesSection("1"));

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(file, new RobotModel());
        locator.locateVariableDefinition(variableDetector(visitedVars));
        assertThat(visitedVars).containsOnly("scalar_1", "list_1", "dict_1", "invalid_1");
    }

    @Test
    public void locallyDefinedVariablesAreLocated_onlyUntilDetectorWantsToContinue() throws Exception {
        final IFile file = projectProvider.createFile("resource.robot", createVariablesSection("1"));

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(file, new RobotModel());
        locator.locateVariableDefinition(limitedVariableDetector(visitedVars));
        assertThat(visitedVars).containsOnly("scalar_1", "list_1");
    }


    @Test
    public void variablesDefinedInResourceFilesAreLocated() throws Exception {
        projectProvider.createFile("resource.robot", createVariablesSection("1"));
        final IFile sourceFile = projectProvider.createFile("importingFile.robot",
                createResourceImportSettingsSection("resource.robot"));

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(sourceFile, new RobotModel());
        locator.locateVariableDefinition(variableDetector(visitedVars));
        assertThat(visitedVars).containsOnly("scalar_1", "list_1", "dict_1", "invalid_1");
    }

    @Test
    public void variablesDefinedInResourceFilesAreLocated_onlyUntilDetectorWantsToContinue() throws Exception {
        projectProvider.createFile("resource.robot", createVariablesSection("1"));
        final IFile sourceFile = projectProvider.createFile("importingFile.robot",
                createResourceImportSettingsSection("resource.robot"));

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(sourceFile, new RobotModel());
        locator.locateVariableDefinition(limitedVariableDetector(visitedVars));
        assertThat(visitedVars).containsOnly("scalar_1", "list_1");
    }

    @Test
    public void variablesDefinedInVariablesFilesAreLocated() throws Exception {
        final IFile sourceFile = projectProvider.createFile("importingFile.robot",
                createVariablesImportSettingsSection("vars.py"));

        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(sourceFile);
        
        final RobotSettingsSection settings = suiteFile.findSection(RobotSettingsSection.class).get();
        final RobotSetting varSetting = (RobotSetting) settings.findChild("Variables");
        final VariablesImport varsImport = (VariablesImport) varSetting.getLinkedElement();
        
        final VariablesFileImportReference varsImportRef = new VariablesFileImportReference(varsImport);
        varsImportRef.map(ImmutableMap.of("var_a", 42, "var_b", 1729));

        final RobotFileOutput output = suiteFile.getLinkedElement().getParent();
        output.setVariablesImportReferences(newArrayList(varsImportRef));

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(sourceFile, model);
        locator.locateVariableDefinition(varFileVariableDetector(visitedVars));
        assertThat(visitedVars).containsOnly("${var_a}", "${var_b}");
    }

    @Test
    public void variablesDefinedInVariablesFilesAreLocated_onlyUntilDetectorWantsToContinue() throws Exception {
        final IFile sourceFile = projectProvider.createFile("importingFile.robot",
                createVariablesImportSettingsSection("vars.py"));

        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(sourceFile);

        final RobotSettingsSection settings = suiteFile.findSection(RobotSettingsSection.class).get();
        final RobotSetting varSetting = (RobotSetting) settings.findChild("Variables");
        final VariablesImport varsImport = (VariablesImport) varSetting.getLinkedElement();

        final VariablesFileImportReference varsImportRef = new VariablesFileImportReference(varsImport);
        varsImportRef.map(ImmutableMap.of("var_a", 42, "var_b", 1729));

        final RobotFileOutput output = suiteFile.getLinkedElement().getParent();
        output.setVariablesImportReferences(newArrayList(varsImportRef));

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(sourceFile, model);
        locator.locateVariableDefinition(limitedVarFileVariableDetector(visitedVars));
        assertThat(visitedVars).containsOnly("${var_a}");
    }

    @Test
    public void variablesDefinedInGlobalVariablesFilesLinkedInRedXmlAreLocated() throws IOException, CoreException {
        final IFile sourceFile = projectProvider.createFile("importingFile.robot", "");

        final RobotModel model = new RobotModel();
        final RobotProject robotProject = model.createRobotProject(sourceFile.getProject());

        final ReferencedVariableFile varsImportRef = new ReferencedVariableFile();
        varsImportRef.setVariables(ImmutableMap.<String, Object> of("var_a", 42, "var_b", 1729));

        robotProject.setReferencedVariablesFiles(newArrayList(varsImportRef));

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(sourceFile, model);
        locator.locateVariableDefinition(varFileVariableDetector(visitedVars));
        assertThat(visitedVars).containsOnly("${var_a}", "${var_b}");
    }

    @Test
    public void variablesDefinedInGlobalVariablesFilesLinkedInRedXmlAreLocated_onlyUntilDetectorWantsToContinue()
            throws IOException, CoreException {
        final IFile sourceFile = projectProvider.createFile("importingFile.robot", "");

        final RobotModel model = new RobotModel();
        final RobotProject robotProject = model.createRobotProject(sourceFile.getProject());

        final ReferencedVariableFile varsImportRef = new ReferencedVariableFile();
        varsImportRef.setVariables(ImmutableMap.<String, Object> of("var_a", 42, "var_b", 1729));

        robotProject.setReferencedVariablesFiles(newArrayList(varsImportRef));

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(sourceFile, model);
        locator.locateVariableDefinition(limitedVarFileVariableDetector(visitedVars));
        assertThat(visitedVars).containsOnly("${var_a}");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void globalVariablesAreLocated() throws Exception {
        final IFile sourceFile = projectProvider.createFile("source.robot", "");

        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(sourceFile);
        final RobotProjectHolder projectHolder = suiteFile.getProject().getRobotProjectHolder();
        projectHolder.getGlobalVariables().clear();
        projectHolder.getGlobalVariables()
                .addAll(newArrayList(new ScalarRobotInternalVariable("global_scalar", null),
                        new ListRobotInternalVariable("global_list", null),
                        new DictionaryRobotInternalVariable("global_dict", null)));

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(sourceFile, model);
        locator.locateVariableDefinition(globalVarDetector(visitedVars));
        assertThat(visitedVars).containsOnly("global_scalar", "global_list", "global_dict");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void globalVariablesAreLocated_onlyUntilDetectorWantsToContinue() throws Exception {
        final IFile sourceFile = projectProvider.createFile("source.robot", "");

        final RobotModel model = new RobotModel();
        final RobotSuiteFile suiteFile = model.createSuiteFile(sourceFile);
        final RobotProjectHolder projectHolder = suiteFile.getProject().getRobotProjectHolder();
        projectHolder.getGlobalVariables().clear();
        projectHolder.getGlobalVariables()
                .addAll(newArrayList(new ScalarRobotInternalVariable("global_scalar", null),
                        new ListRobotInternalVariable("global_list", null),
                        new DictionaryRobotInternalVariable("global_dict", null)));

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(sourceFile, model);
        locator.locateVariableDefinition(limitedGlobalVarDetector(visitedVars));
        assertThat(visitedVars).containsOnly("global_scalar", "global_list");
    }

    @Test
    public void variablesAreImportedProperly_whenResourcesAreImportingThemselvesInLoop() throws Exception {

        // those files imports forms a cycle
        final IFile sourceFile = projectProvider.createFile("res1.robot", ObjectArrays
                .concat(createVariablesSection("1"), createResourceImportSettingsSection("res2.robot"), String.class));
        projectProvider.createFile("res2.robot", ObjectArrays.concat(createVariablesSection("2"),
                createResourceImportSettingsSection("res3.robot"), String.class));
        projectProvider.createFile("res3.robot", ObjectArrays.concat(createVariablesSection("3"),
                createResourceImportSettingsSection("res1.robot"), String.class));

        final Set<String> visitedVars = new HashSet<>();
        final VariableDefinitionLocator locator = new VariableDefinitionLocator(sourceFile, new RobotModel());
        locator.locateVariableDefinition(variableDetector(visitedVars));
        assertThat(visitedVars).containsOnly("scalar_1", "list_1", "dict_1", "invalid_1", "scalar_2", "list_2",
                "dict_2", "invalid_2", "scalar_3", "list_3", "dict_3", "invalid_3");
    }

    private static String[] createVariablesSection(final String variableNameSuffix) {
        return new String[] {
                "*** Variables ***",
                "${scalar_" + variableNameSuffix + "}  10",
                "@{list_" + variableNameSuffix + "}    10    20",
                "&{dict_" + variableNameSuffix + "}    a=10  b=20",
                "invalid_" + variableNameSuffix + "    50"};
    }

    private static String[] createVariablesImportSettingsSection(final String varPath) {
        return new String[] {
                "*** Settings ***",
                "Variables  " + varPath };
    }

    private static String[] createResourceImportSettingsSection(final String resourcePath) {
        return new String[] {
                "*** Settings ***",
                "Resource  " + resourcePath};
    }

    private static String[] createTestCasesSection() {
        return new String[] {
                "*** Test Cases ***",
                "case",
                "  ${x}=  call",
                "  Log  ${x}",
                "  ${y}=  call",
                "  Log  ${y}"};
    }

    private static String[] createKeywordsSection() {
        return new String[] {
                "*** Keywords ***",
                "keyword",
                "  [Arguments]  ${x}  ${y}",
                "  Log  1",
                "keyword take ${e} and ${f}",
                "  Log  2" };
    }

    private static VariableDetector localVariableDetector(final Set<String> visitedVars) {
        return new TestVariableDetector() {
            @Override
            public ContinueDecision localVariableDetected(final RobotFileInternalElement element,
                    final RobotToken variable) {
                visitedVars.add(variable.getText());
                return ContinueDecision.CONTINUE;
            }
        };
    }

    private static VariableDetector limitedLocalVariableDetector(final Set<String> visitedVars) {
        return new TestVariableDetector() {

            @Override
            public ContinueDecision localVariableDetected(final RobotFileInternalElement element,
                    final RobotToken variable) {
                visitedVars.add(variable.getText());
                return visitedVars.size() < 1 ? ContinueDecision.CONTINUE : ContinueDecision.STOP;
            }
        };
    }

    private static TestVariableDetector globalVarDetector(final Set<String> visitedVars) {
        return new TestVariableDetector() {

            @Override
            public ContinueDecision globalVariableDetected(final String name, final Object value) {
                visitedVars.add(name);
                return ContinueDecision.CONTINUE;
            }
        };
    }

    private static TestVariableDetector limitedGlobalVarDetector(final Set<String> visitedVars) {
        return new TestVariableDetector() {

            @Override
            public ContinueDecision globalVariableDetected(final String name, final Object value) {
                visitedVars.add(name);
                return visitedVars.size() < 2 ? ContinueDecision.CONTINUE : ContinueDecision.STOP;
            }
        };
    }

    private static TestVariableDetector variableDetector(final Set<String> visitedVars) {
        return new TestVariableDetector() {

            @Override
            public ContinueDecision variableDetected(final RobotVariable variable) {
                visitedVars.add(variable.getName());
                return ContinueDecision.CONTINUE;
            }
        };
    }

    private static TestVariableDetector limitedVariableDetector(final Set<String> visitedVars) {
        return new TestVariableDetector() {

            @Override
            public ContinueDecision variableDetected(final RobotVariable variable) {
                visitedVars.add(variable.getName());
                return visitedVars.size() < 2 ? ContinueDecision.CONTINUE : ContinueDecision.STOP;
            }
        };
    }

    private VariableDetector varFileVariableDetector(final Set<String> visitedVars) {
        return new TestVariableDetector() {

            @Override
            public ContinueDecision varFileVariableDetected(final ReferencedVariableFile file,
                    final String variableName, final Object value) {
                visitedVars.add(variableName);
                return ContinueDecision.CONTINUE;
            }
        };
    }

    private VariableDetector limitedVarFileVariableDetector(final Set<String> visitedVars) {
        return new TestVariableDetector() {

            @Override
            public ContinueDecision varFileVariableDetected(final ReferencedVariableFile file,
                    final String variableName, final Object value) {
                visitedVars.add(variableName);
                return visitedVars.size() < 1 ? ContinueDecision.CONTINUE : ContinueDecision.STOP;
            }
        };
    }

    private static class TestVariableDetector implements VariableDetector {

        @Override
        public ContinueDecision localVariableDetected(final RobotFileInternalElement element,
                final RobotToken variable) {
            return ContinueDecision.CONTINUE;
        }

        @Override
        public ContinueDecision variableDetected(final RobotVariable variable) {
            return ContinueDecision.CONTINUE;
        }

        @Override
        public ContinueDecision varFileVariableDetected(final ReferencedVariableFile file, final String variableName,
                final Object value) {
            return ContinueDecision.CONTINUE;
        }

        @Override
        public ContinueDecision globalVariableDetected(final String name, final Object value) {
            return ContinueDecision.CONTINUE;
        }
    }
}

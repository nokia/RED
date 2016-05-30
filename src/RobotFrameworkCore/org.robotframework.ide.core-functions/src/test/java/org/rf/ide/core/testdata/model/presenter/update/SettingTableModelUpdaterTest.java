/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.setting.DefaultTags;
import org.rf.ide.core.testdata.model.table.setting.ForceTags;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.model.table.setting.SuiteSetup;
import org.rf.ide.core.testdata.model.table.setting.SuiteTeardown;
import org.rf.ide.core.testdata.model.table.setting.TestSetup;
import org.rf.ide.core.testdata.model.table.setting.TestTeardown;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.rf.ide.core.testdata.model.table.setting.TestTimeout;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.write.NewRobotFileTestHelper;

public class SettingTableModelUpdaterTest {

    private static SettingTable settingTable;

    private static SettingTableModelUpdater modelUpdater;

    @BeforeClass
    public static void setup() {
        RobotFile model = NewRobotFileTestHelper.getModelFileToModify("2.9");
        model.includeSettingTableSection();
        settingTable = model.getSettingTable();

        modelUpdater = new SettingTableModelUpdater();
    }

    @Test
    public void testSuiteSetupCRUD() {
        final String keywordName = "setup";
        final List<String> settingArgs = newArrayList("arg1", "arg2");
        final String comment = "comment";
        final List<String> args = newArrayList(keywordName);
        args.addAll(settingArgs);

        final AModelElement<?> modelElement = modelUpdater.create(settingTable, SettingName.SUITE_SETUP.getName(),
                comment, args);

        assertTrue(modelElement.getModelType() == ModelType.SUITE_SETUP);
        SuiteSetup setting = (SuiteSetup) modelElement;
        checkSetting(setting.getKeywordName(), keywordName, setting.getArguments(), settingArgs, setting.getComment(),
                comment);

        final String newKeywordName = "new setup";
        final String newArg = "arg3";
        settingArgs.add(newArg);
        final String newComment = "new comment";

        modelUpdater.update(setting, 0, newKeywordName);
        modelUpdater.update(setting, 3, newArg);
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getKeywordName(), newKeywordName, setting.getArguments(), settingArgs,
                setting.getComment(), newComment);

        assertFalse(settingTable.getSuiteSetups().isEmpty());
        modelUpdater.remove(settingTable, setting);
        assertTrue(settingTable.getSuiteSetups().isEmpty());
    }

    @Test
    public void testSuiteTeardownCRUD() {
        final String keywordName = "teardown";
        final List<String> settingArgs = newArrayList("arg1", "arg2");
        final String comment = "comment";
        final List<String> args = newArrayList(keywordName);
        args.addAll(settingArgs);

        final AModelElement<?> modelElement = modelUpdater.create(settingTable, SettingName.SUITE_TEARDOWN.getName(),
                comment, args);

        assertTrue(modelElement.getModelType() == ModelType.SUITE_TEARDOWN);
        SuiteTeardown setting = (SuiteTeardown) modelElement;
        checkSetting(setting.getKeywordName(), keywordName, setting.getArguments(), settingArgs, setting.getComment(),
                comment);

        final String newKeywordName = "new teardown";
        final String newArg = "new arg2";
        settingArgs.remove(1);
        settingArgs.add(newArg);
        final String newComment = "new comment";

        modelUpdater.update(setting, 0, newKeywordName);
        modelUpdater.update(setting, 2, newArg);
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getKeywordName(), newKeywordName, setting.getArguments(), settingArgs,
                setting.getComment(), newComment);

        assertFalse(settingTable.getSuiteTeardowns().isEmpty());
        modelUpdater.remove(settingTable, setting);
        assertTrue(settingTable.getSuiteTeardowns().isEmpty());
    }

    @Test
    public void testTestSetupCRUD() {
        final String keywordName = "setup";
        final List<String> settingArgs = newArrayList("arg1", "arg2");
        final String comment = "comment";
        final List<String> args = newArrayList(keywordName);
        args.addAll(settingArgs);

        final AModelElement<?> modelElement = modelUpdater.create(settingTable, SettingName.TEST_SETUP.getName(),
                comment, args);

        assertTrue(modelElement.getModelType() == ModelType.SUITE_TEST_SETUP);
        TestSetup setting = (TestSetup) modelElement;
        checkSetting(setting.getKeywordName(), keywordName, setting.getArguments(), settingArgs, setting.getComment(),
                comment);

        final String newKeywordName = "new test setup";
        final String newArg = "new arg1";
        settingArgs.remove(0);
        settingArgs.add(0, newArg);
        final String newComment = "new comment";

        modelUpdater.update(setting, 0, newKeywordName);
        modelUpdater.update(setting, 1, newArg);
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getKeywordName(), newKeywordName, setting.getArguments(), settingArgs,
                setting.getComment(), newComment);

        assertFalse(settingTable.getTestSetups().isEmpty());
        modelUpdater.remove(settingTable, setting);
        assertTrue(settingTable.getTestSetups().isEmpty());
    }

    @Test
    public void testTestTeardownCRUD() {
        final String keywordName = "teardown";
        final List<String> settingArgs = newArrayList("arg1", "arg2");
        final String comment = "comment";
        final List<String> args = newArrayList(keywordName);
        args.addAll(settingArgs);

        final AModelElement<?> modelElement = modelUpdater.create(settingTable, SettingName.TEST_TEARDOWN.getName(),
                comment, args);

        assertTrue(modelElement.getModelType() == ModelType.SUITE_TEST_TEARDOWN);
        TestTeardown setting = (TestTeardown) modelElement;
        checkSetting(setting.getKeywordName(), keywordName, setting.getArguments(), settingArgs, setting.getComment(),
                comment);

        final String newKeywordName = "new test teardown";
        final String newArg1 = "";
        final String newArg2 = "new arg4";
        settingArgs.add(newArg1);
        settingArgs.add(newArg2);
        final String newComment = "new comment";

        modelUpdater.update(setting, 0, newKeywordName);
        modelUpdater.update(setting, 3, newArg1);
        modelUpdater.update(setting, 4, newArg2);
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getKeywordName(), newKeywordName, setting.getArguments(), settingArgs,
                setting.getComment(), newComment);

        assertFalse(settingTable.getTestTeardowns().isEmpty());
        modelUpdater.remove(settingTable, setting);
        assertTrue(settingTable.getTestTeardowns().isEmpty());
    }

    @Test
    public void testTestTemplateCRUD() {
        final String template = "template";
        final List<String> settingArgs = newArrayList("arg1", "arg2");
        final String comment = "comment";
        final List<String> args = newArrayList(template);
        args.addAll(settingArgs);

        final AModelElement<?> modelElement = modelUpdater.create(settingTable, SettingName.TEST_TEMPLATE.getName(),
                comment, args);

        assertTrue(modelElement.getModelType() == ModelType.SUITE_TEST_TEMPLATE);
        TestTemplate setting = (TestTemplate) modelElement;
        checkSetting(setting.getKeywordName(), template, setting.getUnexpectedTrashArguments(), settingArgs,
                setting.getComment(), comment);

        final String newKeywordName = "new template";
        final String newArg = "new arg2";
        settingArgs.remove(1);
        settingArgs.add(newArg);
        final String newComment = "new comment";

        modelUpdater.update(setting, 0, newKeywordName);
        modelUpdater.update(setting, 2, newArg);
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getKeywordName(), newKeywordName, setting.getUnexpectedTrashArguments(), settingArgs,
                setting.getComment(), newComment);

        assertFalse(settingTable.getTestTemplates().isEmpty());
        modelUpdater.remove(settingTable, setting);
        assertTrue(settingTable.getTestTemplates().isEmpty());
    }

    @Test
    public void testTestTimeoutCRUD() {
        final String timeout = "2 seconds";
        final List<String> settingArgs = newArrayList("arg1", "arg2");
        final String comment = "comment";
        final List<String> args = newArrayList(timeout);
        args.addAll(settingArgs);

        final AModelElement<?> modelElement = modelUpdater.create(settingTable, SettingName.TEST_TIMEOUT.getName(),
                comment, args);

        assertTrue(modelElement.getModelType() == ModelType.SUITE_TEST_TIMEOUT);
        TestTimeout setting = (TestTimeout) modelElement;
        checkSetting(setting.getTimeout(), timeout, setting.getMessageArguments(), settingArgs, setting.getComment(),
                comment);

        final String newTimeout = "4 seconds";
        final String newArg = "new arg2";
        settingArgs.remove(1);
        settingArgs.add(newArg);
        final String newComment = "new comment";

        modelUpdater.update(setting, 0, newTimeout);
        modelUpdater.update(setting, 2, newArg);
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getTimeout(), newTimeout, setting.getMessageArguments(), settingArgs, setting.getComment(),
                newComment);

        assertFalse(settingTable.getTestTimeouts().isEmpty());
        modelUpdater.remove(settingTable, setting);
        assertTrue(settingTable.getTestTimeouts().isEmpty());
    }

    @Test
    public void testForceTagsCRUD() {
        final List<String> tags = newArrayList("tag1", "tag2");
        final String comment = "comment";

        final AModelElement<?> modelElement = modelUpdater.create(settingTable, SettingName.FORCE_TAGS.getName(),
                comment, tags);

        assertTrue(modelElement.getModelType() == ModelType.FORCE_TAGS_SETTING);
        ForceTags setting = (ForceTags) modelElement;
        checkSetting(setting.getTags(), tags, setting.getComment(), comment);

        final List<String> newTags = newArrayList("tag3", "tag4", "tag5");
        final String newComment = "new comment";

        modelUpdater.update(setting, 0, newTags.get(0));
        modelUpdater.update(setting, 1, newTags.get(1));
        modelUpdater.update(setting, 2, newTags.get(2));
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getTags(), newTags, setting.getComment(), newComment);

        assertFalse(settingTable.getForceTags().isEmpty());
        modelUpdater.remove(settingTable, setting);
        assertTrue(settingTable.getForceTags().isEmpty());
    }

    @Test
    public void testDefaultTagsCRUD() {
        final List<String> tags = newArrayList("tag1", "tag2");
        final String comment = "comment";

        final AModelElement<?> modelElement = modelUpdater.create(settingTable, SettingName.DEFAULT_TAGS.getName(),
                comment, tags);

        assertTrue(modelElement.getModelType() == ModelType.DEFAULT_TAGS_SETTING);
        DefaultTags setting = (DefaultTags) modelElement;
        checkSetting(setting.getTags(), tags, setting.getComment(), comment);

        final List<String> newTags = newArrayList("tag1", "tag4");
        final String newComment = "new comment";

        modelUpdater.update(setting, 1, newTags.get(1));
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getTags(), newTags, setting.getComment(), newComment);

        assertFalse(settingTable.getDefaultTags().isEmpty());
        modelUpdater.remove(settingTable, setting);
        assertTrue(settingTable.getDefaultTags().isEmpty());
    }

    @Test
    public void testDocumentationCRUD() {
        final List<String> documentation = newArrayList("docPart1", "docPart2");
        final String comment = "comment";

        final AModelElement<?> modelElement = modelUpdater.create(settingTable, SettingName.DOCUMENTATION.getName(),
                comment, documentation);

        assertTrue(modelElement.getModelType() == ModelType.SUITE_DOCUMENTATION);
        SuiteDocumentation setting = (SuiteDocumentation) modelElement;
        checkSetting(setting.getDocumentationText(), documentation, setting.getComment(), comment);

        final List<String> newDocumentation = newArrayList("docPart3", "docPart4");
        final String newComment = "new comment";

        modelUpdater.update(setting, 0, newDocumentation.get(0));
        modelUpdater.update(setting, 1, newDocumentation.get(1));
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getDocumentationText(), newDocumentation, setting.getComment(), newComment);

        assertFalse(settingTable.getDocumentation().isEmpty());
        modelUpdater.remove(settingTable, setting);
        assertTrue(settingTable.getDocumentation().isEmpty());
    }

    @Test
    public void testLibraryImportCRUD() {
        final String libName = "lib";
        final List<String> settingArgs = newArrayList("arg1", "arg2");
        final String comment = "comment";
        final List<String> args = newArrayList(libName);
        args.addAll(settingArgs);

        final AModelElement<?> modelElement = modelUpdater.create(settingTable, SettingName.LIBRARY_IMPORT.getName(),
                comment, args);

        assertTrue(modelElement.getModelType() == ModelType.LIBRARY_IMPORT_SETTING);
        LibraryImport setting = (LibraryImport) modelElement;
        checkSetting(setting.getPathOrName(), libName, setting.getArguments(), settingArgs, setting.getComment(),
                comment);

        final String newLibName = "new lib";
        final List<String> newSettingArgs = newArrayList("arg3", "arg4");
        final String newComment = "new comment";

        modelUpdater.update(setting, 0, newLibName);
        modelUpdater.update(setting, 1, newSettingArgs.get(0));
        modelUpdater.update(setting, 2, newSettingArgs.get(1));
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getPathOrName(), newLibName, setting.getArguments(), newSettingArgs, setting.getComment(),
                newComment);

        assertTrue(settingTable.getImports().contains(setting));
        modelUpdater.remove(settingTable, setting);
        assertFalse(settingTable.getImports().contains(setting));
    }

    @Test
    public void testResourceImportCRUD() {
        final String resourceName = "resource";
        final List<String> settingArgs = newArrayList("arg1", "arg2");
        final String comment = "comment";
        final List<String> args = newArrayList(resourceName);
        args.addAll(settingArgs);

        final AModelElement<?> modelElement = modelUpdater.create(settingTable, SettingName.RESOURCE_IMPORT.getName(),
                comment, args);

        assertTrue(modelElement.getModelType() == ModelType.RESOURCE_IMPORT_SETTING);
        ResourceImport setting = (ResourceImport) modelElement;
        checkSetting(setting.getPathOrName(), resourceName, setting.getUnexpectedTrashArguments(), settingArgs,
                setting.getComment(), comment);

        final String newResourceName = "new resource";
        final List<String> newSettingArgs = newArrayList("arg3", "arg4");
        final String newComment = "new comment";

        modelUpdater.update(setting, 0, newResourceName);
        modelUpdater.update(setting, 1, newSettingArgs.get(0));
        modelUpdater.update(setting, 2, newSettingArgs.get(1));
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getPathOrName(), newResourceName, setting.getUnexpectedTrashArguments(), newSettingArgs,
                setting.getComment(), newComment);

        assertTrue(settingTable.getImports().contains(setting));
        modelUpdater.remove(settingTable, setting);
        assertFalse(settingTable.getImports().contains(setting));
    }

    @Test
    public void testVariablesImportCRUD() {
        final String varName = "variables.py";
        final List<String> settingArgs = newArrayList("arg1", "arg2");
        final String comment = "comment";
        final List<String> args = newArrayList(varName);
        args.addAll(settingArgs);

        final AModelElement<?> modelElement = modelUpdater.create(settingTable, SettingName.VARIABLES_IMPORT.getName(),
                comment, args);

        assertTrue(modelElement.getModelType() == ModelType.VARIABLES_IMPORT_SETTING);
        VariablesImport setting = (VariablesImport) modelElement;
        checkSetting(setting.getPathOrName(), varName, setting.getArguments(), settingArgs, setting.getComment(),
                comment);

        final String newVarName = "new_variables.py";
        final List<String> newSettingArgs = newArrayList("arg3", "");
        final String newComment = "new comment";

        modelUpdater.update(setting, 0, newVarName);
        modelUpdater.update(setting, 1, newSettingArgs.get(0));
        modelUpdater.update(setting, 2, "");
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getPathOrName(), newVarName, setting.getArguments(), newSettingArgs, setting.getComment(),
                newComment);

        assertTrue(settingTable.getImports().contains(setting));
        modelUpdater.remove(settingTable, setting);
        assertFalse(settingTable.getImports().contains(setting));
    }

    @Test
    public void testMetadataCRUD() {
        final String metadata = "data";
        final List<String> settingArgs = newArrayList("arg1", "arg2");
        final String comment = "comment";
        final List<String> args = newArrayList(metadata);
        args.addAll(settingArgs);

        final AModelElement<?> modelElement = modelUpdater.create(settingTable, SettingName.METADATA.getName(), comment,
                args);

        assertTrue(modelElement.getModelType() == ModelType.METADATA_SETTING);
        Metadata setting = (Metadata) modelElement;
        checkSetting(setting.getKey(), metadata, setting.getValues(), settingArgs, setting.getComment(), comment);

        final String newMetadata = "data";
        final List<String> newSettingArgs = newArrayList("arg3", "arg4", "arg5");
        final String newComment = "new comment";

        modelUpdater.update(setting, 0, newMetadata);
        modelUpdater.update(setting, 1, newSettingArgs.get(0));
        modelUpdater.update(setting, 2, newSettingArgs.get(1));
        modelUpdater.update(setting, 3, newSettingArgs.get(2));
        modelUpdater.updateComment(setting, newComment);

        checkSetting(setting.getKey(), newMetadata, setting.getValues(), newSettingArgs, setting.getComment(),
                newComment);

        assertFalse(settingTable.getMetadatas().isEmpty());
        modelUpdater.remove(settingTable, setting);
        assertTrue(settingTable.getMetadatas().isEmpty());
    }

    @Test
    public void testCreateWhenNoTableExists() {
        assertNull(modelUpdater.create(null, SettingName.METADATA.getName(), "", newArrayList("")));
    }

    @Test
    public void testCreateWithOtherTable() {
        RobotFile file = NewRobotFileTestHelper.getModelFileToModify("3.0");
        file.includeSettingTableSection();
        VariableTable variableTable = file.getVariableTable();
        assertNull(modelUpdater.create(variableTable, SettingName.METADATA.getName(), "", newArrayList("")));
    }

    @Test
    public void testCreateWithUnknownSetting() {
        assertNull(modelUpdater.create(settingTable, "Unknown", "", newArrayList("")));
    }

    private void checkSetting(final RobotToken actualKeywordName, final String expectedKeywordName,
            final List<RobotToken> actualArguments, final List<String> expectedArguments,
            final List<RobotToken> actualComments, final String expectedComment) {
        assertTrue(actualKeywordName.getText().equals(expectedKeywordName));
        checkSettingArguments(actualArguments, expectedArguments);
        checkSettingComment(actualComments, expectedComment);
    }

    private void checkSetting(final List<RobotToken> actualArguments, final List<String> expectedArguments,
            final List<RobotToken> actualComments, final String expectedComment) {
        checkSettingArguments(actualArguments, expectedArguments);
        checkSettingComment(actualComments, expectedComment);
    }

    private void checkSettingComment(final List<RobotToken> actualComments, final String expectedComment) {
        assertTrue(actualComments.get(0).getText().equals("#" + expectedComment));
    }

    private void checkSettingArguments(final List<RobotToken> actualArguments, final List<String> expectedArguments) {
        assertEquals(expectedArguments.size(), actualArguments.size());
        for (int i = 0; i < actualArguments.size(); i++) {
            assertTrue(actualArguments.get(i).getText().equals(expectedArguments.get(i)));
        }
    }

    enum SettingName {
        SUITE_SETUP("Suite Setup"),
        SUITE_TEARDOWN("Suite Teardown"),
        TEST_SETUP("Test Setup"),
        TEST_TEARDOWN("Test Teardown"),
        TEST_TEMPLATE("Test Template"),
        TEST_TIMEOUT("Test Timeout"),
        FORCE_TAGS("Force Tags"),
        DEFAULT_TAGS("Default Tags"),
        DOCUMENTATION("Documentation"),

        LIBRARY_IMPORT("Library"),
        RESOURCE_IMPORT("Resource"),
        VARIABLES_IMPORT("Variables"),

        METADATA("Metadata");

        private String name;

        private SettingName(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}

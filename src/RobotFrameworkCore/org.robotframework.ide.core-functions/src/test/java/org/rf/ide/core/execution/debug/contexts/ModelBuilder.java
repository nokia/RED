/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug.contexts;

import java.io.File;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.TaskTable;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.setting.SuiteSetup;
import org.rf.ide.core.testdata.model.table.setting.SuiteTeardown;
import org.rf.ide.core.testdata.model.table.setting.TaskTemplate;
import org.rf.ide.core.testdata.model.table.setting.TestSetup;
import org.rf.ide.core.testdata.model.table.setting.TestTeardown;
import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class ModelBuilder {

    public static interface TablesBuildingStep {

        SettingsTableBuildingStep withSettingsTable();

        VariablesTableBuildingStep withVariablesTable();

        TestCasesTableBuildingStep withTestCasesTable();

        TasksTableBuildingStep withTasksTable();

        KeywordsTableBuildingStep withKeywordsTable();
        
        RobotFile build();
    }

    public static interface SettingsTableBuildingStep {

        SettingsTableBuildingStep withTestSetup(String keyword, String... arguments);

        SettingsTableBuildingStep withTestTeardown(String keyword, String... arguments);

        SettingsTableBuildingStep withSuiteSetup(String keyword, String... arguments);

        SettingsTableBuildingStep withSuiteTeardown(String keyword, String... arguments);

        SettingsTableBuildingStep withTestTemplate(String keyword, String... arguments);

        SettingsTableBuildingStep withTaskTemplate(String keyword, String... arguments);

        VariablesTableBuildingStep withVariablesTable();

        TestCasesTableBuildingStep withTestCasesTable();

        TasksTableBuildingStep withTasksTable();

        KeywordsTableBuildingStep withKeywordsTable();

        RobotFile build();
    }

    public static interface VariablesTableBuildingStep {

        SettingsTableBuildingStep withSettingsTable();

        TestCasesTableBuildingStep withTestCasesTable();

        TasksTableBuildingStep withTasksTable();

        KeywordsTableBuildingStep withKeywordsTable();

        RobotFile build();
    }

    public static interface KeywordsTableBuildingStep {

        UserKeywordBuildingStep withUserKeyword(String keywordName);

        SettingsTableBuildingStep withSettingsTable();

        VariablesTableBuildingStep withVariablesTable();

        TestCasesTableBuildingStep withTestCasesTable();

        TasksTableBuildingStep withTasksTable();

        RobotFile build();
    }

    public static interface UserKeywordBuildingStep {

        UserKeywordBuildingStep withKeywordTeardown(String keyword, String... arguments);

        UserKeywordBuildingStep executable(String action, String... arguments);

        SettingsTableBuildingStep withSettingsTable();

        VariablesTableBuildingStep withVariablesTable();

        TestCasesTableBuildingStep withTestCasesTable();

        TasksTableBuildingStep withTasksTable();

        RobotFile build();
    }

    public static interface TestCasesTableBuildingStep {

        TestCaseBuildingStep withTestCase(String testName);

        SettingsTableBuildingStep withSettingsTable();

        VariablesTableBuildingStep withVariablesTable();

        KeywordsTableBuildingStep withKeywordsTable();

        TasksTableBuildingStep withTasksTable();

        RobotFile build();
    }

    public static interface TasksTableBuildingStep {

        TaskBuildingStep withTask(String taskName);

        TestCasesTableBuildingStep withTestCasesTable();

        SettingsTableBuildingStep withSettingsTable();

        VariablesTableBuildingStep withVariablesTable();

        KeywordsTableBuildingStep withKeywordsTable();

        RobotFile build();
    }

    public static interface TaskBuildingStep {

        TaskBuildingStep withTemplate(String keyword, String... arguments);

        TestCasesTableBuildingStep withTestCasesTable();

        SettingsTableBuildingStep withSettingsTable();

        VariablesTableBuildingStep withVariablesTable();

        KeywordsTableBuildingStep withKeywordsTable();

        RobotFile build();
    }

    public static interface TestCaseBuildingStep {

        TestCaseBuildingStep withTestSetup(String keyword, String... arguments);

        TestCaseBuildingStep withTestTeardown(String keyword, String... arguments);

        TestCaseBuildingStep withTemplate(String keyword, String... arguments);

        TestCaseBuildingStep executable(String action, String... arguments);

        TestCaseBuildingStep executableEndTerminatedLoop(String action, String... arguments);

        SettingsTableBuildingStep withSettingsTable();

        VariablesTableBuildingStep withVariablesTable();

        KeywordsTableBuildingStep withKeywordsTable();

        TasksTableBuildingStep withTasksTable();

        RobotFile build();
    }

    private static class TableBuilder implements TablesBuildingStep {

        private final RobotFile robotFile;

        TableBuilder(final RobotFile robotFile) {
            this.robotFile = robotFile;
        }

        @Override
        public SettingsTableBuildingStep withSettingsTable() {
            robotFile.includeSettingTableSection();
            return new SettingsTableBuilder(this, robotFile.getSettingTable());
        }

        @Override
        public VariablesTableBuildingStep withVariablesTable() {
            robotFile.includeVariableTableSection();
            return new VariablesTableBuilder(this, robotFile.getVariableTable());
        }

        @Override
        public TestCasesTableBuildingStep withTestCasesTable() {
            robotFile.includeTestCaseTableSection();
            return new TestCasesTableBuilder(this, robotFile.getTestCaseTable());
        }

        @Override
        public TasksTableBuildingStep withTasksTable() {
            robotFile.includeTaskTableSection();
            return new TasksTableBuilder(this, robotFile.getTasksTable());
        }

        @Override
        public KeywordsTableBuildingStep withKeywordsTable() {
            robotFile.includeKeywordTableSection();
            return new KeywordsTableBuilder(this, robotFile.getKeywordTable());
        }

        @Override
        public RobotFile build() {
            return robotFile;
        }
    }

    private static class SettingsTableBuilder implements SettingsTableBuildingStep {

        private final TableBuilder tableBuilder;

        private final SettingTable settingsTable;

        SettingsTableBuilder(final TableBuilder tableBuilder, final SettingTable settingsTable) {
            this.tableBuilder = tableBuilder;
            this.settingsTable = settingsTable;
        }

        @Override
        public SettingsTableBuildingStep withTestSetup(final String keyword, final String... arguments) {
            final TestSetup testSetup = new TestSetup(RobotToken.create("Test Setup"));
            if (keyword != null) {
                testSetup.setKeywordName(RobotToken.create(keyword));
            }
            for (final String arg : arguments) {
                testSetup.addArgument(RobotToken.create(arg));
            }
            settingsTable.addTestSetup(testSetup);
            return this;
        }

        @Override
        public SettingsTableBuildingStep withTestTeardown(final String keyword, final String... arguments) {
            final TestTeardown testTeardown = new TestTeardown(RobotToken.create("Test Teardown"));
            if (keyword != null) {
                testTeardown.setKeywordName(RobotToken.create(keyword));
            }
            for (final String arg : arguments) {
                testTeardown.addArgument(RobotToken.create(arg));
            }
            settingsTable.addTestTeardown(testTeardown);
            return this;
        }

        @Override
        public SettingsTableBuildingStep withSuiteSetup(final String keyword, final String... arguments) {
            final SuiteSetup suiteSetup = new SuiteSetup(RobotToken.create("Suite Setup"));
            if (keyword != null) {
                suiteSetup.setKeywordName(RobotToken.create(keyword));
            }
            for (final String arg : arguments) {
                suiteSetup.addArgument(RobotToken.create(arg));
            }
            settingsTable.addSuiteSetup(suiteSetup);
            return this;
        }

        @Override
        public SettingsTableBuildingStep withSuiteTeardown(final String keyword, final String... arguments) {
            final SuiteTeardown suiteTeardown = new SuiteTeardown(RobotToken.create("Suite Teardown"));
            if (keyword != null) {
                suiteTeardown.setKeywordName(RobotToken.create(keyword));
            }
            for (final String arg : arguments) {
                suiteTeardown.addArgument(RobotToken.create(arg));
            }
            settingsTable.addSuiteTeardown(suiteTeardown);
            return this;
        }

        @Override
        public SettingsTableBuildingStep withTestTemplate(final String keyword, final String... arguments) {
            final TestTemplate testTemplate = new TestTemplate(RobotToken.create("Test Template"));
            if (keyword != null) {
                testTemplate.setKeywordName(RobotToken.create(keyword));
            }
            for (final String arg : arguments) {
                testTemplate.addUnexpectedTrashArgument(RobotToken.create(arg));
            }
            settingsTable.addTestTemplate(testTemplate);
            return this;
        }

        @Override
        public SettingsTableBuildingStep withTaskTemplate(final String keyword, final String... arguments) {
            final TaskTemplate taskTemplate = new TaskTemplate(RobotToken.create("Task Template"));
            if (keyword != null) {
                taskTemplate.setKeywordName(RobotToken.create(keyword));
            }
            for (final String arg : arguments) {
                taskTemplate.addUnexpectedTrashArgument(RobotToken.create(arg));
            }
            settingsTable.addTaskTemplate(taskTemplate);
            return this;
        }

        @Override
        public VariablesTableBuildingStep withVariablesTable() {
            return tableBuilder.withVariablesTable();
        }

        @Override
        public TestCasesTableBuildingStep withTestCasesTable() {
            return tableBuilder.withTestCasesTable();
        }

        @Override
        public TasksTableBuildingStep withTasksTable() {
            return tableBuilder.withTasksTable();
        }

        @Override
        public KeywordsTableBuildingStep withKeywordsTable() {
            return tableBuilder.withKeywordsTable();
        }

        @Override
        public RobotFile build() {
            return tableBuilder.build();
        }
    }

    private static class VariablesTableBuilder implements VariablesTableBuildingStep {

        private final TableBuilder tableBuilder;

        private final VariableTable variableTable;

        VariablesTableBuilder(final TableBuilder tableBuilder, final VariableTable variableTable) {
            this.tableBuilder = tableBuilder;
            this.variableTable = variableTable;
        }

        @Override
        public SettingsTableBuildingStep withSettingsTable() {
            return tableBuilder.withSettingsTable();
        }

        @Override
        public TestCasesTableBuildingStep withTestCasesTable() {
            return tableBuilder.withTestCasesTable();
        }

        @Override
        public TasksTableBuildingStep withTasksTable() {
            return tableBuilder.withTasksTable();
        }

        @Override
        public KeywordsTableBuildingStep withKeywordsTable() {
            return tableBuilder.withKeywordsTable();
        }

        @Override
        public RobotFile build() {
            return tableBuilder.build();
        }
    }

    private static class TestCasesTableBuilder implements TestCasesTableBuildingStep {

        private final TableBuilder tableBuilder;

        private final TestCaseTable testCaseTable;

        TestCasesTableBuilder(final TableBuilder tableBuilder, final TestCaseTable testCaseTable) {
            this.tableBuilder = tableBuilder;
            this.testCaseTable = testCaseTable;
        }

        @Override
        public TestCaseBuildingStep withTestCase(final String testName) {
            final TestCase test = new TestCase(RobotToken.create(testName));
            testCaseTable.addTest(test);
            return new TestCaseBuilder(this, test);
        }

        @Override
        public TasksTableBuildingStep withTasksTable() {
            return tableBuilder.withTasksTable();
        }

        @Override
        public SettingsTableBuildingStep withSettingsTable() {
            return tableBuilder.withSettingsTable();
        }

        @Override
        public VariablesTableBuildingStep withVariablesTable() {
            return tableBuilder.withVariablesTable();
        }

        @Override
        public KeywordsTableBuildingStep withKeywordsTable() {
            return tableBuilder.withKeywordsTable();
        }

        @Override
        public RobotFile build() {
            return tableBuilder.build();
        }
    }

    private static class TestCaseBuilder implements TestCaseBuildingStep {

        private final TestCasesTableBuilder testsTableBuilder;

        private final TestCase test;

        public TestCaseBuilder(final TestCasesTableBuilder testsTableBuilder, final TestCase test) {
            this.testsTableBuilder = testsTableBuilder;
            this.test = test;
        }

        @Override
        public TestCaseBuildingStep withTestSetup(final String keyword, final String... arguments) {
            final LocalSetting<TestCase> setup = new LocalSetting<>(ModelType.TEST_CASE_SETUP,
                    RobotToken.create("[Setup]"));
            if (keyword != null) {
                setup.addToken(keyword);
            }
            for (final String arg : arguments) {
                setup.addToken(arg);
            }
            test.addElement(setup);
            return this;
        }

        @Override
        public TestCaseBuildingStep withTestTeardown(final String keyword, final String... arguments) {
            final LocalSetting<TestCase> teardown = new LocalSetting<>(ModelType.TEST_CASE_TEARDOWN,
                    RobotToken.create("[Teardown]"));
            if (keyword != null) {
                teardown.addToken(keyword);
            }
            for (final String arg : arguments) {
                teardown.addToken(arg);
            }
            test.addElement(teardown);
            return this;
        }

        @Override
        public TestCaseBuildingStep withTemplate(final String keyword, final String... arguments) {
            final LocalSetting<TestCase> template = new LocalSetting<>(ModelType.TEST_CASE_TEMPLATE,
                    RobotToken.create("[Template]"));
            if (keyword != null) {
                template.addToken(keyword);
            }
            for (final String arg : arguments) {
                template.addToken(arg);
            }
            test.addElement(template);
            return this;
        }

        @Override
        public TestCaseBuildingStep executable(final String action, final String... arguments) {
            final RobotExecutableRow<TestCase> row = new RobotExecutableRow<>();
            row.setAction(RobotToken.create(action));
            final boolean isForLoop = action.equals("FOR") || action.equalsIgnoreCase(":for");
            if (isForLoop) {
                row.getAction().getTypes().add(RobotTokenType.FOR_TOKEN);
            }
            if (action.equals("END")) {
                row.getAction().getTypes().add(RobotTokenType.FOR_END_TOKEN);
            }
            for (final String arg : arguments) {
                final RobotToken token = RobotToken.create(arg);
                if (isForLoop && (arg.equals("IN") || arg.equals("IN RANGE") || arg.equals("IN ZIP")
                        || arg.equals("IN ENUMERATE"))) {
                    token.setType(RobotTokenType.IN_TOKEN);
                }
                row.addArgument(token);
            }
            test.addElement(row);
            return this;
        }

        @Override
        public TestCaseBuildingStep executableEndTerminatedLoop(final String action, final String... arguments) {
            final RobotExecutableRow<TestCase> row = new RobotExecutableRow<>();
            row.setAction(RobotToken.create(""));
            row.getAction().getTypes().add(RobotTokenType.FOR_WITH_END_CONTINUATION);

            row.addArgument(RobotToken.create(action));
            for (final String arg : arguments) {
                row.addArgument(RobotToken.create(arg));
            }
            test.addElement(row);
            return this;
        }

        @Override
        public TasksTableBuildingStep withTasksTable() {
            return testsTableBuilder.withTasksTable();
        }

        @Override
        public SettingsTableBuildingStep withSettingsTable() {
            return testsTableBuilder.withSettingsTable();
        }

        @Override
        public VariablesTableBuildingStep withVariablesTable() {
            return testsTableBuilder.withVariablesTable();
        }

        @Override
        public KeywordsTableBuildingStep withKeywordsTable() {
            return testsTableBuilder.withKeywordsTable();
        }

        @Override
        public RobotFile build() {
            return testsTableBuilder.build();
        }
    }

    private static class TasksTableBuilder implements TasksTableBuildingStep {

        private final TableBuilder tableBuilder;

        private final TaskTable tasksTable;

        public TasksTableBuilder(final TableBuilder tableBuilder, final TaskTable tasksTable) {
            this.tableBuilder = tableBuilder;
            this.tasksTable = tasksTable;
        }

        @Override
        public TaskBuildingStep withTask(final String taskName) {
            final Task task = new Task(RobotToken.create(taskName));
            tasksTable.addTask(task);
            return new TaskBuilder(this, task);
        }

        @Override
        public KeywordsTableBuildingStep withKeywordsTable() {
            return tableBuilder.withKeywordsTable();
        }

        @Override
        public SettingsTableBuildingStep withSettingsTable() {
            return tableBuilder.withSettingsTable();
        }

        @Override
        public VariablesTableBuildingStep withVariablesTable() {
            return tableBuilder.withVariablesTable();
        }

        @Override
        public TestCasesTableBuildingStep withTestCasesTable() {
            return tableBuilder.withTestCasesTable();
        }

        @Override
        public RobotFile build() {
            return tableBuilder.build();
        }
    }

    private static class TaskBuilder implements TaskBuildingStep {

        private final TasksTableBuilder tasksTableBuilder;

        private final Task task;

        public TaskBuilder(final TasksTableBuilder tasksTableBuilder, final Task task) {
            this.tasksTableBuilder = tasksTableBuilder;
            this.task = task;
        }

        @Override
        public TaskBuilder withTemplate(final String keyword, final String... arguments) {
            final LocalSetting<TestCase> template = new LocalSetting<>(ModelType.TASK_TEMPLATE,
                    RobotToken.create("[Template]"));
            if (keyword != null) {
                template.addToken(keyword);
            }
            for (final String arg : arguments) {
                template.addToken(arg);
            }
            task.addElement(template);
            return this;
        }

        @Override
        public TestCasesTableBuildingStep withTestCasesTable() {
            return tasksTableBuilder.withTestCasesTable();
        }

        @Override
        public SettingsTableBuildingStep withSettingsTable() {
            return tasksTableBuilder.withSettingsTable();
        }

        @Override
        public VariablesTableBuildingStep withVariablesTable() {
            return tasksTableBuilder.withVariablesTable();
        }

        @Override
        public KeywordsTableBuildingStep withKeywordsTable() {
            return tasksTableBuilder.withKeywordsTable();
        }

        @Override
        public RobotFile build() {
            return tasksTableBuilder.build();
        }

    }

    private static class KeywordsTableBuilder implements KeywordsTableBuildingStep {

        private final TableBuilder tableBuilder;

        private final KeywordTable keywordTable;

        KeywordsTableBuilder(final TableBuilder tableBuilder, final KeywordTable keywordTable) {
            this.tableBuilder = tableBuilder;
            this.keywordTable = keywordTable;
        }

        @Override
        public UserKeywordBuildingStep withUserKeyword(final String keywordName) {
            final UserKeyword keyword = new UserKeyword(RobotToken.create(keywordName));
            keywordTable.addKeyword(keyword);
            return new UserKeywordBuilder(this, keyword);
        }

        @Override
        public TasksTableBuildingStep withTasksTable() {
            return tableBuilder.withTasksTable();
        }

        @Override
        public SettingsTableBuildingStep withSettingsTable() {
            return tableBuilder.withSettingsTable();
        }

        @Override
        public VariablesTableBuildingStep withVariablesTable() {
            return tableBuilder.withVariablesTable();
        }

        @Override
        public TestCasesTableBuildingStep withTestCasesTable() {
            return tableBuilder.withTestCasesTable();
        }

        @Override
        public RobotFile build() {
            return tableBuilder.build();
        }
    }

    public static TablesBuildingStep modelForFile() {
        return modelForFile("file.robot");
    }

    public static TablesBuildingStep modelForFile(final RobotVersion version) {
        return modelForFile("file.robot", version);
    }

    public static TablesBuildingStep modelForFile(final String filename) {
        return modelForFile(filename, RobotVersion.from("3.0.0"));
    }

    public static TablesBuildingStep modelForFile(final String filename, final RobotVersion version) {
        final RobotFileOutput rfo = new RobotFileOutput(version);
        rfo.setProcessedFile(new File(filename));
        final RobotFile robotFile = rfo.getFileModel();
        robotFile.excludeTable(RobotTokenType.KEYWORDS_TABLE_HEADER);
        robotFile.excludeTable(RobotTokenType.TEST_CASES_TABLE_HEADER);
        robotFile.excludeTable(RobotTokenType.TASKS_TABLE_HEADER);
        robotFile.excludeTable(RobotTokenType.SETTINGS_TABLE_HEADER);
        robotFile.excludeTable(RobotTokenType.VARIABLES_TABLE_HEADER);
        
        return new TableBuilder(robotFile);
    }

    private static class UserKeywordBuilder implements UserKeywordBuildingStep {

        private final KeywordsTableBuilder keywordsTableBuilder;

        private final UserKeyword keyword;

        public UserKeywordBuilder(final KeywordsTableBuilder keywordsTableBuilder, final UserKeyword keyword) {
            this.keywordsTableBuilder = keywordsTableBuilder;
            this.keyword = keyword;
        }

        @Override
        public UserKeywordBuildingStep withKeywordTeardown(final String keyword, final String... arguments) {
            final LocalSetting<UserKeyword> teardown = new LocalSetting<>(ModelType.USER_KEYWORD_TEARDOWN,
                    RobotToken.create("[Teardown]"));
            if (keyword != null) {
                teardown.addToken(keyword);
            }
            for (final String arg : arguments) {
                teardown.addToken(arg);
            }
            this.keyword.addElement(teardown);
            return this;
        }

        @Override
        public UserKeywordBuildingStep executable(final String action, final String... arguments) {
            final RobotExecutableRow<UserKeyword> row = new RobotExecutableRow<>();
            row.setAction(RobotToken.create(action));
            final boolean isForLoop = action.equalsIgnoreCase("for") || action.equalsIgnoreCase(":for");
            if (isForLoop) {
                row.getAction().getTypes().add(RobotTokenType.FOR_TOKEN);
            }
            for (final String arg : arguments) {
                final RobotToken token = RobotToken.create(arg);
                if (isForLoop && (arg.equals("IN") || arg.equals("IN RANGE") || arg.equals("IN ZIP")
                        || arg.equals("IN ENUMERATE"))) {
                    token.setType(RobotTokenType.IN_TOKEN);
                }
                row.addArgument(token);
            }
            keyword.addElement(row);
            return this;
        }

        @Override
        public SettingsTableBuildingStep withSettingsTable() {
            return keywordsTableBuilder.withSettingsTable();
        }

        @Override
        public VariablesTableBuildingStep withVariablesTable() {
            return keywordsTableBuilder.withVariablesTable();
        }

        @Override
        public TestCasesTableBuildingStep withTestCasesTable() {
            return keywordsTableBuilder.withTestCasesTable();
        }

        @Override
        public TasksTableBuildingStep withTasksTable() {
            return keywordsTableBuilder.withTasksTable();
        }

        @Override
        public RobotFile build() {
            return keywordsTableBuilder.build();
        }
    }
}

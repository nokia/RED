/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug.contexts;

import java.io.File;

import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTeardown;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.setting.SuiteSetup;
import org.rf.ide.core.testdata.model.table.setting.SuiteTeardown;
import org.rf.ide.core.testdata.model.table.setting.TestSetup;
import org.rf.ide.core.testdata.model.table.setting.TestTeardown;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseSetup;
import org.rf.ide.core.testdata.model.table.testcases.TestCaseTeardown;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class ModelBuilder {

    public static interface TablesBuildingStep {

        SettingsTableBuildingStep withSettingsTable();

        VariablesTableBuildingStep withVariablesTable();

        TestCasesTableBuildingStep withTestCasesTable();

        KeywordsTableBuildingStep withKeywordsTable();
        
        RobotFile build();
    }

    public static interface SettingsTableBuildingStep {

        SettingsTableBuildingStep withTestSetup(String keyword, String... arguments);

        SettingsTableBuildingStep withTestTeardown(String keyword, String... arguments);

        SettingsTableBuildingStep withSuiteSetup(String keyword, String... arguments);

        SettingsTableBuildingStep withSuiteTeardown(String keyword, String... arguments);

        VariablesTableBuildingStep withVariablesTable();

        TestCasesTableBuildingStep withTestCasesTable();

        KeywordsTableBuildingStep withKeywordsTable();

        RobotFile build();
    }

    public static interface VariablesTableBuildingStep {

        SettingsTableBuildingStep withSettingsTable();

        TestCasesTableBuildingStep withTestCasesTable();

        KeywordsTableBuildingStep withKeywordsTable();

        RobotFile build();
    }

    public static interface KeywordsTableBuildingStep {

        UserKeywordBuildingStep withUserKeyword(String keywordName);

        SettingsTableBuildingStep withSettingsTable();

        VariablesTableBuildingStep withVariablesTable();

        TestCasesTableBuildingStep withTestCasesTable();

        RobotFile build();
    }

    public static interface UserKeywordBuildingStep {

        UserKeywordBuildingStep withKeywordTeardown(String keyword, String... arguments);

        UserKeywordBuildingStep executable(String action, String... arguments);

        SettingsTableBuildingStep withSettingsTable();

        VariablesTableBuildingStep withVariablesTable();

        TestCasesTableBuildingStep withTestCasesTable();

        RobotFile build();
    }

    public static interface TestCasesTableBuildingStep {

        TestCaseBuildingStep withTestCase(String testName);

        SettingsTableBuildingStep withSettingsTable();

        VariablesTableBuildingStep withVariablesTable();

        KeywordsTableBuildingStep withKeywordsTable();

        RobotFile build();
    }

    public static interface TestCaseBuildingStep {

        TestCaseBuildingStep withTestSetup(String keyword, String... arguments);

        TestCaseBuildingStep withTestTeardown(String keyword, String... arguments);

        TestCaseBuildingStep executable(String action, String... arguments);

        SettingsTableBuildingStep withSettingsTable();

        VariablesTableBuildingStep withVariablesTable();

        KeywordsTableBuildingStep withKeywordsTable();

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
        public VariablesTableBuildingStep withVariablesTable() {
            return tableBuilder.withVariablesTable();
        }

        @Override
        public TestCasesTableBuildingStep withTestCasesTable() {
            return tableBuilder.withTestCasesTable();
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
            final TestCaseSetup setup = new TestCaseSetup(RobotToken.create("[Setup]"));
            if (keyword != null) {
                setup.setKeywordName(keyword);
            }
            for (final String arg : arguments) {
                setup.addArgument(arg);
            }
            test.addElement(setup);
            return this;
        }

        @Override
        public TestCaseBuildingStep withTestTeardown(final String keyword, final String... arguments) {
            final TestCaseTeardown teardown = new TestCaseTeardown(RobotToken.create("[Teardown]"));
            if (keyword != null) {
                teardown.setKeywordName(keyword);
            }
            for (final String arg : arguments) {
                teardown.addArgument(arg);
            }
            test.addElement(teardown);
            return this;
        }

        @Override
        public TestCaseBuildingStep executable(final String action, final String... arguments) {
            final RobotExecutableRow<TestCase> row = new RobotExecutableRow<>();
            row.setAction(RobotToken.create(action));
            for (final String arg : arguments) {
                row.addArgument(RobotToken.create(arg));
            }
            test.addElement(row);
            return this;
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

    public static TablesBuildingStep modelForFile(final String filename) {
        final RobotFileOutput rfo = new RobotFileOutput(RobotVersion.from("3.0.0"));
        rfo.setProcessedFile(new File(filename));
        final RobotFile robotFile = rfo.getFileModel();
        robotFile.excludeKeywordTableSection();
        robotFile.excludeTestCaseTableSection();
        robotFile.excludeSettingTableSection();
        robotFile.excludeVariableTableSection();
        
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
            final KeywordTeardown teardown = new KeywordTeardown(RobotToken.create("[Teardown]"));
            if (keyword != null) {
                teardown.setKeywordName(keyword);
            }
            for (final String arg : arguments) {
                teardown.addArgument(arg);
            }
            this.keyword.addElement(teardown);
            return this;
        }

        @Override
        public UserKeywordBuildingStep executable(final String action, final String... arguments) {
            final RobotExecutableRow<UserKeyword> row = new RobotExecutableRow<>();
            row.setAction(RobotToken.create(action));
            for (final String arg : arguments) {
                row.addArgument(RobotToken.create(arg));
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
        public RobotFile build() {
            return keywordsTableBuilder.build();
        }
    }
}

package org.robotframework.ide.core.testData.model.objectCreator;

import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.VariableTable;
import org.robotframework.ide.core.testData.model.table.setting.DefaultTags;
import org.robotframework.ide.core.testData.model.table.setting.ForceTags;
import org.robotframework.ide.core.testData.model.table.setting.LibraryAlias;
import org.robotframework.ide.core.testData.model.table.setting.LibraryImport;
import org.robotframework.ide.core.testData.model.table.setting.Metadata;
import org.robotframework.ide.core.testData.model.table.setting.ResourceImport;
import org.robotframework.ide.core.testData.model.table.setting.SuiteDocumentation;
import org.robotframework.ide.core.testData.model.table.setting.SuiteSetup;
import org.robotframework.ide.core.testData.model.table.setting.SuiteTeardown;
import org.robotframework.ide.core.testData.model.table.setting.TestSetup;
import org.robotframework.ide.core.testData.model.table.setting.TestTeardown;
import org.robotframework.ide.core.testData.model.table.setting.TestTemplate;
import org.robotframework.ide.core.testData.model.table.setting.TestTimeout;
import org.robotframework.ide.core.testData.model.table.setting.UnknownSetting;
import org.robotframework.ide.core.testData.model.table.setting.VariablesImport;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseSetup;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTags;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTeardown;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTemplate;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTimeout;
import org.robotframework.ide.core.testData.model.table.testCases.TestDocumentation;
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordArguments;
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordDocumentation;
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordReturn;
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordTags;
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordTeardown;
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordTimeout;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;
import org.robotframework.ide.core.testData.model.table.variables.DictionaryVariable;
import org.robotframework.ide.core.testData.model.table.variables.ListVariable;
import org.robotframework.ide.core.testData.model.table.variables.ScalarVariable;
import org.robotframework.ide.core.testData.model.table.variables.UnknownVariable;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class RobotModelObjectCreator implements IRobotModelObjectCreator {

    @Override
    public RobotExecutableRow createRobotExecutableRow() {
        return new RobotExecutableRow();
    }


    @Override
    public KeywordTimeout createKeywordTimeout(final RobotToken declaration) {
        return new KeywordTimeout(declaration);
    }


    @Override
    public KeywordTeardown createKeywordTeardown(final RobotToken declaration) {
        return new KeywordTeardown(declaration);
    }


    @Override
    public KeywordReturn createKeywordReturn(final RobotToken declaration) {
        return new KeywordReturn(declaration);
    }


    @Override
    public KeywordArguments createKeywordArguments(final RobotToken declaration) {
        return new KeywordArguments(declaration);
    }


    @Override
    public KeywordTags createKeywordTags(final RobotToken declaration) {
        return new KeywordTags(declaration);
    }


    @Override
    public KeywordDocumentation createKeywordDocumentation(
            final RobotToken declaration) {
        return new KeywordDocumentation(declaration);
    }


    @Override
    public UserKeyword createUserKeyword(final RobotToken declaration) {
        return new UserKeyword(declaration);
    }


    @Override
    public TestDocumentation createTestDocumentation(
            final RobotToken declaration) {
        return new TestDocumentation(declaration);
    }


    @Override
    public TestCaseTimeout createTestCaseTimeout(final RobotToken declaration) {
        return new TestCaseTimeout(declaration);
    }


    @Override
    public TestCaseTemplate createTestCaseTemplate(final RobotToken declaration) {
        return new TestCaseTemplate(declaration);
    }


    @Override
    public TestCaseTeardown createTestCaseTeardown(final RobotToken declaration) {
        return new TestCaseTeardown(declaration);
    }


    @Override
    public TestCaseTags createTestCaseTags(final RobotToken declaration) {
        return new TestCaseTags(declaration);
    }


    @Override
    public TestCaseSetup createTestCaseSetup(final RobotToken declaration) {
        return new TestCaseSetup(declaration);
    }


    @Override
    public TestCase createTestCase(final RobotToken declaration) {
        return new TestCase(declaration);
    }


    @Override
    public UnknownVariable createUnknownVariable(final String name,
            final RobotToken declaration) {
        return new UnknownVariable(name, declaration);
    }


    @Override
    public DictionaryVariable createDictionaryVariable(final String name,
            final RobotToken declaration) {
        return new DictionaryVariable(name, declaration);
    }


    @Override
    public ListVariable createListVariable(final String name,
            final RobotToken declaration) {
        return new ListVariable(name, declaration);
    }


    @Override
    public ScalarVariable createScalarVariable(final String name,
            final RobotToken declaration) {
        return new ScalarVariable(name, declaration);
    }


    @Override
    public UnknownSetting createUnknownSetting(final RobotToken declaration) {
        return new UnknownSetting(declaration);
    }


    @Override
    public TestTimeout createTestTimeout(final RobotToken declaration) {
        return new TestTimeout(declaration);
    }


    @Override
    public TestTemplate createTestTemplate(final RobotToken declaration) {
        return new TestTemplate(declaration);
    }


    @Override
    public TestTeardown createTestTeardown(final RobotToken declaration) {
        return new TestTeardown(declaration);
    }


    @Override
    public TestSetup createTestSetup(final RobotToken declaration) {
        return new TestSetup(declaration);
    }


    @Override
    public DefaultTags createDefaultTags(final RobotToken declaration) {
        return new DefaultTags(declaration);
    }


    @Override
    public ForceTags createForceTags(final RobotToken declaration) {
        return new ForceTags(declaration);
    }


    @Override
    public SuiteTeardown createSuiteTeardown(final RobotToken declaration) {
        return new SuiteTeardown(declaration);
    }


    @Override
    public SuiteSetup createSuiteSetup(final RobotToken declaration) {
        return new SuiteSetup(declaration);
    }


    @Override
    public Metadata createMetadata(final RobotToken declaration) {
        return new Metadata(declaration);
    }


    @Override
    public SuiteDocumentation createSuiteDocumentation(
            final RobotToken declaration) {
        return new SuiteDocumentation(declaration);
    }


    @Override
    public ResourceImport createResourceImport(final RobotToken declaration) {
        return new ResourceImport(declaration);
    }


    @Override
    public VariablesImport createVariablesImport(final RobotToken declaration) {
        return new VariablesImport(declaration);
    }


    @Override
    public LibraryAlias createLibraryAlias(final RobotToken declaration) {
        return new LibraryAlias(declaration);
    }


    @Override
    public LibraryImport createLibraryImport(final RobotToken declaration) {
        return new LibraryImport(declaration);
    }


    @Override
    public TableHeader createTableHeader(final RobotToken tableHeaderToken) {
        return new TableHeader(tableHeaderToken);
    }


    @Override
    public KeywordTable createKeywordTable() {
        return new KeywordTable();
    }


    @Override
    public TestCaseTable createTestCaseTable() {
        return new TestCaseTable();
    }


    @Override
    public VariableTable createVariableTable() {
        return new VariableTable();
    }


    @Override
    public SettingTable createSettingTable() {
        return new SettingTable();
    }


    @Override
    public RobotLine createRobotLine(final int lineNumber) {
        return new RobotLine(lineNumber);
    }


    @Override
    public RobotFileOutput createRobotFileOutput() {
        RobotFileOutput rfo = new RobotFileOutput(this);

        return rfo;
    }


    public static RobotModelObjectCreator newInstance() {
        return new RobotModelObjectCreator();
    }


    private RobotModelObjectCreator() {
    }
}

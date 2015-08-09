package org.robotframework.ide.core.testData.model.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.ARobotSectionTable;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.setting.AImported;
import org.robotframework.ide.core.testData.model.table.setting.DefaultTags;
import org.robotframework.ide.core.testData.model.table.setting.ForceTags;
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
import org.robotframework.ide.core.testData.model.table.setting.VariablesImport;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseSetup;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTags;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTeardown;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTemplate;
import org.robotframework.ide.core.testData.model.table.testCases.TestCaseTimeout;
import org.robotframework.ide.core.testData.model.table.testCases.TestDocumentation;
import org.robotframework.ide.core.testData.model.table.variables.IVariableHolder;
import org.robotframework.ide.core.testData.model.table.variables.ListVariable;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class HashCommentMapper implements IParsingMapper {

    private final ElementsUtility utility;


    public HashCommentMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        boolean addToStack = false;
        if (rt.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
            rt.setType(RobotTokenType.START_HASH_COMMENT);
            addToStack = true;
        } else {
            rt.setType(RobotTokenType.COMMENT_CONTINUE);
        }

        ParsingState commentHolder = findNearestCommentDeclaringModelElement(processingState);
        RobotFile fileModel = robotFileOutput.getFileModel();
        if (utility.isTableState(commentHolder)) {
            mapTableHeaderComment(rt, commentHolder, fileModel);
        } else if (commentHolder == ParsingState.SETTING_LIBRARY_IMPORT) {
            mapLibraryComment(rt, commentHolder, fileModel);
        } else if (commentHolder == ParsingState.SETTING_VARIABLE_IMPORT) {
            mapVariablesComment(rt, commentHolder, fileModel);
        } else if (commentHolder == ParsingState.SETTING_RESOURCE_IMPORT) {
            mapResourceComment(rt, commentHolder, fileModel);
        } else if (commentHolder == ParsingState.SETTING_DOCUMENTATION) {
            mapSettingDocumentationComment(rt, commentHolder, fileModel);
        } else if (commentHolder == ParsingState.SETTING_METADATA) {
            mapSettingMetadataComment(rt, commentHolder, fileModel);
        } else if (commentHolder == ParsingState.SETTING_SUITE_SETUP
                || commentHolder == ParsingState.SETTING_SUITE_SETUP_KEYWORD
                || commentHolder == ParsingState.SETTING_SUITE_SETUP_KEYWORD_ARGUMENT) {
            mapSuiteSetupComment(rt, commentHolder, fileModel);
        } else if (commentHolder == ParsingState.SETTING_SUITE_TEARDOWN
                || commentHolder == ParsingState.SETTING_SUITE_TEARDOWN_KEYWORD
                || commentHolder == ParsingState.SETTING_SUITE_TEARDOWN_KEYWORD_ARGUMENT) {
            mapSuiteTeardownComment(rt, commentHolder, fileModel);
        } else if (commentHolder == ParsingState.SETTING_FORCE_TAGS
                || commentHolder == ParsingState.SETTING_FORCE_TAGS_TAG_NAME) {
            mapForceTagsComment(rt, commentHolder, fileModel);
        } else if (commentHolder == ParsingState.SETTING_DEFAULT_TAGS
                || commentHolder == ParsingState.SETTING_DEFAULT_TAGS_TAG_NAME) {
            mapDefaultTagsComment(rt, commentHolder, fileModel);
        } else if (commentHolder == ParsingState.SETTING_TEST_SETUP
                || commentHolder == ParsingState.SETTING_TEST_SETUP_KEYWORD
                || commentHolder == ParsingState.SETTING_TEST_SETUP_KEYWORD_ARGUMENT) {
            mapTestSetupComment(rt, commentHolder, fileModel);
        } else if (commentHolder == ParsingState.SETTING_TEST_TEARDOWN
                || commentHolder == ParsingState.SETTING_TEST_TEARDOWN_KEYWORD
                || commentHolder == ParsingState.SETTING_TEST_TEARDOWN_KEYWORD_ARGUMENT) {
            mapTestTeardownComment(rt, commentHolder, fileModel);
        } else if (commentHolder == ParsingState.SETTING_TEST_TEMPLATE
                || commentHolder == ParsingState.SETTING_TEST_TEMPLATE_KEYWORD
                || commentHolder == ParsingState.SETTING_TEST_TEMPLATE_KEYWORD_UNWANTED_ARGUMENTS) {
            mapTestTemplateComment(rt, commentHolder, fileModel);
        } else if (commentHolder == ParsingState.SETTING_TEST_TIMEOUT
                || commentHolder == ParsingState.SETTING_TEST_TIMEOUT_VALUE
                || commentHolder == ParsingState.SETTING_TEST_TIMEOUT_UNWANTED_ARGUMENTS) {
            mapTestTemplateComment(rt, commentHolder, fileModel);
        } else if (commentHolder == ParsingState.SCALAR_VARIABLE_DECLARATION
                || commentHolder == ParsingState.SCALAR_VARIABLE_VALUE
                || commentHolder == ParsingState.LIST_VARIABLE_DECLARATION
                || commentHolder == ParsingState.LIST_VARIABLE_VALUE
                || commentHolder == ParsingState.DICTIONARY_VARIABLE_DECLARATION
                || commentHolder == ParsingState.DICTIONARY_VARIABLE_VALUE
                || commentHolder == ParsingState.VARIABLE_TABLE_INSIDE) {
            mapVariableComment(rt, commentHolder, fileModel);
        } else if (commentHolder == ParsingState.TEST_CASE_DECLARATION) {
            // it will be line inside test
        } else if (commentHolder == ParsingState.TEST_CASE_SETTING_DOCUMENTATION_DECLARATION
                || commentHolder == ParsingState.TEST_CASE_SETTING_DOCUMENTATION_TEXT) {
            mapTestCaseDocumentationComment(rt, commentHolder, fileModel);
        } else if (commentHolder == ParsingState.TEST_CASE_SETTING_SETUP
                || commentHolder == ParsingState.TEST_CASE_SETTING_SETUP_KEYWORD
                || commentHolder == ParsingState.TEST_CASE_SETTING_SETUP_KEYWORD_ARGUMENT) {
            mapTestCaseSetupComment(rt, commentHolder, fileModel);
        } else if (commentHolder == ParsingState.TEST_CASE_SETTING_TEARDOWN
                || commentHolder == ParsingState.TEST_CASE_SETTING_TEARDOWN_KEYWORD
                || commentHolder == ParsingState.TEST_CASE_SETTING_TEARDOWN_KEYWORD_ARGUMENT) {
            mapTestCaseTeardownComment(rt, commentHolder, fileModel);
        } else if (commentHolder == ParsingState.TEST_CASE_SETTING_TAGS
                || commentHolder == ParsingState.TEST_CASE_SETTING_TAGS_TAG_NAME) {
            mapTestCaseTagsComment(rt, commentHolder, fileModel);
        } else if (commentHolder == ParsingState.TEST_CASE_SETTING_TEST_TEMPLATE
                || commentHolder == ParsingState.TEST_CASE_SETTING_TEST_TEMPLATE_KEYWORD
                || commentHolder == ParsingState.TEST_CASE_SETTING_TEST_TEMPLATE_KEYWORD_UNWANTED_ARGUMENTS) {
            mapTestTemplateComment(rt, commentHolder, fileModel);
        } else if (commentHolder == ParsingState.TEST_CASE_SETTING_TEST_TIMEOUT
                || commentHolder == ParsingState.TEST_CASE_SETTING_TEST_TIMEOUT_VALUE
                || commentHolder == ParsingState.TEST_CASE_SETTING_TEST_TIMEOUT_VALUE) {
            mapTestCaseTimeoutComment(rt, commentHolder, fileModel);
        }

        if (addToStack) {
            processingState.push(ParsingState.COMMENT);
        }

        return rt;
    }


    @VisibleForTesting
    protected void mapTestCaseTimeoutComment(RobotToken rt,
            ParsingState commentHolder, RobotFile fileModel) {
        List<TestCase> testCases = fileModel.getTestCaseTable().getTestCases();
        TestCase testCase = testCases.get(testCases.size() - 1);

        List<TestCaseTimeout> timeouts = testCase.getTimeouts();
        TestCaseTimeout testCaseTimeout = timeouts.get(timeouts.size() - 1);
        testCaseTimeout.addCommentPart(rt);
    }


    @VisibleForTesting
    protected void mapTestCaseTemplateComment(RobotToken rt,
            ParsingState commentHolder, RobotFile fileModel) {
        List<TestCase> testCases = fileModel.getTestCaseTable().getTestCases();
        TestCase testCase = testCases.get(testCases.size() - 1);

        List<TestCaseTemplate> templates = testCase.getTemplates();
        TestCaseTemplate testCaseTemplate = templates.get(templates.size() - 1);
        testCaseTemplate.addCommentPart(rt);
    }


    @VisibleForTesting
    protected void mapTestCaseTagsComment(RobotToken rt,
            ParsingState commentHolder, RobotFile fileModel) {
        List<TestCase> testCases = fileModel.getTestCaseTable().getTestCases();
        TestCase testCase = testCases.get(testCases.size() - 1);

        List<TestCaseTags> tags = testCase.getTags();
        TestCaseTags testCaseTags = tags.get(tags.size() - 1);
        testCaseTags.addCommentPart(rt);
    }


    @VisibleForTesting
    protected void mapTestCaseTeardownComment(RobotToken rt,
            ParsingState commentHolder, RobotFile fileModel) {
        List<TestCase> testCases = fileModel.getTestCaseTable().getTestCases();
        TestCase testCase = testCases.get(testCases.size() - 1);

        List<TestCaseTeardown> teardowns = testCase.getTeardowns();
        TestCaseTeardown testCaseTeardown = teardowns.get(teardowns.size() - 1);
        testCaseTeardown.addCommentPart(rt);
    }


    @VisibleForTesting
    protected void mapTestCaseSetupComment(RobotToken rt,
            ParsingState commentHolder, RobotFile fileModel) {
        List<TestCase> testCases = fileModel.getTestCaseTable().getTestCases();
        TestCase testCase = testCases.get(testCases.size() - 1);

        List<TestCaseSetup> setups = testCase.getSetups();
        TestCaseSetup testCaseSetup = setups.get(setups.size() - 1);
        testCaseSetup.addCommentPart(rt);
    }


    @VisibleForTesting
    protected void mapTestCaseDocumentationComment(RobotToken rt,
            ParsingState commentHolder, RobotFile fileModel) {
        List<TestCase> testCases = fileModel.getTestCaseTable().getTestCases();
        TestCase testCase = testCases.get(testCases.size() - 1);

        List<TestDocumentation> documentation = testCase.getDocumentation();
        TestDocumentation testDocumentation = documentation.get(documentation
                .size() - 1);
        testDocumentation.addCommentPart(rt);
    }


    @VisibleForTesting
    protected void mapVariableComment(RobotToken rt,
            ParsingState commentHolder, RobotFile fileModel) {
        List<IVariableHolder> variables = fileModel.getVariableTable()
                .getVariables();
        if (variables.isEmpty()) {
            ListVariable var = new ListVariable(null,
                    createArtifactalListVariable(rt));
            var.addCommentPart(rt);
            variables.add(var);
        } else {
            IVariableHolder var = variables.get(variables.size() - 1);
            if (var.getDeclaration().getLineNumber() == rt.getLineNumber()) {
                var.addCommentPart(rt);
            } else {
                ListVariable newVar = new ListVariable(null,
                        createArtifactalListVariable(rt));
                newVar.addCommentPart(rt);
                variables.add(newVar);
            }
        }
    }


    @VisibleForTesting
    protected RobotToken createArtifactalListVariable(final RobotToken rt) {
        RobotToken token = new RobotToken();
        token.setLineNumber(rt.getLineNumber());
        token.setStartColumn(rt.getStartColumn());
        token.setText(new StringBuilder());
        token.setRaw(new StringBuilder());
        token.setType(RobotTokenType.VARIABLES_LIST_DECLARATION);

        return token;
    }


    @VisibleForTesting
    protected void mapTestTimeoutComment(RobotToken rt,
            ParsingState commentHolder, RobotFile fileModel) {
        List<TestTimeout> testTimeouts = fileModel.getSettingTable()
                .getTestTimeouts();
        if (!testTimeouts.isEmpty()) {
            TestTimeout testTimeout = testTimeouts.get(testTimeouts.size() - 1);
            testTimeout.addCommentPart(rt);
        } else {
            // FIXME: errors internal
        }
    }


    @VisibleForTesting
    protected void mapTestTemplateComment(RobotToken rt,
            ParsingState commentHolder, RobotFile fileModel) {
        List<TestTemplate> testTemplates = fileModel.getSettingTable()
                .getTestTemplates();
        if (!testTemplates.isEmpty()) {
            TestTemplate testTemplate = testTemplates
                    .get(testTemplates.size() - 1);
            testTemplate.addCommentPart(rt);
        } else {
            // FIXME: errors internal
        }
    }


    @VisibleForTesting
    protected void mapTestTeardownComment(RobotToken rt,
            ParsingState commentHolder, RobotFile fileModel) {
        List<TestTeardown> testTeardowns = fileModel.getSettingTable()
                .getTestTeardowns();
        if (!testTeardowns.isEmpty()) {
            TestTeardown testTeardown = testTeardowns
                    .get(testTeardowns.size() - 1);
            testTeardown.addCommentPart(rt);
        } else {
            // FIXME: errors internal
        }
    }


    @VisibleForTesting
    protected void mapTestSetupComment(RobotToken rt,
            ParsingState commentHolder, RobotFile fileModel) {
        List<TestSetup> testSetups = fileModel.getSettingTable()
                .getTestSetups();
        if (!testSetups.isEmpty()) {
            TestSetup testSetup = testSetups.get(testSetups.size() - 1);
            testSetup.addCommentPart(rt);
        } else {
            // FIXME: errors internal
        }
    }


    @VisibleForTesting
    protected void mapDefaultTagsComment(RobotToken rt,
            ParsingState commentHolder, RobotFile fileModel) {
        List<DefaultTags> suiteDefaultTags = fileModel.getSettingTable()
                .getDefaultTags();
        if (!suiteDefaultTags.isEmpty()) {
            DefaultTags defaultTags = suiteDefaultTags.get(suiteDefaultTags
                    .size() - 1);
            defaultTags.addCommentPart(rt);
        } else {
            // FIXME: errors internal
        }
    }


    @VisibleForTesting
    protected void mapForceTagsComment(RobotToken rt,
            ParsingState commentHolder, RobotFile fileModel) {
        List<ForceTags> suiteForceTags = fileModel.getSettingTable()
                .getForceTags();
        if (!suiteForceTags.isEmpty()) {
            ForceTags forceTags = suiteForceTags.get(suiteForceTags.size() - 1);
            forceTags.addCommentPart(rt);
        } else {
            // FIXME: errors internal
        }
    }


    @VisibleForTesting
    protected void mapSuiteTeardownComment(RobotToken rt,
            ParsingState commentHolder, RobotFile fileModel) {
        List<SuiteTeardown> suiteTeardowns = fileModel.getSettingTable()
                .getSuiteTeardowns();
        if (!suiteTeardowns.isEmpty()) {
            SuiteTeardown suiteTeardown = suiteTeardowns.get(suiteTeardowns
                    .size() - 1);
            suiteTeardown.addCommentPart(rt);
        } else {
            // FIXME: errors internal
        }
    }


    @VisibleForTesting
    protected void mapSuiteSetupComment(RobotToken rt,
            ParsingState commentHolder, RobotFile fileModel) {
        List<SuiteSetup> suiteSetups = fileModel.getSettingTable()
                .getSuiteSetups();
        if (!suiteSetups.isEmpty()) {
            SuiteSetup suiteSetup = suiteSetups.get(suiteSetups.size() - 1);
            suiteSetup.addCommentPart(rt);
        } else {
            // FIXME: errors internal
        }
    }


    @VisibleForTesting
    protected void mapSettingMetadataComment(RobotToken rt,
            ParsingState commentHolder, RobotFile fileModel) {
        List<Metadata> metadatas = fileModel.getSettingTable().getMetadatas();
        if (!metadatas.isEmpty()) {
            Metadata metadata = metadatas.get(metadatas.size() - 1);
            metadata.addCommentPart(rt);
        } else {
            // FIXME: errors
        }
    }


    @VisibleForTesting
    protected void mapSettingDocumentationComment(RobotToken rt,
            ParsingState commentHolder, RobotFile fileModel) {
        List<SuiteDocumentation> documentations = fileModel.getSettingTable()
                .getDocumentation();
        if (!documentations.isEmpty()) {
            SuiteDocumentation suiteDoc = documentations.get(documentations
                    .size() - 1);
            suiteDoc.addCommentPart(rt);
        } else {
            // FIXME: errors
        }
    }


    @VisibleForTesting
    protected void mapResourceComment(RobotToken rt,
            ParsingState commentHolder, RobotFile fileModel) {
        List<AImported> imports = fileModel.getSettingTable().getImports();
        if (!imports.isEmpty()) {
            AImported aImported = imports.get(imports.size() - 1);
            if (aImported instanceof ResourceImport) {
                ResourceImport res = (ResourceImport) aImported;
                res.addCommentPart(rt);
            } else {
                // FIXME: error
            }
        } else {
            // FIXME: errors
        }
    }


    @VisibleForTesting
    protected void mapVariablesComment(RobotToken rt,
            ParsingState commentHolder, RobotFile fileModel) {
        List<AImported> imports = fileModel.getSettingTable().getImports();
        if (!imports.isEmpty()) {
            AImported aImported = imports.get(imports.size() - 1);
            if (aImported instanceof VariablesImport) {
                VariablesImport vars = (VariablesImport) aImported;
                vars.addCommentPart(rt);
            } else {
                // FIXME: error
            }
        } else {
            // FIXME: errors
        }
    }


    @VisibleForTesting
    protected void mapLibraryComment(RobotToken rt, ParsingState commentHolder,
            RobotFile fileModel) {
        List<AImported> imports = fileModel.getSettingTable().getImports();
        if (!imports.isEmpty()) {
            AImported aImported = imports.get(imports.size() - 1);
            if (aImported instanceof LibraryImport) {
                LibraryImport lib = (LibraryImport) aImported;
                lib.addCommentPart(rt);
            } else {
                // FIXME: error
            }
        } else {
            // FIXME: errors
        }
    }


    @VisibleForTesting
    protected void mapTableHeaderComment(RobotToken rt,
            ParsingState commentHolder, RobotFile fileModel) {
        ARobotSectionTable table = null;
        if (commentHolder == ParsingState.SETTING_TABLE_HEADER) {
            table = fileModel.getSettingTable();
        } else if (commentHolder == ParsingState.VARIABLE_TABLE_HEADER) {
            table = fileModel.getVariableTable();
        } else if (commentHolder == ParsingState.KEYWORD_TABLE_HEADER) {
            table = fileModel.getKeywordTable();
        } else if (commentHolder == ParsingState.TEST_CASE_TABLE_HEADER) {
            table = fileModel.getTestCaseTable();
        }

        List<TableHeader> headers = table.getHeaders();
        if (!headers.isEmpty()) {
            TableHeader header = headers.get(headers.size() - 1);
            header.addComment(rt);
        } else {
            // FIXME: it is not possible
        }
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result = false;

        if (rt.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
            processingState.push(ParsingState.COMMENT);
            result = true;
        } else if (!processingState.isEmpty()) {
            ParsingState state = processingState.peek();
            result = (state == ParsingState.COMMENT);
        }

        return result;
    }


    @VisibleForTesting
    protected ParsingState findNearestCommentDeclaringModelElement(
            Stack<ParsingState> processingState) {
        ParsingState state = ParsingState.TRASH;

        int capacity = processingState.size();
        for (int i = capacity - 1; i >= 0; i--) {
            ParsingState s = processingState.get(i);
            if (utility.isTableState(s) || isInsideTableState(s)
                    || isSettingImports(s) || isSettingTableElement(s)) {
                state = s;
                break;
            }
        }

        return state;
    }


    @VisibleForTesting
    protected boolean isSettingTableElement(ParsingState s) {
        return (s == ParsingState.SETTING_DOCUMENTATION || s == ParsingState.SETTING_METADATA);
    }


    @VisibleForTesting
    protected boolean isSettingImports(ParsingState s) {
        return (s == ParsingState.SETTING_LIBRARY_IMPORT
                || s == ParsingState.SETTING_VARIABLE_IMPORT || s == ParsingState.SETTING_RESOURCE_IMPORT);
    }


    @VisibleForTesting
    protected boolean isInsideTableState(ParsingState state) {
        return (state == ParsingState.KEYWORD_TABLE_INSIDE
                || state == ParsingState.SETTING_TABLE_INSIDE
                || state == ParsingState.TEST_CASE_TABLE_INSIDE || state == ParsingState.VARIABLE_TABLE_INSIDE);
    }
}

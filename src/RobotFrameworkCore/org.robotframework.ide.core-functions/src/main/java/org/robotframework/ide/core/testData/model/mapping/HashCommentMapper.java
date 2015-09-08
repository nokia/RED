package org.robotframework.ide.core.testData.model.mapping;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.IRobotFile;
import org.robotframework.ide.core.testData.model.IRobotFileOutput;
import org.robotframework.ide.core.testData.model.mapping.hashComment.TableHeaderCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.VariablesDeclarationCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableSetting.SettingDefaultTagsCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableSetting.SettingDocumentationCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableSetting.SettingForceTagsCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableSetting.SettingLibraryCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableSetting.SettingMetadataCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableSetting.SettingResourceCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableSetting.SettingSuiteSetupCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableSetting.SettingSuiteTeardownCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableSetting.SettingTestSetupCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableSetting.SettingTestTeardownCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableSetting.SettingTestTemplateCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableSetting.SettingTestTimeoutCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableSetting.SettingVariableCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableTestCase.TestCaseSettingDocumentationCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableTestCase.TestCaseSettingSetupCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableTestCase.TestCaseSettingTagsCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableTestCase.TestCaseSettingTeardownCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableTestCase.TestCaseSettingTemplateCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableTestCase.TestCaseSettingTimeoutCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableUserKeyword.UserKeywordSettingArgumentsCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableUserKeyword.UserKeywordSettingDocumentationCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableUserKeyword.UserKeywordSettingReturnCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableUserKeyword.UserKeywordSettingTagsCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableUserKeyword.UserKeywordSettingTeardownCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.hashComment.tableUserKeyword.UserKeywordSettingTimeoutCommentMapper;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class HashCommentMapper implements IParsingMapper {

    private final ElementsUtility utility;

    private static final List<IHashCommentMapper> commentMappers = new LinkedList<>();
    static {
        commentMappers.add(new TableHeaderCommentMapper());
        commentMappers.add(new SettingLibraryCommentMapper());
        commentMappers.add(new SettingVariableCommentMapper());
        commentMappers.add(new SettingResourceCommentMapper());
        commentMappers.add(new SettingDocumentationCommentMapper());
        commentMappers.add(new SettingMetadataCommentMapper());
        commentMappers.add(new SettingSuiteSetupCommentMapper());
        commentMappers.add(new SettingSuiteTeardownCommentMapper());
        commentMappers.add(new SettingForceTagsCommentMapper());
        commentMappers.add(new SettingDefaultTagsCommentMapper());
        commentMappers.add(new SettingTestSetupCommentMapper());
        commentMappers.add(new SettingTestTeardownCommentMapper());
        commentMappers.add(new SettingTestTemplateCommentMapper());
        commentMappers.add(new SettingTestTimeoutCommentMapper());
        commentMappers.add(new VariablesDeclarationCommentMapper());
        commentMappers.add(new TestCaseSettingDocumentationCommentMapper());
        commentMappers.add(new TestCaseSettingSetupCommentMapper());
        commentMappers.add(new TestCaseSettingTeardownCommentMapper());
        commentMappers.add(new TestCaseSettingTagsCommentMapper());
        commentMappers.add(new TestCaseSettingTemplateCommentMapper());
        commentMappers.add(new TestCaseSettingTimeoutCommentMapper());
        commentMappers.add(new UserKeywordSettingDocumentationCommentMapper());
        commentMappers.add(new UserKeywordSettingTagsCommentMapper());
        commentMappers.add(new UserKeywordSettingArgumentsCommentMapper());
        commentMappers.add(new UserKeywordSettingReturnCommentMapper());
        commentMappers.add(new UserKeywordSettingTeardownCommentMapper());
        commentMappers.add(new UserKeywordSettingTimeoutCommentMapper());
    }


    public HashCommentMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            IRobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        boolean addToStack = false;
        if (rt.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
            rt.setType(RobotTokenType.START_HASH_COMMENT);
            addToStack = true;
        } else {
            rt.setType(RobotTokenType.COMMENT_CONTINUE);
        }

        ParsingState commentHolder = findNearestCommentDeclaringModelElement(processingState);
        IRobotFile fileModel = robotFileOutput.getFileModel();
        IHashCommentMapper commentMapper = findApplicableMapper(commentHolder);
        if (commentHolder != ParsingState.TRASH || commentMapper != null) {
            commentMapper.map(rt, commentHolder, fileModel);
        }

        if (addToStack) {
            processingState.push(ParsingState.COMMENT);
        }

        return rt;
    }


    @VisibleForTesting
    public IHashCommentMapper findApplicableMapper(ParsingState state) {
        IHashCommentMapper mapperApplicable = null;
        for (IHashCommentMapper mapper : commentMappers) {
            if (mapper.isApplicable(state)) {
                mapperApplicable = mapper;
                break;
            }
        }

        return mapperApplicable;
    }


    @Override
    public boolean checkIfCanBeMapped(IRobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result = false;

        ParsingState nearestState = utility.getCurrentStatus(processingState);
        if (rt.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
            if (isInsideTestCase(nearestState) || isInsideKeyword(nearestState)) {
                result = false;
            } else if (!processingState.isEmpty()) {
                processingState.push(ParsingState.COMMENT);
                result = true;
            }
        } else if (!processingState.isEmpty()) {
            ParsingState state = processingState.peek();
            result = (state == ParsingState.COMMENT);
        }

        return result;
    }


    @VisibleForTesting
    protected boolean isInsideTestCase(final ParsingState state) {
        return (state == ParsingState.TEST_CASE_INSIDE_ACTION
                || state == ParsingState.TEST_CASE_INSIDE_ACTION_ARGUMENT || state == ParsingState.TEST_CASE_DECLARATION);
    }


    @VisibleForTesting
    protected boolean isInsideKeyword(final ParsingState state) {
        return (state == ParsingState.KEYWORD_INSIDE_ACTION
                || state == ParsingState.KEYWORD_INSIDE_ACTION_ARGUMENT || state == ParsingState.KEYWORD_DECLARATION);
    }


    @VisibleForTesting
    protected ParsingState findNearestCommentDeclaringModelElement(
            Stack<ParsingState> processingState) {
        ParsingState state = ParsingState.TRASH;

        int capacity = processingState.size();
        for (int i = capacity - 1; i >= 0; i--) {
            ParsingState s = processingState.get(i);
            if (ParsingState.getSettingsStates().contains(s)) {
                state = s;
                break;
            }
        }

        return state;
    }
}

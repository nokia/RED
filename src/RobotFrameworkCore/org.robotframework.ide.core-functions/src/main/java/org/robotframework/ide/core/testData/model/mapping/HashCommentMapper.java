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
import org.robotframework.ide.core.testData.model.table.setting.LibraryImport;
import org.robotframework.ide.core.testData.model.table.setting.Metadata;
import org.robotframework.ide.core.testData.model.table.setting.ResourceImport;
import org.robotframework.ide.core.testData.model.table.setting.SuiteDocumentation;
import org.robotframework.ide.core.testData.model.table.setting.VariablesImport;
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
        }

        if (addToStack) {
            processingState.push(ParsingState.COMMENT);
        }

        return rt;
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

package org.robotframework.ide.core.testData.model.table.variables.mapping;

import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.VariableTable;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.variables.DictionaryVariable;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class DictionaryVariableMapper implements IParsingMapper {

    private final ElementsUtility utility;
    private final CommonVariableHelper varHelper;


    public DictionaryVariableMapper() {
        this.utility = new ElementsUtility();
        this.varHelper = new CommonVariableHelper();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        VariableTable varTable = robotFileOutput.getFileModel()
                .getVariableTable();
        rt.setText(new StringBuilder(text));
        rt.setType(RobotTokenType.VARIABLES_DICTIONARY_DECLARATION);

        DictionaryVariable var = robotFileOutput.getObjectCreator()
                .createDictionaryVariable(varHelper.extractVariableName(text),
                        rt);
        varTable.addVariable(var);

        processingState.push(ParsingState.DICTIONARY_VARIABLE_DECLARATION);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result = false;
        if (rt.getTypes().contains(
                RobotTokenType.VARIABLES_DICTIONARY_DECLARATION)) {
            if (utility.isTheFirstColumn(currentLine, rt)) {
                if (varHelper.isIncludedInVariableTable(currentLine,
                        processingState)) {
                    if (varHelper.isCorrectVariable(text)) {
                        result = true;
                    } else {
                        // FIXME: error here or in validation
                    }
                } else {
                    // FIXME: it is in wrong place means no variable table
                    // declaration
                }
            } else {
                // FIXME: wrong place case.
            }
        }
        return result;
    }
}

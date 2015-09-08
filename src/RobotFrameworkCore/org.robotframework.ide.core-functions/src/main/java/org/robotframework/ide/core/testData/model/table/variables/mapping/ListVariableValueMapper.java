package org.robotframework.ide.core.testData.model.table.variables.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.VariableTable;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.variables.IVariableHolder;
import org.robotframework.ide.core.testData.model.table.variables.ListVariable;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class ListVariableValueMapper implements IParsingMapper {

    private final ElementsUtility utility;


    public ListVariableValueMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.VARIABLES_VARIABLE_VALUE);

        VariableTable variableTable = robotFileOutput.getFileModel()
                .getVariableTable();
        List<IVariableHolder> variables = variableTable.getVariables();
        if (!variables.isEmpty()) {
            IVariableHolder var = variables.get(variables.size() - 1);
            ((ListVariable) var).addItem(rt);
        } else {
            // FIXME: some error
        }
        processingState.push(ParsingState.LIST_VARIABLE_VALUE);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        ParsingState state = utility.getCurrentStatus(processingState);
        return (state == ParsingState.LIST_VARIABLE_DECLARATION || state == ParsingState.LIST_VARIABLE_VALUE);
    }
}

package org.robotframework.ide.core.testData.model.table.variables.mapping;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.IRobotFileOutput;
import org.robotframework.ide.core.testData.model.table.VariableTable;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.variables.IVariableHolder;
import org.robotframework.ide.core.testData.model.table.variables.ScalarVariable;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class ScalarVariableValueMapper implements IParsingMapper {

    private final ElementsUtility utility;


    public ScalarVariableValueMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            IRobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.VARIABLES_VARIABLE_VALUE);

        VariableTable variableTable = robotFileOutput.getFileModel()
                .getVariableTable();
        List<IVariableHolder> variables = variableTable.getVariables();
        if (!variables.isEmpty()) {
            IVariableHolder var = variables.get(variables.size() - 1);
            ((ScalarVariable) var).addValue(rt);
        } else {
            // FIXME: some error
        }
        processingState.push(ParsingState.SCALAR_VARIABLE_VALUE);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(IRobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        ParsingState state = utility.getCurrentStatus(processingState);
        return (state == ParsingState.SCALAR_VARIABLE_DECLARATION || state == ParsingState.SCALAR_VARIABLE_VALUE);
    }
}

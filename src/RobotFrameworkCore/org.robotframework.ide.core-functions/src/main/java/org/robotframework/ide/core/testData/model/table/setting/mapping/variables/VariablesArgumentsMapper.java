package org.robotframework.ide.core.testData.model.table.setting.mapping.variables;

import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.IRobotFileOutput;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.setting.AImported;
import org.robotframework.ide.core.testData.model.table.setting.VariablesImport;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class VariablesArgumentsMapper implements IParsingMapper {

    private final ElementsUtility utility;


    public VariablesArgumentsMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            IRobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        rt.setType(RobotTokenType.SETTING_VARIABLES_ARGUMENT);
        rt.setText(new StringBuilder(text));
        AImported imported = utility.getNearestImport(robotFileOutput);
        VariablesImport vars;
        if (imported instanceof VariablesImport) {
            vars = (VariablesImport) imported;
        } else {
            vars = null;

            // FIXME: sth wrong - declaration of library not inside setting and
            // was not catch by previous library declaration logic
        }

        vars.addArgument(rt);

        processingState.push(ParsingState.SETTING_VARIABLE_ARGUMENTS);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(IRobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result;
        if (!processingState.isEmpty()) {
            ParsingState currentState = utility
                    .getCurrentStatus(processingState);
            if (currentState == ParsingState.SETTING_VARIABLE_IMPORT_PATH
                    || currentState == ParsingState.SETTING_VARIABLE_ARGUMENTS) {
                result = true;
            } else {
                result = false;
            }
        } else {
            result = false;
        }
        return result;
    }

}

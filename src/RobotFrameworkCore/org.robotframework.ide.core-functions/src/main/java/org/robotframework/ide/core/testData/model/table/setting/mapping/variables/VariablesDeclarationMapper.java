package org.robotframework.ide.core.testData.model.table.setting.mapping.variables;

import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.setting.VariablesImport;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.TxtRobotFileParser.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class VariablesDeclarationMapper implements IParsingMapper {

    private final ElementsUtility utility;


    public VariablesDeclarationMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        rt.setType(RobotTokenType.SETTING_VARIABLES_DECLARATION);
        rt.setText(new StringBuilder(text));

        SettingTable settings = robotFileOutput.getFileModel()
                .getSettingTable();
        VariablesImport variables = new VariablesImport(rt);
        settings.addImported(variables);
        processingState.push(ParsingState.SETTING_VARIABLE_IMPORT);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt,
            Stack<ParsingState> processingState) {
        boolean result = false;
        if (rt.getType() == RobotTokenType.SETTING_VARIABLES_DECLARATION) {
            if (utility.isTheFirstColumn(currentLine, rt)) {
                if (isIncludedInSettingTable(currentLine, processingState)) {
                    result = true;
                } else {
                    // FIXME: it is in wrong place means no settings table
                    // declaration
                }
            } else {
                // FIXME: wrong place | | Library or | Library | Library X |
                // case.
            }
        }
        return result;
    }


    @VisibleForTesting
    protected boolean isIncludedInSettingTable(final RobotLine line,
            final Stack<ParsingState> processingState) {
        boolean result;
        if (!processingState.isEmpty()) {
            result = (processingState.get(processingState.size() - 1) == ParsingState.SETTING_TABLE_INSIDE);
        } else {
            result = false;
        }

        return result;
    }
}

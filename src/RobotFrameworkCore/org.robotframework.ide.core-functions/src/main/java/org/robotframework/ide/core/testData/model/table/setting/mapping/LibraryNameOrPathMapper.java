package org.robotframework.ide.core.testData.model.table.setting.mapping;

import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.setting.AImported;
import org.robotframework.ide.core.testData.model.table.setting.LibraryImport;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.TxtRobotFileParser.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken.RobotTokenType;


public class LibraryNameOrPathMapper implements IParsingMapper {

    private final ElementsUtility utility;


    public LibraryNameOrPathMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        rt.setType(RobotTokenType.SETTING_LIBRARY_NAME);
        rt.setText(new StringBuilder(text));
        AImported imported = utility.getNearestImport(robotFileOutput);
        LibraryImport lib;
        if (imported instanceof LibraryImport) {
            lib = (LibraryImport) imported;
        } else {
            lib = null;

            // FIXME: sth wrong - declaration of library not inside setting and
            // was not catch by previous library declaration logic
        }
        lib.setLibraryNameOrPathToFile(rt);

        processingState.push(ParsingState.SETTING_LIBRARY_NAME_OR_PATH);
        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt,
            Stack<ParsingState> processingState) {
        boolean result;
        if (!processingState.isEmpty()) {
            result = (processingState.get(processingState.size() - 1) == ParsingState.SETTING_LIBRARY_IMPORT);
        } else {
            result = false;
        }

        return result;
    }
}

package org.robotframework.ide.core.testData.model.table.setting.mapping.library;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.setting.AImported;
import org.robotframework.ide.core.testData.model.table.setting.LibraryAlias;
import org.robotframework.ide.core.testData.model.table.setting.LibraryImport;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class LibraryAliasDeclarationMapper implements IParsingMapper {

    private final ElementsUtility utility;


    public LibraryAliasDeclarationMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        rt.setType(RobotTokenType.SETTING_LIBRARY_ALIAS);
        rt.setText(new StringBuilder(text));

        LibraryImport lib = getNearestLibraryImport(robotFileOutput);
        LibraryAlias alias = robotFileOutput.getObjectCreator()
                .createLibraryAlias(rt);
        lib.setAlias(alias);

        processingState.push(ParsingState.SETTING_LIBRARY_IMPORT_ALIAS);
        return rt;
    }


    @VisibleForTesting
    protected LibraryImport getNearestLibraryImport(
            final RobotFileOutput robotFileOutput) {
        LibraryImport library = null;
        List<AImported> imports = robotFileOutput.getFileModel()
                .getSettingTable().getImports();
        if (!imports.isEmpty()) {
            AImported aImported = imports.get(imports.size() - 1);
            if (aImported instanceof LibraryImport) {
                library = (LibraryImport) aImported;
            } else {
                // FIXME: sth wrong inside mapping logic
            }
        } else {
            // FIXME: sth wrong - declaration of library not inside setting and
            // was not catch by previous library declaration logic
        }

        return library;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result;
        if (rt.getTypes().contains(RobotTokenType.SETTING_LIBRARY_ALIAS)) {
            ParsingState state = utility.getCurrentStatus(processingState);
            if (state == ParsingState.SETTING_LIBRARY_NAME_OR_PATH
                    || state == ParsingState.SETTING_LIBRARY_ARGUMENTS) {
                result = true;
            } else if (state == ParsingState.SETTING_LIBRARY_IMPORT) {
                // FIXME: warning that library name is the same as alias
                // declaration
                result = false;
            } else {
                // FIXME: wrong place
                result = false;
            }
        } else {
            result = false;
        }

        return result;
    }
}

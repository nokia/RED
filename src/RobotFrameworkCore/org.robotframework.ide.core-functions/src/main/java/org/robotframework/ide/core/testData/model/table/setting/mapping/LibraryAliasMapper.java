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


public class LibraryAliasMapper implements IParsingMapper {

    private final ElementsUtility utility;
    private final LibraryAliasFixer libraryFixer;


    public LibraryAliasMapper() {
        this.utility = new ElementsUtility();
        this.libraryFixer = new LibraryAliasFixer();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        rt.setType(RobotTokenType.SETTING_LIBRARY_ALIAS_VALUE);
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

        lib.getAlias().setLibraryAlias(rt);
        processingState.push(ParsingState.SETTING_LIBRARY_IMPORT_ALIAS_VALUE);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt,
            Stack<ParsingState> processingState) {
        boolean result;

        ParsingState status = utility.getCurrentStatus(processingState);
        if (status == ParsingState.SETTING_LIBRARY_IMPORT_ALIAS) {
            result = true;
        } else if (status == ParsingState.SETTING_LIBRARY_IMPORT_ALIAS_VALUE) {
            AImported imported = utility.getNearestImport(robotFileOutput);
            LibraryImport lib;
            if (imported instanceof LibraryImport) {
                lib = (LibraryImport) imported;
            } else {
                lib = null;

                // FIXME: sth wrong - declaration of library not inside setting
                // and
                // was not catch by previous library declaration logic
            }

            libraryFixer.applyFixes(lib, rt, processingState);
            // FIXME: fixer apply we have Library OperatingSystem WITH NAME x |
            // y
            // (two aliases)
            result = false;
        } else {
            result = false;
        }

        return result;
    }
}

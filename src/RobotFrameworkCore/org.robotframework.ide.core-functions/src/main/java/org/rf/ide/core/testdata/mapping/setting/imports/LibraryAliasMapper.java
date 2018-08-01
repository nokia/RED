/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.setting.imports;

import java.util.Stack;

import org.rf.ide.core.testdata.mapping.table.ElementsUtility;
import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class LibraryAliasMapper implements IParsingMapper {

    private final ElementsUtility utility;
    private final ParsingStateHelper parsingStateHelper;
    private final LibraryAliasFixer libraryFixer;


    public LibraryAliasMapper() {
        this.utility = new ElementsUtility();
        this.parsingStateHelper = new ParsingStateHelper();
        this.libraryFixer = new LibraryAliasFixer(utility, parsingStateHelper);
    }


    @Override
    public RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp,
            final String text) {
        rt.getTypes().add(0, RobotTokenType.SETTING_LIBRARY_ALIAS_VALUE);
        rt.setText(text);

        final AImported imported = utility.getNearestImport(robotFileOutput);
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
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {
        boolean result;

        final ParsingState status = parsingStateHelper
                .getCurrentStatus(processingState);
        if (status == ParsingState.SETTING_LIBRARY_IMPORT_ALIAS) {
            result = true;
        } else if (status == ParsingState.SETTING_LIBRARY_IMPORT_ALIAS_VALUE) {
            final AImported imported = utility.getNearestImport(robotFileOutput);
            LibraryImport lib;
            if (imported instanceof LibraryImport) {
                lib = (LibraryImport) imported;
            } else {
                lib = null;

                // FIXME: sth wrong - declaration of library not inside setting
                // and
                // was not catch by previous library declaration logic
            }

            libraryFixer.applyFixes(robotFileOutput, lib, rt, processingState);
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

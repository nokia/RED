/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.setting.imports;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.model.table.setting.LibraryAlias;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class LibraryAliasDeclarationMapper implements IParsingMapper {

    private final ParsingStateHelper utility;


    public LibraryAliasDeclarationMapper() {
        this.utility = new ParsingStateHelper();
    }


    @Override
    public RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp,
            final String text) {
        rt.setType(RobotTokenType.SETTING_LIBRARY_ALIAS);
        rt.setText(text);

        final LibraryImport lib = getNearestLibraryImport(robotFileOutput);
        final LibraryAlias alias = new LibraryAlias(rt);
        lib.setAlias(alias);

        processingState.push(ParsingState.SETTING_LIBRARY_IMPORT_ALIAS);
        return rt;
    }


    @VisibleForTesting
    protected LibraryImport getNearestLibraryImport(
            final RobotFileOutput robotFileOutput) {
        LibraryImport library = null;
        final List<AImported> imports = robotFileOutput.getFileModel()
                .getSettingTable().getImports();
        if (!imports.isEmpty()) {
            final AImported aImported = imports.get(imports.size() - 1);
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
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {
        boolean result;
        if (rt.getTypes().contains(RobotTokenType.SETTING_LIBRARY_ALIAS)) {
            final ParsingState state = utility.getCurrentStatus(processingState);
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

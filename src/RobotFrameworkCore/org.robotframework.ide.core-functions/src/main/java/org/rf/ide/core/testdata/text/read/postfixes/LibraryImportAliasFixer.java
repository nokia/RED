/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.postfixes;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.LibraryAlias;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.text.read.postfixes.PostProcessingFixActions.IPostProcessFixer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

class LibraryImportAliasFixer implements IPostProcessFixer {

    @Override
    public void applyFix(final RobotFileOutput parsingOutput) {
        final RobotFile model = parsingOutput.getFileModel();
        final SettingTable settingTable = model.getSettingTable();
        if (settingTable.isPresent()) {
            for (final LibraryImport libraryImport : settingTable.getLibraryImports()) {
                final int indexOfAlias = indexOfAliasStarting(libraryImport, parsingOutput.getRobotVersion());
                if (indexOfAlias > -1) {
                    final LibraryAlias alias = new LibraryAlias(getAliasArgument(libraryImport, 1));
                    alias.setLibraryAlias(getAliasArgument(libraryImport, 0));

                    // remove alias value and alias declaration
                    libraryImport.removeArgument(indexOfAlias);
                    libraryImport.removeArgument(indexOfAlias);

                    libraryImport.setAlias(alias);
                }
            }
        }
    }

    private RobotToken getAliasArgument(final LibraryImport libraryImport, final int offsetFromEnd) {
        final RobotToken arg = libraryImport.getArguments()
                .get(libraryImport.getArguments().size() - 1 - offsetFromEnd);
        arg.getTypes().remove(RobotTokenType.SETTING_LIBRARY_ARGUMENT);
        return arg;
    }

    private int indexOfAliasStarting(final LibraryImport libraryImport, final RobotVersion robotVersion) {
        final int size = libraryImport.getArguments().size();
        if (size >= 2) {
            final String penultimateArg = libraryImport.getArguments().get(size - 2).getText();
            final String aliasRepresentation = RobotTokenType.SETTING_LIBRARY_ALIAS
                    .getTheMostCorrectOneRepresentation(robotVersion)
                    .getRepresentation();
            if (LibraryAlias.equalsIgnoreSpaces(penultimateArg, aliasRepresentation,
                    robotVersion.isNewerOrEqualTo(new RobotVersion(3, 1)))) {
                return size - 2;
            }
        }
        return -1;
    }
}

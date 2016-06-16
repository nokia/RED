/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.settings;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.presenter.update.ISettingTableElementOperation;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.model.table.setting.LibraryAlias;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class LibraryImportModelOperation implements ISettingTableElementOperation {

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return (elementType == RobotTokenType.SETTING_LIBRARY_DECLARATION);
    }

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return (elementType == ModelType.LIBRARY_IMPORT_SETTING);
    }

    @Override
    public AModelElement<?> create(final SettingTable settingsTable, final int tableIndex, final List<String> args,
            final String comment) {
        final LibraryImport newLibraryImport = isValidTableIndex(settingsTable, tableIndex)
                ? settingsTable.newLibraryImport(tableIndex) : settingsTable.newLibraryImport();
        if (!args.isEmpty()) {
            newLibraryImport.setPathOrName(args.get(0));
        }

        int argsNr = args.size();
        int indexOfAlias = indexOfAliasStarting(args, settingsTable.getParent().getParent().getRobotVersion());
        if (indexOfAlias >= 0) {
            RobotToken aliasText = new RobotToken();
            aliasText.setText(args.get(argsNr - 1));

            RobotToken aliasDecText = new RobotToken();
            aliasDecText.setText(args.get(argsNr - 2));

            LibraryAlias libAliasDec = new LibraryAlias(aliasDecText);
            libAliasDec.setLibraryAlias(aliasText);

            newLibraryImport.setAlias(libAliasDec);
            argsNr -= 2;
        }

        for (int i = 1; i < argsNr; i++) {
            newLibraryImport.addArgument(args.get(i));
        }
        if (comment != null && !comment.isEmpty()) {
            newLibraryImport.setComment(comment);
        }
        return newLibraryImport;
    }

    private int indexOfAliasStarting(final List<String> args, final RobotVersion rVersionInstalled) {
        int aliasDecIndex = -1;

        int size = args.size();
        if (size >= 2) {
            String lastArg = args.get(size - 1);
            String penultimateArg = args.get(size - 2);

            final String aliasDecText = RobotTokenType.SETTING_LIBRARY_ALIAS
                    .getTheMostCorrectOneRepresentation(rVersionInstalled).getRepresentation();
            if (!lastArg.equals(aliasDecText) && penultimateArg.equals(aliasDecText)) {
                aliasDecIndex = size - 2;
            }
        }

        return aliasDecIndex;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final LibraryImport libraryImport = (LibraryImport) modelElement;
        if (index == 0) {
            libraryImport.setPathOrName(value);
        } else if (index > 0) {
            if (value == null) {
                libraryImport.removeElementToken(index-1);
            } else {
                final int properIndex = index - 1;

                int nrOfArgs = libraryImport.getArguments().size();
                int nrOfArgsWithAlias = nrOfArgs + libraryImport.getAlias().getElementTokens().size();

                if (nrOfArgs < nrOfArgsWithAlias) {
                    libraryImport.addArgument(libraryImport.getAlias().getDeclaration());
                    libraryImport.addArgument(libraryImport.getAlias().getLibraryAlias());
                    libraryImport.setAlias(new LibraryAlias(null));
                }

                libraryImport.setArguments(properIndex, value);

                int indexOfAlias = indexOfAliasStarting(
                        Lists.transform(libraryImport.getArguments(), new GetTokenText()),
                        libraryImport.getParent().getParent().getParent().getRobotVersion());

                if (indexOfAlias > -1) {
                    RobotToken aliasDec = libraryImport.getArguments().get(libraryImport.getArguments().size() - 2);
                    RobotToken aliasValue = libraryImport.getArguments().get(libraryImport.getArguments().size() - 1);

                    // remove alias value and alias declaration
                    libraryImport.removeArgument(indexOfAlias);
                    libraryImport.removeArgument(indexOfAlias);

                    LibraryAlias alias = new LibraryAlias(aliasDec);
                    alias.setLibraryAlias(aliasValue);

                    libraryImport.setAlias(alias);
                }
            }
        }
    }

    private class GetTokenText implements Function<RobotToken, String> {

        @Override
        public String apply(final RobotToken token) {
            return token.getText();
        }

    }

    @Override
    public void remove(final SettingTable settingsTable, final AModelElement<?> modelElement) {
        settingsTable.removeImported((AImported) modelElement);
    }

    private boolean isValidTableIndex(final SettingTable settingsTable, final int tableIndex) {
        return tableIndex >= 0 && tableIndex < settingsTable.getImports().size();
    }
}

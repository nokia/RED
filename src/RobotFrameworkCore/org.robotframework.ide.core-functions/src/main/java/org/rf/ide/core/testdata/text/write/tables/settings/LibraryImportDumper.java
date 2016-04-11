/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.settings;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.RobotElementsComparatorWithPositionChangedPresave;
import org.rf.ide.core.testdata.model.table.setting.LibraryAlias;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.tables.ANotExecutableTableElementDumper;

public class LibraryImportDumper extends ANotExecutableTableElementDumper {

    public LibraryImportDumper(final DumperHelper aDumpHelper) {
        super(aDumpHelper, ModelType.LIBRARY_IMPORT_SETTING);
    }

    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            AModelElement<? extends ARobotSectionTable> currentElement) {
        LibraryImport library = (LibraryImport) currentElement;
        List<RobotToken> libNames = new ArrayList<>(0);
        if (library.getPathOrName() != null) {
            libNames.add(library.getPathOrName());
        }

        List<RobotToken> libAliasDec = new ArrayList<>(0);
        if (library.getAlias() != null && library.getAlias().isPresent()) {
            libAliasDec.add(library.getAlias().getDeclaration());
        }

        List<RobotToken> libAliasNames = new ArrayList<>(0);
        if (!libAliasDec.isEmpty()) {
            LibraryAlias alias = library.getAlias();
            if (alias.isPresent() && alias.getLibraryAlias() != null) {
                libAliasNames.add(alias.getLibraryAlias());
            }
        }

        RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();
        sorter.addPresaveSequenceForType(RobotTokenType.SETTING_LIBRARY_NAME, 1, libNames);
        sorter.addPresaveSequenceForType(RobotTokenType.SETTING_LIBRARY_ARGUMENT, 2, library.getArguments());
        sorter.addPresaveSequenceForType(RobotTokenType.SETTING_LIBRARY_ALIAS, 3, libAliasDec);
        sorter.addPresaveSequenceForType(RobotTokenType.SETTING_LIBRARY_ALIAS_VALUE, 4, libAliasNames);
        sorter.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 5,
                getElementHelper().filter(library.getComment(), RobotTokenType.START_HASH_COMMENT));
        sorter.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 5,
                getElementHelper().filter(library.getComment(), RobotTokenType.COMMENT_CONTINUE));

        return sorter;
    }

}

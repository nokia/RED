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
import org.rf.ide.core.testdata.model.table.RobotElementsComparatorWithPositionChangedPresave;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.tables.ANotExecutableTableElementDumper;

public class VariablesImportDumper extends ANotExecutableTableElementDumper<SettingTable> {

    public VariablesImportDumper(final DumperHelper helper) {
        super(helper, ModelType.VARIABLES_IMPORT_SETTING);
    }

    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            final AModelElement<SettingTable> currentElement) {
        final VariablesImport variables = (VariablesImport) currentElement;

        final List<RobotToken> varsPaths = new ArrayList<>(0);
        if (variables.getPathOrName() != null) {
            varsPaths.add(variables.getPathOrName());
        }

        final RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();
        sorter.addPresaveSequenceForType(RobotTokenType.SETTING_VARIABLES_FILE_NAME, 1, varsPaths);
        sorter.addPresaveSequenceForType(RobotTokenType.SETTING_VARIABLES_ARGUMENT, 2, variables.getArguments());
        sorter.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 3,
                elemUtility.filter(variables.getComment(), RobotTokenType.START_HASH_COMMENT));
        sorter.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 4,
                elemUtility.filter(variables.getComment(), RobotTokenType.COMMENT_CONTINUE));

        return sorter;
    }
}

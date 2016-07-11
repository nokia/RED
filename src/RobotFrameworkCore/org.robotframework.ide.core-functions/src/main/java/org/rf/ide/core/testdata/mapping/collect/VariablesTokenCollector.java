/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.collect;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

/**
 * @author wypych
 */
public class VariablesTokenCollector implements ITableTokensCollector {

    @Override
    public List<RobotToken> collect(final RobotFileOutput outModel) {
        final List<RobotToken> tokens = new ArrayList<>(0);

        final VariableTable variablesTable = outModel.getFileModel().getVariableTable();
        if (variablesTable.isPresent()) {
            tokens.addAll(AModelElementElementsHelper.collect(variablesTable.getHeaders()));
            tokens.addAll(AModelElementElementsHelper.collect(variablesTable.getVariables()));
        }

        return tokens;
    }
}

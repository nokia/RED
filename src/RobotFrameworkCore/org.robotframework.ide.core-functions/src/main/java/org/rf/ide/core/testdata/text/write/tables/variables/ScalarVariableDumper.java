/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.variables;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.RobotElementsComparatorWithPositionChangedPresave;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.tables.ANotExecutableTableElementDumper;

public class ScalarVariableDumper extends ANotExecutableTableElementDumper {

    public ScalarVariableDumper(final DumperHelper aDumpHelper) {
        super(aDumpHelper, ModelType.SCALAR_VARIABLE_DECLARATION_IN_TABLE);
    }

    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            final AModelElement<? extends ARobotSectionTable> currentElement) {
        ScalarVariable var = (ScalarVariable) currentElement;

        RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();
        sorter.addPresaveSequenceForType(RobotTokenType.VARIABLES_VARIABLE_VALUE, 1, var.getValues());
        sorter.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 2,
                getElementHelper().filter(var.getComment(), RobotTokenType.START_HASH_COMMENT));
        sorter.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 2,
                getElementHelper().filter(var.getComment(), RobotTokenType.COMMENT_CONTINUE));

        return sorter;
    }
}

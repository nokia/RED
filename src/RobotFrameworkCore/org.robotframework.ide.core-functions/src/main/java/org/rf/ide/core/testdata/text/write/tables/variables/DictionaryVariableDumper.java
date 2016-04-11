/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables.variables;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.RobotElementsComparatorWithPositionChangedPresave;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.tables.ANotExecutableTableElementDumper;

public class DictionaryVariableDumper extends ANotExecutableTableElementDumper {

    public DictionaryVariableDumper(final DumperHelper aDumpHelper) {
        super(aDumpHelper, ModelType.DICTIONARY_VARIABLE_DECLARATION_IN_TABLE);
    }

    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            final AModelElement<? extends ARobotSectionTable> currentElement) {
        DictionaryVariable var = (DictionaryVariable) currentElement;

        List<RobotToken> itemsAsValue = new ArrayList<>(0);
        for (final DictionaryKeyValuePair dv : var.getItems()) {
            RobotToken key = dv.getKey();
            if (!key.isDirty() && !dv.getValue().isDirty() && !dv.getRaw().getRaw().isEmpty()) {
                itemsAsValue.add(dv.getRaw());
            } else {
                RobotToken joinedKeyValue = new RobotToken();
                joinedKeyValue.setStartOffset(key.getStartOffset());
                joinedKeyValue.setLineNumber(key.getLineNumber());
                joinedKeyValue.setStartColumn(key.getStartColumn());
                joinedKeyValue.setRaw(key.getText() + "=" + dv.getValue().getText());
                joinedKeyValue.setText(joinedKeyValue.getRaw());

                itemsAsValue.add(joinedKeyValue);
            }
        }

        RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();
        sorter.addPresaveSequenceForType(RobotTokenType.VARIABLES_VARIABLE_VALUE, 1, itemsAsValue);
        sorter.addPresaveSequenceForType(RobotTokenType.START_HASH_COMMENT, 2,
                getElementHelper().filter(var.getComment(), RobotTokenType.START_HASH_COMMENT));
        sorter.addPresaveSequenceForType(RobotTokenType.COMMENT_CONTINUE, 2,
                getElementHelper().filter(var.getComment(), RobotTokenType.COMMENT_CONTINUE));

        return sorter;
    }
}

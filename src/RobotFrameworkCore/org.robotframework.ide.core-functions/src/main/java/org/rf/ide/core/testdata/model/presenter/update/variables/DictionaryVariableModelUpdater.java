/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.variables;

import java.util.List;

import org.rf.ide.core.testdata.model.presenter.update.IVariableTableElementOperation;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class DictionaryVariableModelUpdater implements IVariableTableElementOperation<DictionaryKeyValuePair> {

    @Override
    public boolean ableToHandle(final AVariable dst) {
        return (dst instanceof DictionaryVariable);
    }

    @Override
    public List<DictionaryKeyValuePair> convert(final List<?> p) {
        return VariablesValueConverter.convert(p, DictionaryKeyValuePair.class);
    }

    @Override
    public void addOrSet(final AVariable dst, final int startIndexForModification,
            final List<DictionaryKeyValuePair> toAdd) {
        final DictionaryVariable var = (DictionaryVariable) dst;

        final List<DictionaryKeyValuePair> items = var.getItems();

        while (startIndexForModification > items.size()) {
            var.put(new RobotToken(), new RobotToken(), new RobotToken());
        }

        int nrOfEntriesToModify = toAdd.size();
        for (int i = 0; i < nrOfEntriesToModify; i++) {
            DictionaryKeyValuePair dVPtoAdd = toAdd.get(i);
            if (items.size() > startIndexForModification + i) {
                DictionaryKeyValuePair dVP = var.getItems().get(startIndexForModification + i);
                dVP.getRaw().setText(dVPtoAdd.getRaw().getText());
                dVP.getKey().setText(dVPtoAdd.getKey().getText());
                dVP.getValue().setText(dVPtoAdd.getValue().getText());
            } else {
                var.put(dVPtoAdd.getRaw(), dVPtoAdd.getKey(), dVPtoAdd.getValue());
            }
        }
    }
}

/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.mapping;

import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.testdata.model.table.variables.mapping.DictionaryVariableMapper;
import org.rf.ide.core.testdata.model.table.variables.mapping.DictionaryVariableValueMapper;
import org.rf.ide.core.testdata.model.table.variables.mapping.ListVariableMapper;
import org.rf.ide.core.testdata.model.table.variables.mapping.ListVariableValueMapper;
import org.rf.ide.core.testdata.model.table.variables.mapping.ScalarVariableMapper;
import org.rf.ide.core.testdata.model.table.variables.mapping.ScalarVariableValueMapper;


public class VariablesDeclarationMapperProvider {

    private static final List<IParsingMapper> mappers = Arrays
            .asList(new ScalarVariableMapper(),
                    new ScalarVariableValueMapper(), new ListVariableMapper(),
                    new ListVariableValueMapper(),
                    new DictionaryVariableMapper(),
                    new DictionaryVariableValueMapper());


    public List<IParsingMapper> getMappers() {
        return mappers;
    }
}

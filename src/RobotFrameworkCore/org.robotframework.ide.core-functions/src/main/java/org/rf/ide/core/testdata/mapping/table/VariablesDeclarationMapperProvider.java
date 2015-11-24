/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.table;

import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.testdata.mapping.variables.DictionaryVariableMapper;
import org.rf.ide.core.testdata.mapping.variables.DictionaryVariableValueMapper;
import org.rf.ide.core.testdata.mapping.variables.ListVariableMapper;
import org.rf.ide.core.testdata.mapping.variables.ListVariableValueMapper;
import org.rf.ide.core.testdata.mapping.variables.ScalarVariableMapper;
import org.rf.ide.core.testdata.mapping.variables.ScalarVariableValueMapper;


public class VariablesDeclarationMapperProvider {

    private static final List<IParsingMapper> MAPPERS = Arrays
            .asList(new ScalarVariableMapper(),
                    new ScalarVariableValueMapper(), new ListVariableMapper(),
                    new ListVariableValueMapper(),
                    new DictionaryVariableMapper(),
                    new DictionaryVariableValueMapper());


    public List<IParsingMapper> getMappers() {
        return MAPPERS;
    }
}

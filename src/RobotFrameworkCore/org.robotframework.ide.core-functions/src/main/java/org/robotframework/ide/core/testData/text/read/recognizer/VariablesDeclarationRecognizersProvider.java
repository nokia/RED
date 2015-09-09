/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.recognizer;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.recognizer.header.VariablesTableHeaderRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.variables.DictionaryVariableDeclarationRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.variables.ListVariableDeclarationRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.variables.ScalarVariableDeclarationRecognizer;


public class VariablesDeclarationRecognizersProvider {

    private static volatile List<ATokenRecognizer> recognized = new LinkedList<>();
    static {
        recognized.add(new VariablesTableHeaderRecognizer());
        recognized.add(new ScalarVariableDeclarationRecognizer());
        recognized.add(new ListVariableDeclarationRecognizer());
        recognized.add(new DictionaryVariableDeclarationRecognizer());
    }


    public List<ATokenRecognizer> getRecognizers() {
        return recognized;
    }
}

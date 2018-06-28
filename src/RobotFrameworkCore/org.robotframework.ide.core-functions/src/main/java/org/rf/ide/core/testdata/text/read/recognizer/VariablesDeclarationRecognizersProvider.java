/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.text.read.recognizer.header.VariablesTableHeaderRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.variables.DictionaryVariableDeclarationRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.variables.EnvironmentVariableDeclarationRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.variables.ListVariableDeclarationRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.variables.ScalarVariableDeclarationRecognizer;

public class VariablesDeclarationRecognizersProvider {

    private static volatile List<ATokenRecognizer> recognized = new ArrayList<>();
    static {
        recognized.add(new VariablesTableHeaderRecognizer());
        recognized.add(new ScalarVariableDeclarationRecognizer());
        recognized.add(new ListVariableDeclarationRecognizer());
        recognized.add(new DictionaryVariableDeclarationRecognizer());
        recognized.add(new EnvironmentVariableDeclarationRecognizer());
    }

    public List<ATokenRecognizer> getRecognizers() {
        final List<ATokenRecognizer> recognizersProvided = new ArrayList<>();
        synchronized (recognized) {
            for (final ATokenRecognizer rec : recognized) {
                recognizersProvided.add(rec.newInstance());
            }
        }
        return recognizersProvided;
    }
}

/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.text.read.recognizer.variables.DictionaryVariableDeclarationRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.variables.EnvironmentVariableDeclarationRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.variables.ListVariableDeclarationRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.variables.ScalarVariableDeclarationRecognizer;

public class VariablesDeclarationRecognizersProvider {

    private static final List<ATokenRecognizer> RECOGNIZERS = new ArrayList<>();
    static {
        RECOGNIZERS.add(new ScalarVariableDeclarationRecognizer());
        RECOGNIZERS.add(new ListVariableDeclarationRecognizer());
        RECOGNIZERS.add(new DictionaryVariableDeclarationRecognizer());
        RECOGNIZERS.add(new EnvironmentVariableDeclarationRecognizer());
    }

    public List<ATokenRecognizer> getRecognizers() {
        final List<ATokenRecognizer> recognizersProvided = new ArrayList<>();
        synchronized (RECOGNIZERS) {
            for (final ATokenRecognizer rec : RECOGNIZERS) {
                recognizersProvided.add(rec.newInstance());
            }
        }
        return recognizersProvided;
    }
}

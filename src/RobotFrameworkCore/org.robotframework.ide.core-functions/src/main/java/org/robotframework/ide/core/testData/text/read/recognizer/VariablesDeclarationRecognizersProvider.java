package org.robotframework.ide.core.testData.text.read.recognizer;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.recognizer.variables.DictionaryVariableDeclarationRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.variables.ListVariableDeclarationRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.variables.ScalarVariableDeclarationRecognizer;


public class VariablesDeclarationRecognizersProvider {

    private static volatile List<ATokenRecognizer> recognized = new LinkedList<>();
    static {
        recognized.add(new ScalarVariableDeclarationRecognizer());
        recognized.add(new ListVariableDeclarationRecognizer());
        recognized.add(new DictionaryVariableDeclarationRecognizer());
    }


    public List<ATokenRecognizer> getRecognizers() {
        return recognized;
    }
}

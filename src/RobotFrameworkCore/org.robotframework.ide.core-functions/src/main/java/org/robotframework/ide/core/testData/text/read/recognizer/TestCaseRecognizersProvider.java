package org.robotframework.ide.core.testData.text.read.recognizer;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.recognizer.testCases.TestDocumentationRecognizer;


public class TestCaseRecognizersProvider {

    private static volatile List<ATokenRecognizer> recognized = new LinkedList<>();
    static {
        recognized.add(new TestDocumentationRecognizer());
    }


    public List<ATokenRecognizer> getRecognizers() {
        return recognized;
    }
}

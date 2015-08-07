package org.robotframework.ide.core.testData.text.read.recognizer;

import java.util.LinkedList;
import java.util.List;


public class UserKeywordRecognizersProvider {

    private static volatile List<ATokenRecognizer> recognized = new LinkedList<>();
    static {

    }


    public List<ATokenRecognizer> getRecognizers() {
        return recognized;
    }
}

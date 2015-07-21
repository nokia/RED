package org.robotframework.ide.core.testData.text.read.recognizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken.RobotTokenType;


public class ATokenRecognizer {

    private final Pattern pattern;
    private Matcher m;
    private int lineNumber = -1;
    private final RobotTokenType type;
    private String text;


    protected ATokenRecognizer(final Pattern p, final RobotTokenType type) {
        this.pattern = p;
        this.type = type;
    }


    public boolean hasNext(String text, int lineNumber) {
        this.text = text;
        this.lineNumber = lineNumber;
        m = pattern.matcher(text);
        return m.find();
    }


    public RobotToken next() {
        RobotToken t = new RobotToken();
        t.setLineNumber(lineNumber);
        if (m.find()) {
            int start = m.start();
            t.setLineNumber(start);
            int end = m.end();
            t.setType(type);
            t.setText(new StringBuilder().append(text.substring(start, end)));
            t.setType(getProducedType());
        }

        return t;
    }


    public RobotTokenType getProducedType() {
        return type;
    }
}

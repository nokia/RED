package org.robotframework.ide.core.testData.text.read.columnSeparators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator.SeparatorType;


public class PipeSeparator extends ALineSeparator {

    private static final Pattern PIPE_SEPARATOR = Pattern
            .compile("(^[ ]?[|]([ ]|\\t)+)|(([ ]|\\t)+[|]([ ]|\\t)+)|(([ ]|\\t)+[|]([ ]|\\t)*$)");
    private final Matcher matcher;


    public PipeSeparator(int lineNumber, String line) {
        super(lineNumber, line);
        this.matcher = PIPE_SEPARATOR.matcher(line);
    }


    @Override
    public Separator next() {
        int start = matcher.start();
        int end = matcher.end();

        Separator s = new Separator();
        s.setType(SeparatorType.PIPE);
        s.setStartColumn(start);
        s.setText(new StringBuilder().append(line.substring(start, end)));
        s.setLineNumber(getLineNumber());

        return s;
    }


    @Override
    public boolean hasNext() {
        return matcher.find();
    }
}

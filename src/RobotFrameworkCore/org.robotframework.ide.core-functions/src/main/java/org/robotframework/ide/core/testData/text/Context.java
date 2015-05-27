package org.robotframework.ide.core.testData.text;

import java.util.LinkedList;
import java.util.List;


public enum Context {
    SEPARATOR;

    private List<RobotToken> tokensIncluded = new LinkedList<>();


    public void addToken(final RobotToken token) {
        this.tokensIncluded.add(token);
    }


    public List<RobotToken> getTokens() {
        return tokensIncluded;
    }
}

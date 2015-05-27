package org.robotframework.ide.core.testData.text;

import java.util.LinkedList;
import java.util.List;


public class RobotTokenContext {

    private Context ctx;


    public RobotTokenContext(final Context ctx) {
        this.ctx = ctx;
    }

    private List<RobotToken> tokensIncluded = new LinkedList<>();


    public void addToken(final RobotToken token) {
        this.tokensIncluded.add(token);
    }


    public List<RobotToken> getTokens() {
        return tokensIncluded;
    }


    public Context getContext() {
        return ctx;
    }
}

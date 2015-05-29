package org.robotframework.ide.core.testData.text;

import java.util.LinkedList;
import java.util.List;


public class RobotTokenContext {

    private ContextType ctx = ContextType.UNKNOWN;
    private List<Integer> tokensId = new LinkedList<>();


    public RobotTokenContext(final ContextType ctx) {
        this.ctx = ctx;
    }


    public void addToken(final int tokenId) {
        tokensId.add(tokenId);
    }


    public List<Integer> getTokensId() {
        return tokensId;
    }


    public ContextType getContext() {
        return ctx;
    }


    public void changeContext(final ContextType ctx) {
        this.ctx = ctx;
    }


    @Override
    public String toString() {
        return String.format("RobotTokenContext [context=%s, tokensId=%s]",
                ctx, tokensId);
    }
}

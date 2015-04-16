package org.robotframework.ide.core.testData.parser.util.lexer;

import java.util.LinkedList;
import java.util.List;


/**
 * Represents single matching status.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class MatchResult {

    private MatchResult parent;
    private final IMatcher usedMatcher;
    private MatchStatus status;
    private final Position matchPosition = new Position();
    private final List<MatchResult> subResults = new LinkedList<MatchResult>();
    private final List<String> messages = new LinkedList<String>();


    public MatchResult(final IMatcher usedMatcher, MatchStatus status) {
        this.usedMatcher = usedMatcher;
        this.status = status;
        setParent(null);
    }


    private MatchResult(final MatchResult parent, final IMatcher usedMatcher,
            MatchStatus status) {
        this.usedMatcher = usedMatcher;
        this.status = status;
        setParent(parent);
    }


    public void addSubResult(final IMatcher subMatcher, MatchStatus subStatus) {
        this.subResults.add(new MatchResult(this, subMatcher, subStatus));
    }


    public void addSubResult(final MatchResult subResult) {
        subResult.setParent(this);
        this.subResults.add(subResult);
    }


    /**
     * 
     * @param message
     *            an information about matching
     */
    public void addMessage(String message) {
        this.messages.add(message);
    }


    /**
     * 
     * @return all matching messages
     */
    public List<String> getMessages() {
        return this.messages;
    }


    public List<MatchResult> getSubResults() {
        return subResults;
    }


    private void setParent(final MatchResult parentResult) {
        this.parent = parentResult;
    }


    /**
     * 
     * @return {@code null} or parent matcher in case this matching result was
     *         sub result
     */
    public MatchResult getParent() {
        return this.parent;
    }


    public boolean hasParent() {
        return this.parent != null;
    }


    public IMatcher getMatcher() {
        return this.usedMatcher;
    }


    public Position getPosition() {
        return this.matchPosition;
    }


    public void setStatus(MatchStatus status) {
        this.status = status;
    }


    public MatchStatus getStatus() {
        return this.status;
    }

    /**
     * Informs about status of matching performed
     * 
     * @author wypych
     * @serial RobotFramework 2.8.6
     * @serial 1.0
     * 
     */
    public static enum MatchStatus {
        NOT_FOUND, FOUND, IMMEDIATELLY_BREAK
    }
}

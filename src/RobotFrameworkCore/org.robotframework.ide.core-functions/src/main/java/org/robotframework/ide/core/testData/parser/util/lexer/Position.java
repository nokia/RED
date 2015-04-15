package org.robotframework.ide.core.testData.parser.util.lexer;

/**
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class Position {

    public static final int NOT_DECLARED = -1;

    private int start = NOT_DECLARED;
    private int end = NOT_DECLARED;


    /**
     * 
     * @param start
     * @param end
     */
    public Position(final int start, final int end) {
        this.start = start;
        this.end = end;
    }


    public Position() {
        this(NOT_DECLARED, NOT_DECLARED);
    }


    public int getStart() {
        return start;
    }


    public void setStart(int start) {
        this.start = start;
    }


    public int getEnd() {
        return end;
    }


    public void setEnd(int end) {
        this.end = end;
    }
}

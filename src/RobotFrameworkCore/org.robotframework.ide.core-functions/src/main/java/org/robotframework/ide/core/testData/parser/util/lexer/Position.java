package org.robotframework.ide.core.testData.parser.util.lexer;

/**
 * Contains information about start and end position to match or matching
 * criteria.
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


    public Position(final int start, final int end) {
        this.start = start;
        this.end = end;
    }


    /**
     * Initialization with both {@code start} and {@code end} set to
     * {@link #NOT_DECLARED}
     */
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


    @Override
    public String toString() {
        return "Position [start=" + start + ", end=" + end + "]";
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + end;
        result = prime * result + start;
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Position other = (Position) obj;
        if (end != other.end)
            return false;
        if (start != other.start)
            return false;
        return true;
    }
}

package org.robotframework.ide.core.execution;

/**
 * @author mmarzec
 */
public class ExecutionElement {

    private String name;

    private String source;

    private int elapsedTime;

    private String message;

    private String status;

    private ExecutionElementType type;

    public enum ExecutionElementType {
        SUITE,
        TEST
    }

    public ExecutionElement(String name, ExecutionElementType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ExecutionElementType getType() {
        return type;
    }

}

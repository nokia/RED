package org.robotframework.ide.eclipse.main.plugin.debug.utils;

/**
 * @author mmarzec
 */
public class BreakpointContext {

    private String file;

    private int line = 0;

    public BreakpointContext() {

    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public void setContext(String file, int line) {
        this.file = file;
        this.line = line;
    }
}

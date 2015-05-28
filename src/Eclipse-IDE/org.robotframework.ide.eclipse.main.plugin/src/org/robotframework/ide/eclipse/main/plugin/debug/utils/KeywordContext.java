package org.robotframework.ide.eclipse.main.plugin.debug.utils;

import java.util.Map;

/**
 * @author mmarzec
 *
 */
public class KeywordContext {

    private Map<String, Object> variables;

    private String fileName;
    
    private int lineNumber;

    public KeywordContext(Map<String, Object> variables, String fileName, int lineNumber) {
        this.variables = variables;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }
    
    public KeywordContext() {
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    
}

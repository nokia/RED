package org.robotframework.ide.eclipse.main.plugin.debug;

import java.util.Map;

/**
 * @author mmarzec
 *
 */
public class KeywordContext {

    private Map<String, String> variables;

    private String fileName;
    
    private int lineNumber;

    public KeywordContext(Map<String, String> variables, String fileName, int lineNumber) {
        this.variables = variables;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }
    
    public KeywordContext() {
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
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

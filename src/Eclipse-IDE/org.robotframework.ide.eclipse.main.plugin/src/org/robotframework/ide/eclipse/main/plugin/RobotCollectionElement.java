package org.robotframework.ide.eclipse.main.plugin;

import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.VariableEditFormDialog;


public class RobotCollectionElement {
    
    private VariableEditFormDialog formDialog;

    private int index;

    private String key;

    private String value;

    public RobotCollectionElement(VariableEditFormDialog formDialog, int index, String value) {
        this.formDialog = formDialog;
        this.index = index;
        this.value = value;
    }

    public RobotCollectionElement(VariableEditFormDialog formDialog, int index, String key, String value) {
        this(formDialog, index, value);
        this.key = key;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void incrementIndex() {
        this.index++;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public VariableEditFormDialog getFormDialog() {
        return formDialog;
    }

}
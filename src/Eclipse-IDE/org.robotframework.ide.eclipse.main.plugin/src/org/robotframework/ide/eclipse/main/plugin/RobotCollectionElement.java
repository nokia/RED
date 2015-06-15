package org.robotframework.ide.eclipse.main.plugin;

public class RobotCollectionElement {

    private int index;

    private String key;

    private String value;

    public RobotCollectionElement(int index, String value) {
        this.index = index;
        this.value = value;
    }

    public RobotCollectionElement(int index, String key, String value) {
        this(index, value);
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
}

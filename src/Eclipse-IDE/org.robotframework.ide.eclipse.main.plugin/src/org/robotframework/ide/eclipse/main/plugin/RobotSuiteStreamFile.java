package org.robotframework.ide.eclipse.main.plugin;

import java.io.InputStream;

public class RobotSuiteStreamFile extends RobotSuiteFile {

    private final String name;
    private final InputStream input;
    private final boolean readOnly;

    public RobotSuiteStreamFile(final String name, final InputStream input, final boolean readOnly) {
        super(null, null);
        this.name = name;
        this.input = input;
        this.readOnly = readOnly;
    }

    @Override
    protected FileSectionsParser createParser() {
        return new FileSectionsParser(input, readOnly);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isEditable() {
        return !readOnly;
    }

    @Override
    public void refreshOnFileChange() {
        // do nothing
    }
}

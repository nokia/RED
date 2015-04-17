package org.robotframework.ide.eclipse.main.plugin;

import java.io.InputStream;

public class RobotSuiteStreamFile extends RobotSuiteFile {

    private final InputStream input;
    private final boolean readOnly;

    public RobotSuiteStreamFile(final InputStream input, final boolean readOnly) {
        super(null, null);
        this.input = input;
        this.readOnly = readOnly;
    }

    @Override
    protected FileSectionsParser createParser() {
        return new FileSectionsParser(input, readOnly);
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

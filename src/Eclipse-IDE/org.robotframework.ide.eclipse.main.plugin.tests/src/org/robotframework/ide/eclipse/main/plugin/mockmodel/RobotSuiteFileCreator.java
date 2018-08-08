/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.mockmodel;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteStreamFile;

import com.google.common.base.Joiner;

/**
 * @author Michal Anglart
 *
 */
public class RobotSuiteFileCreator {

    private final List<String> lines = new ArrayList<>();

    private RobotVersion version;

    public RobotSuiteFileCreator setVersion(final RobotVersion version) {
        this.version = version;
        return this;
    }

    public RobotSuiteFileCreator appendLine(final String line) {
        lines.add(line);
        return this;
    }

    public RobotSuiteFileCreator appendLines(final List<String> lines) {
        this.lines.addAll(lines);
        return this;
    }

    public String getContent() {
        return Joiner.on('\n').join(lines);
    }

    public RobotSuiteFile build() {
        return buildModel("file.robot", false);
    }

    public RobotSuiteFile buildTsv() {
        return buildModel("file.tsv", false);
    }

    public RobotSuiteFile buildReadOnly() {
        return buildModel("file.robot", true);
    }

    public RobotSuiteFile buildReadOnlyTsv() {
        return buildModel("file.tsv", true);
    }

    private RobotSuiteFile buildModel(final String filename, final boolean readOnly) {
        final String content = getContent();
        final RobotSuiteStreamFile model = new RobotSuiteStreamFile(filename, content, readOnly);
        if (version != null) {
            model.setRobotVersion(version);
        }
        model.reparseEverything(content);
        return model;
    }
}

/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.NullRuntimeEnvironment;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.project.ASuiteFileDescriber;

public class RobotSuiteStreamFile extends RobotSuiteFile {

    private final String name;

    private final IPath path;

    private final String content;

    private final boolean readOnly;

    private RobotVersion version;

    public RobotSuiteStreamFile(final String name, final IPath path, final String content, final boolean readOnly) {
        super(null, null);
        this.name = name;
        this.path = path;
        this.content = content;
        this.readOnly = readOnly;
    }

    @Override
    public boolean isFromLocalStorage() {
        return true;
    }

    @Override
    public boolean isEditable() {
        return !readOnly;
    }

    @Override
    public String getFileExtension() {
        return path.getFileExtension();
    }

    @Override
    public IPath getFullPath() {
        return path;
    }

    @Override
    protected String getContentTypeId() {
        if (contentTypeId != null) {
            return contentTypeId;
        }
        contentTypeId = ASuiteFileDescriber.getContentType(name, content);
        return contentTypeId;
    }

    @Override
    public List<RobotSuiteFileSection> getSections() {
        return getSections(createReparsingStrategy(""));
    }

    @Override
    public RobotParser createRobotParser() {
        final IRuntimeEnvironment env = RedPlugin.getDefault().getActiveRobotInstallation();
        return new RobotParser(new RobotProjectHolder(env), version == null ? env.getRobotVersion() : version);
    }

    @Override
    public File getRobotParserFile() {
        return new File(name);
    }

    @Override
    protected ParsingStrategy createReparsingStrategy(final String fileContent) {
        return () -> {
            final RobotParser parser = createRobotParser();
            return parser.parseEditorContent(fileContent, getRobotParserFile());
        };
    }

    @Override
    public IRuntimeEnvironment getRuntimeEnvironment() {
        return new NullRuntimeEnvironment();
    }

    @Override
    public String getName() {
        return name;
    }

    public void setRobotVersion(final RobotVersion version) {
        this.version = version;
    }

    @Override
    public void refreshOnFileChange() {
        // do nothing
    }
}

/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import java.io.File;
import java.util.List;

import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.project.ImportSearchPaths.PathsProvider;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.project.ASuiteFileDescriber;

public class RobotSuiteStreamFile extends RobotSuiteFile {

    private final String name;

    private final String content;

    private final boolean readOnly;

    private RobotVersion version;

    public RobotSuiteStreamFile(final String name, final String content, final boolean readOnly) {
        super(null, null);
        this.name = name;
        this.content = content;
        this.readOnly = readOnly;
    }

    @Override
    public String getFileExtension() {
        return name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : null;
    }


    @Override
    protected String getContentTypeId() {
        final String id = super.getContentTypeId();
        if (id != null) {
            return id;
        }
        contentTypeId = ASuiteFileDescriber.getContentType(name, content);
        return contentTypeId;
    }

    @Override
    public List<RobotSuiteFileSection> getSections() {
        return getSections(createReparsingStrategy(""));
    }

    @Override
    public RobotVersion getRobotParserComplianceVersion() {
        if (version != null) {
            return version;
        }
        final RobotRuntimeEnvironment env = RedPlugin.getDefault().getActiveRobotInstallation();
        return env == null ? RobotVersion.UNKNOWN : RobotVersion.from(env.getVersion());
    }

    @Override
    protected ParsingStrategy createReparsingStrategy(final String newContent) {
        return () -> {
            final RobotRuntimeEnvironment env = RedPlugin.getDefault().getActiveRobotInstallation();
            final RobotParser parser = RobotParser.create(new RobotProjectHolder(env),
                    getRobotParserComplianceVersion(), (PathsProvider) null);
            return parser.parseEditorContent(newContent, new File(name));
        };
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isEditable() {
        return !readOnly;
    }

    public void setRobotVersion(final RobotVersion version) {
        this.version = version;
    }

    @Override
    public void refreshOnFileChange() {
        // do nothing
    }
}

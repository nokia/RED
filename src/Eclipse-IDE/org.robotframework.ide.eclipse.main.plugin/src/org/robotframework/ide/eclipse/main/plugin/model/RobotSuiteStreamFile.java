/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import java.io.File;
import java.util.List;
import java.util.function.Function;

import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.NullRuntimeEnvironment;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.RobotParser.RobotParserConfig;
import org.rf.ide.core.testdata.model.RobotProjectHolder;
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
    public RobotParser createRobotParser(final Function<RobotVersion, RobotParserConfig> configMapper) {
        final IRuntimeEnvironment env = RedPlugin.getDefault().getActiveRobotInstallation();
        final RobotParserConfig parserConfig = configMapper.apply(version == null ? env.getRobotVersion() : version);
        return new RobotParser(new RobotProjectHolder(env), parserConfig);
    }

    @Override
    public File getRobotParserFile() {
        return new File(name);
    }

    @Override
    protected ParsingStrategy createReparsingStrategy(final String fileContent) {
        return () -> {
            final RobotParser parser = createRobotParser(RobotParserConfig::new);
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

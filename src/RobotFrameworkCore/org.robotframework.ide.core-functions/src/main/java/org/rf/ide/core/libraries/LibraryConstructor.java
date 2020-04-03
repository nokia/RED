/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.libraries;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;

/**
 * @author Michal Anglart
 *
 */
@XmlRootElement(namespace = "org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification")
public class LibraryConstructor {

    public static LibraryConstructor create(final String documentation, final List<String> arguments) {
        final LibraryConstructor constructor = new LibraryConstructor();
        constructor.setDocumentation(documentation);
        constructor.setArguments(arguments);
        return constructor;
    }

    public static LibraryConstructor createDefaultForStandardRemote() {
        final LibraryConstructor constructor = new LibraryConstructor();
        constructor.setArguments(newArrayList("uri=" + RemoteLocation.DEFAULT_ADDRESS, "timeout=None"));
        return constructor;
    }

    private String documentation;

    private List<String> arguments;

    private String sourcePath;
    private Integer lineNumber;

    public String getDocumentation() {
        return documentation;
    }

    @XmlElement(name = "doc")
    public void setDocumentation(final String documentation) {
        this.documentation = documentation;
    }

    public List<String> getArguments() {
        return arguments;
    }

    @XmlElementWrapper(name = "arguments")
    @XmlElement(name = "arg")
    public void setArguments(final List<String> arguments) {
        this.arguments = arguments;
    }

    public ArgumentsDescriptor createArgumentsDescriptor() {
        return ArgumentsDescriptor.createDescriptor(arguments);
    }

    @XmlAttribute(name = "source")
    public void setSourcePath(final String path) {
        this.sourcePath = path;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public Optional<File> getSource() {
        return Optional.ofNullable(sourcePath).map(File::new);
    }

    @XmlAttribute(name = "lineno")
    public void setLineNumber(final Integer lineNo) {
        this.lineNumber = lineNo;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }
}

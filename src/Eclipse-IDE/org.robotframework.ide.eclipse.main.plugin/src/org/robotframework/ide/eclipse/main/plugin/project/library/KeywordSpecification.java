/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.library;

import java.util.List;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;

@XmlRootElement(namespace = "org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification")
public class KeywordSpecification {

    public static KeywordSpecification create(final String name) {
        final KeywordSpecification spec = new KeywordSpecification();
        spec.setName(name);
        return spec;
    }

    private String name;
    private String format;
    private String documentation;

    private List<String> arguments;

    private Boolean isDeprecated;

    public String getName() {
        return name;
    }

    @XmlAttribute
    public void setName(final String name) {
        this.name = name;
    }

    public void setFormat(final String format) {
        this.format = format;
    }

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

    public boolean canBeConvertedToHtml() {
        return "ROBOT".equals(format);
    }

    public String getDocumentationAsHtml() {
        if ("ROBOT".equals(format)) {
            return new RobotToHtmlConverter().convert(documentation);
        }
        throw new IllegalArgumentException("Only ROBOT format can be converted to HTML");
    }

    public ArgumentsDescriptor createArgumentsDescriptor() {
        return ArgumentsDescriptor.createDescriptor(arguments);
    }

    public boolean isDeprecated() {
        if (isDeprecated == null) {
            isDeprecated = Boolean.valueOf(documentation != null
                    && Pattern.compile("^\\*deprecated[^\\n\\r]*\\*.*").matcher(documentation.toLowerCase()).find());
        }
        return isDeprecated.booleanValue();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (KeywordSpecification.class == obj.getClass()) {
            final KeywordSpecification that = (KeywordSpecification) obj;
            return Objects.equal(this.name, that.name) && Objects.equal(this.arguments, that.arguments);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, arguments);
    }
}

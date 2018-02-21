/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.libraries;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.google.common.base.Objects;

@XmlRootElement(name = "keywordspec")
public class LibrarySpecification {

    public static String getVersion(final File file) {
        return LibrarySpecificationReader.readSpecification(file).getVersion();
    }

    public static LibrarySpecification create(final String name, final KeywordSpecification... keywords) {
        final LibrarySpecification spec = new LibrarySpecification();
        spec.setName(name);
        for (final KeywordSpecification kw : keywords) {
            spec.keywords.add(kw);
        }
        return spec;
    }

    @XmlTransient
    private LibraryDescriptor descriptor; // descriptor used to generate this spec
    
    @XmlTransient
    private boolean isModified;

    private String name;

    private String scope;

    private String format;

    private String version;

    private LibraryConstructor constructor;

    private String documentation;

    private List<KeywordSpecification> keywords = new ArrayList<>();

    public String getName() {
        return name;
    }

    @XmlAttribute
    public void setName(final String name) {
        this.name = name;
    }

    public String getScope() {
        return scope;
    }

    @XmlElement
    public void setScope(final String scope) {
        this.scope = scope;
    }

    public String getFormat() {
        return format;
    }

    @XmlAttribute
    public void setFormat(final String format) {
        this.format = format;
    }

    public String getVersion() {
        return version;
    }

    @XmlElement
    public void setVersion(final String version) {
        this.version = version;
    }

    public String getDocumentation() {
        return documentation;
    }

    @XmlElement(name = "doc")
    public void setDocumentation(final String documentation) {
        this.documentation = documentation;
    }

    public List<KeywordSpecification> getKeywords() {
        return keywords;
    }

    @XmlElement(name = "kw", required = false)
    public void setKeywords(final List<KeywordSpecification> keywords) {
        this.keywords = keywords;
    }

    public LibraryConstructor getConstructor() {
        return constructor;
    }

    @XmlElement(name = "init")
    public void setConstructor(final LibraryConstructor constructor) {
        this.constructor = constructor;
    }

    public void propagateFormat() {
        if (keywords != null) {
            for (final KeywordSpecification kwSpec : keywords) {
                kwSpec.setFormat(format);
            }
        }
    }

    public boolean isAccessibleWithoutImport() {
        return Arrays.asList("BuiltIn", "Easter", "Reserved").contains(name);
    }

    public boolean isReserved() {
        return "Reserved".equals(name);
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

    @XmlTransient
    public void setDescriptor(final LibraryDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public LibraryDescriptor getDescriptor() {
        return descriptor;
    }

    public boolean isModified() {
        return isModified;
    }
    
    @XmlTransient
    public void setIsModified(final boolean isModified) {
        this.isModified = isModified;
    }
    
    public boolean equalsIgnoreKeywords(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (LibrarySpecification.class == obj.getClass()) {
            final LibrarySpecification that = (LibrarySpecification) obj;
            return Objects.equal(this.name, that.name) && Objects.equal(this.version, that.version)
                    && Objects.equal(this.descriptor, that.descriptor);
        }
        return false;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (LibrarySpecification.class == obj.getClass()) {
            final LibrarySpecification that = (LibrarySpecification) obj;
            return Objects.equal(this.name, that.name) && Objects.equal(this.keywords, that.keywords)
                    && Objects.equal(this.version, that.version) && Objects.equal(this.descriptor, that.descriptor);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, keywords, version, descriptor);
    }
}

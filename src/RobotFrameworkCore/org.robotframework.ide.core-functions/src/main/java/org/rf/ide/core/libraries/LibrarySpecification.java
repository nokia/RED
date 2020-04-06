/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.libraries;

import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.rf.ide.core.libraries.Documentation.DocFormat;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

@XmlRootElement(name = "keywordspec")
public class LibrarySpecification {

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

    private Integer specVersion;

    private String name;

    private String scopeElem;

    private String scopeAttr;

    private String format;

    private String version;

    private String sourcePath;
    private Integer lineNumber;

    private LibraryConstructor constructor;

    private String documentation;

    private List<KeywordSpecification> keywords = new ArrayList<>();

    @XmlAttribute(name = "specversion")
    public void setSpecVersion(final Integer version) {
        this.specVersion = version;
    }

    public Integer getSpecVersion() {
        return specVersion;
    }

    public int getSpecificationVersion() {
        return specVersion == null ? 1 : specVersion;
    }

    public String getName() {
        return name;
    }

    @XmlAttribute
    public void setName(final String name) {
        this.name = name;
    }

    public String getScopeElem() {
        return scopeElem;
    }

    @XmlElement(name = "scope")
    public void setScopeElem(final String scope) {
        this.scopeElem = scope;
    }

    public String getScopeAttr() {
        return scopeAttr;
    }

    @XmlAttribute(name = "scope")
    public void setScopeAttr(final String scope) {
        this.scopeAttr = scope;
    }

    public String getScope() {
        return scopeAttr != null ? scopeAttr : scopeElem;
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

    public Stream<KeywordSpecification> getKeywordsStream() {
        return keywords == null ? Stream.empty() : keywords.stream();
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

    public boolean isAccessibleWithoutImport() {
        return Arrays.asList("BuiltIn", "Easter", "Reserved").contains(name);
    }

    public boolean isReserved() {
        return "Reserved".equals(name);
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

    public ArgumentsDescriptor createArgumentsDescriptor() {
        return constructor == null ? ArgumentsDescriptor.createDescriptor() : constructor.createArgumentsDescriptor();
    }

    public Documentation createDocumentation() {
        return new Documentation(DocFormat.valueOf(format), documentation, getKeywordNames());
    }

    public Documentation createConstructorDocumentation() {
        Preconditions.checkState(constructor != null);
        return new Documentation(DocFormat.valueOf(format), constructor.getDocumentation(), getKeywordNames());
    }

    public Documentation createKeywordDocumentation(final String keywordName) {
        final KeywordSpecification spec = getKeywordsStream().filter(kw -> kw.getName().equals(keywordName))
                .findFirst()
                .orElse(null);
        Preconditions.checkState(spec != null);
        return new Documentation(DocFormat.valueOf(format), spec.getDocumentation(), getKeywordNames());
    }

    public Collection<String> getKeywordNames() {
        return getKeywordsStream().map(KeywordSpecification::getName).collect(toSet());
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

    public static enum LibdocFormat {
        XML,
        HTML
    }
}

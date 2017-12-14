/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.library;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;

import com.google.common.base.Objects;

@XmlRootElement(name = "keywordspec")
public class LibrarySpecification {

    public static String getVersion(final IFile libspecFile) {
        return LibrarySpecificationReader.readSpecification(libspecFile).getVersion();
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
    private IFile sourceFile;

    @XmlTransient
    private RemoteLocation remoteLocation;

    @XmlTransient
    private ReferencedLibrary referencedLibrary;
    
    @XmlTransient
    private boolean isModified;

    private String name;

    private String scope;

    private String format;

    private String version;

    private LibraryConstructor constructor;

    private String documentation;

    private List<KeywordSpecification> keywords = new ArrayList<>();

    private String secondaryKey = "";

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

    public String getSecondaryKey() {
        return secondaryKey;
    }

    public void setSecondaryKey(final String key) {
        this.secondaryKey = key;
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

    public KeywordSpecification getKeywordSpecification(final String keywordName) {
        if (keywords == null) {
            return null;
        }
        for (final KeywordSpecification keywordSpec : keywords) {
            if (keywordSpec.getName().equals(keywordName)) {
                return keywordSpec;
            }
        }
        return null;
    }

    @XmlTransient
    public void setSourceFile(final IFile sourceFile) {
        this.sourceFile = sourceFile;
    }

    public IFile getSourceFile() {
        return sourceFile;
    }

    @XmlTransient
    public void setRemoteLocation(final RemoteLocation location) {
        this.remoteLocation = location;
    }

    public RemoteLocation getRemoteLocation() {
        return remoteLocation;
    }

    public boolean isRemote() {
        return remoteLocation != null;
    }

    @XmlTransient
    public void setReferenced(final ReferencedLibrary library) {
        this.referencedLibrary = library;
    }

    public ReferencedLibrary getReferencedLibrary() {
        return referencedLibrary;
    }

    public boolean isReferenced() {
        return referencedLibrary != null;
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
            return Objects.equal(this.name, that.name) && Objects.equal(this.secondaryKey, that.secondaryKey)
                    && Objects.equal(this.version, that.version)
                    && Objects.equal(this.referencedLibrary, that.referencedLibrary)
                    && Objects.equal(this.remoteLocation, that.remoteLocation);
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
            return Objects.equal(this.name, that.name) && Objects.equal(this.secondaryKey, that.secondaryKey)
                    && Objects.equal(this.keywords, that.keywords) && Objects.equal(this.version, that.version)
                    && Objects.equal(this.referencedLibrary, that.referencedLibrary)
                    && Objects.equal(this.remoteLocation, that.remoteLocation);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, secondaryKey, keywords, version, referencedLibrary, remoteLocation);
    }
}

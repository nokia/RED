/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.locators;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IPath;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor;

import com.google.common.base.Objects;

/**
 * @author Michal Anglart
 */
public abstract class KeywordEntity {

    private final KeywordScope scope;

    private final String sourceName;

    protected final Optional<String> sourceAlias;

    private final String keywordName;

    private final boolean isDeprecated;

    private final ArgumentsDescriptor argumentsDescriptor;

    private final IPath exposingFilepath;

    protected KeywordEntity(final KeywordScope scope, final String sourceName, final String keywordName,
            final Optional<String> libraryAlias, final boolean isDeprecated,
            final ArgumentsDescriptor argumentsDescriptor,
            final IPath exposingFilepath) {
        this.scope = scope;
        this.sourceName = sourceName;
        this.keywordName = keywordName;
        this.sourceAlias = libraryAlias;
        this.isDeprecated = isDeprecated;
        this.argumentsDescriptor = argumentsDescriptor;
        this.exposingFilepath = exposingFilepath;
    }

    public KeywordScope getScope(final IPath useplaceFilepath) {
        if (scope != null) {
            return scope;
        } else {
            return useplaceFilepath.equals(exposingFilepath) ? KeywordScope.LOCAL : KeywordScope.RESOURCE;
        }
    }

    public String getSourceNameInUse() {
        return sourceAlias.orElse(sourceName);
    }

    public List<String> getPossibleQualifiers() {
        final List<String> qualifiers = newArrayList(getSourceName());

        final KeywordScope scope = getScope(exposingFilepath);
        if (sourceAlias.isPresent() && (scope == KeywordScope.REF_LIBRARY || scope == KeywordScope.STD_LIBRARY)) {
            // libraries can be also prefixed using source alias
            qualifiers.add(0, sourceAlias.get());
        }
        return qualifiers;
    }

    public String getNameFromDefinition() {
        return keywordName;
    }

    public IPath getExposingFilepath() {
        return exposingFilepath;
    }

    public boolean isDeprecated() {
        return isDeprecated;
    }

    public String getSourceName() {
        return sourceName;
    }

    public ArgumentsDescriptor getArgumentsDescriptor() {
        return argumentsDescriptor;
    }

    public boolean isSameAs(final KeywordEntity that, final IPath useplaceFilepath) {
        return Objects.equal(this.getSourceNameInUse(), that.getSourceNameInUse())
                && Objects.equal(this.keywordName, that.keywordName)
                && this.getScope(useplaceFilepath) == that.getScope(useplaceFilepath)
                && Objects.equal(this.sourceAlias, that.sourceAlias);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() == getClass()) {
            final KeywordEntity that = (KeywordEntity) obj;
            return Objects.equal(this.sourceName, that.sourceName) && Objects.equal(this.sourceAlias, that.sourceAlias)
                    && Objects.equal(this.keywordName, that.keywordName) && this.scope == that.scope
                    && this.isDeprecated == that.isDeprecated && this.exposingFilepath == that.exposingFilepath;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(scope, sourceName, sourceAlias, keywordName, isDeprecated, exposingFilepath);
    }
}

/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.robotframework.ide.eclipse.main.plugin.model.KeywordScope;
import org.robotframework.ide.eclipse.main.plugin.model.locators.AccessibleKeywordsEntities;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.annotations.VisibleForTesting;

public class FileValidationContext extends AccessibleKeywordsEntities {

    private final ValidationContext context;

    private final IFile file;

    private Set<String> accessibleVariables;

    public FileValidationContext(final ValidationContext context, final IFile file) {
        this(context, file, null);
    }

    @VisibleForTesting
    public FileValidationContext(final ValidationContext context, final IFile file,
            final Set<String> accessibleVariables) {
        super(file.getFullPath(), new AccessibleKeywordsCollector() {

            @Override
            public Map<String, Collection<KeywordEntity>> collect() {
                return context.collectAccessibleKeywordNames(file);
            }
        });
        this.context = context;
        this.file = file;
        this.accessibleVariables = accessibleVariables;
    }

    public IFile getFile() {
        return file;
    }

    public RobotVersion getVersion() {
        return context.getVersion();
    }

    LibrarySpecification getLibrarySpecifications(final String libName) {
        return context.getLibrarySpecification(libName);
    }

    Map<ReferencedLibrary, LibrarySpecification> getReferencedLibrarySpecifications() {
        return context.getReferencedLibrarySpecifications();
    }

    Set<String> getAccessibleVariables() {
        if (accessibleVariables == null) {
            accessibleVariables = context.collectAccessibleVariables(file);
        }
        return accessibleVariables;
    }

    public static final class ValidationKeywordEntity extends KeywordEntity {

        ValidationKeywordEntity(final KeywordScope scope, final String sourceName, final String keywordName,
                final String alias, final boolean isDeprecated, final IPath exposingFilepath) {
            super(scope, sourceName, keywordName, alias, isDeprecated, exposingFilepath);
        }

        public boolean hasInconsistentName(final String useplaceName) {
            return !QualifiedKeywordName.isOccurrenceEqualToDefinition(useplaceName, getNameFromDefinition());
        }

        boolean isFromNestedLibrary(final IFile useplaceFile) {
            final IPath path = useplaceFile.getFullPath();
            final KeywordScope scope = getScope(path);
            return (scope == KeywordScope.REF_LIBRARY || scope == KeywordScope.STD_LIBRARY)
                    && !path.equals(getExposingFilepath());
        }
    }
}

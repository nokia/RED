/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.executor.SuiteExecutor;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext.ValidationKeywordEntity;

import com.google.common.collect.ArrayListMultimap;

class Contexts {

    static KeywordEntity newResourceKeyword(final String name, final IPath exposingPath, final String... args) {
        final String sourceName = exposingPath.removeFileExtension().segment(exposingPath.segmentCount() - 1);
        return new ValidationKeywordEntity(KeywordScope.RESOURCE, sourceName, name, Optional.empty(), false,
                exposingPath, 0, ArgumentsDescriptor.createDescriptor(args));
    }

    static KeywordEntity newStdLibraryKeyword(final String libName, final String name, final IPath exposingPath,
            final String... args) {
        return new ValidationKeywordEntity(KeywordScope.STD_LIBRARY, libName, name, Optional.empty(), false,
                exposingPath, 0, ArgumentsDescriptor.createDescriptor(args));
    }

    static KeywordEntity newRefLibraryKeyword(final String libName, final String name, final IPath exposingPath,
            final String... args) {
        return new ValidationKeywordEntity(KeywordScope.REF_LIBRARY, libName, name, Optional.empty(), false,
                exposingPath, 0, ArgumentsDescriptor.createDescriptor(args));
    }

    static KeywordEntity newRefLibraryKeyword(final String libName, final String alias, final String name,
            final IPath exposingPath, final String... args) {
        return new ValidationKeywordEntity(KeywordScope.REF_LIBRARY, libName, name, Optional.of(alias), false,
                exposingPath, 0, ArgumentsDescriptor.createDescriptor(args));
    }

    static KeywordEntity newBuiltInKeyword(final String name, final String... args) {
        return newStdLibraryKeyword("BuiltIn", name, new Path("/suite.robot"), args);
    }

    static FileValidationContext prepareContext() {
        return prepareContext(new ArrayList<>());
    }

    static FileValidationContext prepareContext(final List<KeywordEntity> accessibleKeywords) {
        return prepareContext(accessibleKeywords, new HashSet<>());
    }

    static FileValidationContext prepareContext(final Set<String> accessibleVariables) {
        return prepareContext(new ArrayList<>(), accessibleVariables);
    }

    static FileValidationContext prepareContext(final Collection<KeywordEntity> accessibleKeywords,
            final Set<String> accessibleVariables) {
        final Map<String, Collection<KeywordEntity>> accessibleKws = accessibleKeywords.stream()
                .collect(groupingBy(KeywordEntity::getKeywordName, toCollection(ArrayList::new)));

        final ValidationContext parentContext = new ValidationContext(null, new RobotModel(), RobotVersion.from("0.0"),
                SuiteExecutor.Python, ArrayListMultimap.create(), new HashMap<>());
        final IFile file = mock(IFile.class);
        when(file.getFullPath()).thenReturn(new Path("/suite.robot"));
        return new FileValidationContext(parentContext, file, () -> accessibleKws, accessibleVariables);
    }
}

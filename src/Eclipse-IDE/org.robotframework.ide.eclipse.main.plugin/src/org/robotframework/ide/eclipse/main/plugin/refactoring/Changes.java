/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.NullChange;

import com.google.common.base.Optional;
import com.google.common.xml.XmlEscapers;

/**
 * @author Michal Anglart
 *
 */
class Changes {

    /**
     * Returns the composite change itself if it contains children changes, or
     * null change otherwise
     * 
     * @param change
     * @return
     */
    static Change normalizeCompositeChange(final CompositeChange change) {
        for (final Change childChange : change.getChildren()) {
            if (childChange instanceof NullChange) {
                change.remove(childChange);
            }
        }
        return change.getChildren().length > 0 ? change : new NullChange();
    }

    static IPath excapeXmlCharacters(final IPath path) {
        return Path.fromPortableString(XmlEscapers.xmlContentEscaper().escape(path.toPortableString()));
    }

    /**
     * Creates a path to which {@code affectedPath} should be transformed given the fact that
     * the {@code sourcePath} changed into {@code destinationPath}. Assumption: ALL the paths
     * are relative to common location.
     * 
     * @param sourcePath
     * @param destinationPath
     * @param affectedPath
     * @return Transformed affected path or absent if the path is not actually affected.
     */
    static Optional<IPath> transformAffectedPath(final IPath sourcePath, final IPath destinationPath,
            final IPath affectedPath) {
        if (sourcePath.isPrefixOf(affectedPath)) {
            final IPath result = destinationPath.append(affectedPath.removeFirstSegments(sourcePath.segmentCount()));
            return Optional.of(result);
        }
        return Optional.absent();
    }
}

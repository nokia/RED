/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import java.util.Optional;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ExcludedPath;

class ExcludedPathsChangesDetector {

    private final IPath beforeRefactorPath;

    private final Optional<IPath> afterRefactorPath;

    private final RobotProjectConfig config;

    ExcludedPathsChangesDetector(final IPath beforeRefactorPath, final Optional<IPath> afterRefactorPath,
            final RobotProjectConfig config) {
        this.beforeRefactorPath = beforeRefactorPath;
        this.afterRefactorPath = afterRefactorPath;
        this.config = config;
    }

    void detect(final RedXmlChangesProcessor<ExcludedPath> processor) {
        for (final ExcludedPath excluded : config.getExcludedPaths()) {
            final IPath potentiallyAffectedPath = Path.fromPortableString(excluded.getPath());
            final IPath adjustedPathBeforeRefactoring = beforeRefactorPath.removeFirstSegments(1);

            if (adjustedPathBeforeRefactoring.isEmpty()) {
                return;
            }
            if (afterRefactorPath.isPresent()) {
                final IPath adjustedPathAfterRefactoring = afterRefactorPath.get().removeFirstSegments(1);

                final Optional<IPath> transformedPath = Changes.transformAffectedPath(adjustedPathBeforeRefactoring,
                        adjustedPathAfterRefactoring, potentiallyAffectedPath);
                if (transformedPath.isPresent()) {
                    processor.pathModified(excluded, ExcludedPath.create(transformedPath.get().toPortableString()));
                }
            } else if (adjustedPathBeforeRefactoring.isPrefixOf(potentiallyAffectedPath)) {
                processor.pathRemoved(config, excluded);
            }
        }
    }
}

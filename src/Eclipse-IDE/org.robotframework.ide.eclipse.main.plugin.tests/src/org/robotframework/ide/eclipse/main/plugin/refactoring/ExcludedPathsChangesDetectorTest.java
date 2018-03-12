/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Optional;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ExcludedFolderPath;

public class ExcludedPathsChangesDetectorTest {

    @Test
    public void excludedPathIsReportedForDeletion_whenPathIsEmptyAfterRefactoringAndIsChildOfPathBefore() {
        @SuppressWarnings("unchecked")
        final RedXmlChangesProcessor<ExcludedFolderPath> processor = mock(RedXmlChangesProcessor.class);

        final IPath beforeRefactorPath = Path.fromPortableString("project/resource");

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addExcludedPath("");
        config.addExcludedPath("resource");
        config.addExcludedPath("resource/a");
        config.addExcludedPath("resource/b");
        config.addExcludedPath("res/c");
        config.addExcludedPath("resource_other/d");

        final ExcludedPathsChangesDetector detector = new ExcludedPathsChangesDetector(beforeRefactorPath,
                Optional.empty(), config);
        detector.detect(processor);

        verify(processor).pathRemoved(same(config), same(config.getExcludedPath().get(1)));
        verify(processor).pathRemoved(same(config), same(config.getExcludedPath().get(2)));
        verify(processor).pathRemoved(same(config), same(config.getExcludedPath().get(3)));
        verifyNoMoreInteractions(processor);
    }

    @Test
    public void excludedPathIsReportedForModification_whenThereIsAPathAfterRefactoringAndHasCommonPartWitPathBefore() {
        @SuppressWarnings("unchecked")
        final RedXmlChangesProcessor<ExcludedFolderPath> processor = mock(RedXmlChangesProcessor.class);

        final IPath beforeRefactorPath = Path.fromPortableString("project/resource");

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addExcludedPath("");
        config.addExcludedPath("resource");
        config.addExcludedPath("resource/a");
        config.addExcludedPath("resource/b");
        config.addExcludedPath("res/c");
        config.addExcludedPath("resource_other/d");

        final ExcludedPathsChangesDetector detector = new ExcludedPathsChangesDetector(beforeRefactorPath,
                Optional.of(Path.fromPortableString("project/different_res")), config);
        detector.detect(processor);

        verify(processor).pathModified(same(config.getExcludedPath().get(1)),
                eq(ExcludedFolderPath.create("different_res")));
        verify(processor).pathModified(same(config.getExcludedPath().get(2)),
                eq(ExcludedFolderPath.create("different_res/a")));
        verify(processor).pathModified(same(config.getExcludedPath().get(3)),
                eq(ExcludedFolderPath.create("different_res/b")));
        verifyNoMoreInteractions(processor);
    }
}

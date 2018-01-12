/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

/**
 * @author Michal Anglart
 */
class RedXmlChangesCollector {

    Optional<Change> collect(final IResource refactoredResource, final Optional<IPath> afterRefactorPath) {
        final RobotProject project = RedPlugin.getModelManager().createProject(refactoredResource.getProject());
        final IFile redXmlFile = project.getConfigurationFile();
        final IPath pathBeforeRefactoring = refactoredResource.getFullPath();

        final Optional<Change> changesInProjectEditor = new RedXmlInProjectEditorChangesCollector(redXmlFile,
                pathBeforeRefactoring, afterRefactorPath).collect();
        final Optional<Change> changesInTextEditor = new RedXmlInTextEditorChangesCollector(redXmlFile,
                pathBeforeRefactoring, afterRefactorPath).collect();

        if (changesInProjectEditor.isPresent() && changesInTextEditor.isPresent()) {
            final CompositeChange compositeChange = new CompositeChange("Change in both editors",
                    new Change[] { changesInProjectEditor.get(), changesInTextEditor.get() });
            compositeChange.markAsSynthetic();
            return Optional.of(compositeChange);
        } else if (changesInProjectEditor.isPresent()) {
            return changesInProjectEditor;
        } else if (changesInTextEditor.isPresent()) {
            return changesInTextEditor;
        } else {
            return new RedXmlInFileChangesCollector(redXmlFile, pathBeforeRefactoring, afterRefactorPath).collect();
        }
    }
}

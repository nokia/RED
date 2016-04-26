/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 *
 */
class RedXmlInFileChangesCollector {

    private final IFile redXmlFile;

    private final Optional<IPath> pathAfterRefactoring;

    private final IPath pathBeforeRefactoring;

    RedXmlInFileChangesCollector(final IFile redXmlFile, final IPath pathBeforeRefactoring,
            final Optional<IPath> pathAfterRefactoring) {
        this.redXmlFile = redXmlFile;
        this.pathBeforeRefactoring = pathBeforeRefactoring;
        this.pathAfterRefactoring = pathAfterRefactoring;
    }

    Optional<Change> collect() {
        final RedXmlEditsCollector redXmlEdits = new RedXmlEditsCollector(pathBeforeRefactoring, pathAfterRefactoring);
        final List<TextEdit> validationExcluded = redXmlEdits
                .collectEditsInExcludedPaths(redXmlFile.getProject().getName(), redXmlFile);

        final MultiTextEdit multiTextEdit = new MultiTextEdit();
        for (final TextEdit edit : validationExcluded) {
            multiTextEdit.addChild(edit);
        }
        
        if (multiTextEdit.hasChildren()) {
            final TextFileChange fileChange = new TextFileChange(
                    "'" + redXmlFile.getFullPath() + "': paths excluded from validation", redXmlFile);
            fileChange.setEdit(multiTextEdit);
            fileChange.addTextEditGroup(new TextEditGroup("Change paths excluded from validation",
                    validationExcluded.toArray(new TextEdit[0])));
            return Optional.<Change> of(fileChange);
        } else {
            return Optional.absent();
        }
    }
}

/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

public class RobotFormEditorActivePageSaver {

    static void saveActivePageId(final IEditorInput editorInput, final String activePageId) {
        if (editorInput instanceof IFileEditorInput) {
            final IFileEditorInput fileInput = (IFileEditorInput) editorInput;
            final IFile file = fileInput.getFile();

            final String sectionName = RobotFormEditor.ID + ".activePage." + file.getFullPath().toPortableString();
            final IDialogSettings dialogSettings = RedPlugin.getDefault().getDialogSettings();
            IDialogSettings section = dialogSettings.getSection(sectionName);
            if (section == null) {
                section = dialogSettings.addNewSection(sectionName);
            }
            section.put("activePage", activePageId);
        }
    }

    static String getLastActivePageId(final IEditorInput editorInput) {
        if (editorInput instanceof IFileEditorInput) {
            final IFileEditorInput fileInput = (IFileEditorInput) editorInput;
            final IFile file = fileInput.getFile();

            return getLastActivePageId(file);
        }
        return null;
    }

    public static String getLastActivePageId(final IFile file) {
        final String sectionName = RobotFormEditor.ID + ".activePage." + file.getFullPath().toPortableString();
        final IDialogSettings dialogSettings = RedPlugin.getDefault().getDialogSettings();
        final IDialogSettings section = dialogSettings.getSection(sectionName);
        if (section == null) {
            return null;
        }
        return section.get("activePage");
    }
}

/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.texteditor.handlers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;

/**
 * @author mmarzec
 *
 */
public class SaveAsHandler {

	@Execute
    public void execute(@Active IEditorPart editorPart) {
        
        IEditorInput input = editorPart.getEditorInput();
        
        FileEditorInput fileEditorInput = null;
        if(input instanceof FileEditorInput) {
        	fileEditorInput = (FileEditorInput) input;
        }
        
        IFile editedFile = fileEditorInput.getFile();
        editedFile.getLocation().toPortableString();
        
        FileDialog dialog = new FileDialog(editorPart.getSite().getShell(), SWT.SAVE);
        dialog.setFilterNames(new String[] { "Text Files", "All Files (*.*)" });
        dialog.setFilterExtensions(new String[] { "*.txt", "*.*" });
                                        
        dialog.setFilterPath(editedFile.getLocation().removeLastSegments(1).toPortableString());
        dialog.setFileName(editedFile.getName());
        
        String filePath = dialog.open();
        
        if(filePath != null) {
	        File newFile = new File(filePath);
			try (InputStream is = editedFile.getContents();
					OutputStream os = new FileOutputStream(newFile)) {
	        	byte[] buffer = new byte[is.available()];
	        	is.read(buffer);
	        	os.write(buffer);
	        	((FileEditorInput) input).getFile().getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (IOException | CoreException e) {
				e.printStackTrace();
			}
        }
    }
}

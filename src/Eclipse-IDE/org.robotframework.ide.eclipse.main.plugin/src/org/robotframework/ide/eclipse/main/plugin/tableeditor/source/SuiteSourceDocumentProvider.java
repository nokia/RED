/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class SuiteSourceDocumentProvider extends FileDocumentProvider {


    @Override
    protected IDocument createDocument(final Object element) throws CoreException {
        final IDocument document = super.createDocument(element);

        if (document != null) {
            final IDocumentPartitioner partitioner = new FastPartitioner(new SuiteSourcePartitionScanner(),
                    SuiteSourcePartitionScanner.LEGAL_CONTENT_TYPES);
            partitioner.connect(document);
            document.setDocumentPartitioner(partitioner);
        }

        return document;
    }

}

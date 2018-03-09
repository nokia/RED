/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.util.function.Function;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

public class Fixers {

    public static Function<RedSuiteMarkerResolution, IDocument> byApplyingToDocument(final IMarker marker,
            final IDocument document, final RobotSuiteFile model) {
        return new Function<RedSuiteMarkerResolution, IDocument>() {

            @Override
            public IDocument apply(final RedSuiteMarkerResolution fixer) {
                final Document docCopy = new Document(document);
                fixer.asContentProposal(marker, document, model).get().apply(docCopy);
                return docCopy;
            }
        };
    }
}

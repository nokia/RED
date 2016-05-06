/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;


import org.assertj.core.api.Condition;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;

import com.google.common.base.Function;

/**
 * @author Michal Anglart
 *
 */
class Proposals {

    static Condition<? super ICompletionProposal> proposalsWithImage(final Image image) {
        return new Condition<ICompletionProposal>() {
            @Override
            public boolean matches(final ICompletionProposal proposal) {
                return proposal.getImage().equals(image);
            }
        };
    }

    static Function<ICompletionProposal, IDocument> byApplyingToDocument(final IDocument document) {
        return new Function<ICompletionProposal, IDocument>() {
            @Override
            public IDocument apply(final ICompletionProposal proposal) {
                final Document docCopy = new Document(document);
                proposal.apply(docCopy);
                return docCopy;
            }
        };
    }

}

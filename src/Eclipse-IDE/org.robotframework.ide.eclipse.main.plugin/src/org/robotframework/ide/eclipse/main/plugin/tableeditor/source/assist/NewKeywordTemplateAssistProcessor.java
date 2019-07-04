/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Optional;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.RedTemplateContextType;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;

public class NewKeywordTemplateAssistProcessor extends RedTemplateAssistProcessor {

    public NewKeywordTemplateAssistProcessor(final SuiteSourceAssistantContext assist) {
        super(assist);
    }

    @Override
    public String getProposalsTitle() {
        return "New keyword templates";
    }

    @Override
    public List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.KEYWORDS_SECTION);
    }

    @Override
    protected String getContextTypeId() {
        return RedTemplateContextType.NEW_KEYWORD_CONTEXT_TYPE;
    }

    @Override
    protected boolean shouldShowProposals(final IDocument document, final int offset, final String lineContent)
            throws BadLocationException {
        final IRegion lineInfo = document.getLineInformationOfOffset(offset);
        if (offset != lineInfo.getOffset()) {
            final Optional<IRegion> cellRegion = DocumentUtilities.findLiveCellRegion(document, assist.isTsvFile(),
                    offset);
            return cellRegion.isPresent() && lineInfo.getOffset() == cellRegion.get().getOffset();
        }
        return isInApplicableContentType(document, offset);
    }
}

/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.assist.ProposalMatch;
import org.robotframework.ide.eclipse.main.plugin.assist.ProposalMatcher;
import org.robotframework.ide.eclipse.main.plugin.assist.ProposalMatchers;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.red.graphics.ImagesManager;

public abstract class RedTemplateAssistProcessor extends TemplateCompletionProcessor
        implements IRedContentAssistProcessor {

    protected final AssistantContext assist;

    public RedTemplateAssistProcessor(final AssistantContext assist) {
        this.assist = assist;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected TemplateContextType getContextType(final ITextViewer viewer, final IRegion region) {
        return RedPlugin.getDefault()
                .getPreferences()
                .getTemplateContextTypeRegistry()
                .getContextType(getContextTypeId());
    }

    protected abstract String getContextTypeId();

    @Override
    protected Template[] getTemplates(final String contextTypeId) {
        final TemplateStore templateStore = RedPlugin.getDefault().getPreferences().getTemplateStore();
        return templateStore.getTemplates(contextTypeId);
    }

    @Override
    protected Image getImage(final Template template) {
        return ImagesManager.getImage(RedImages.getTemplateImage());
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, int offset) {
        try {
            final IDocument document = viewer.getDocument();
            final String lineContent = DocumentUtilities.lineContentBeforeCurrentPosition(document, offset);

            if (!shouldShowProposals(document, offset, lineContent)) {
                return new ICompletionProposal[0];
            }

            final String userContent = extractUserContent(document, offset);

            final ITextSelection selection = (ITextSelection) viewer.getSelectionProvider().getSelection();
            // adjust offset to end of normalized selection
            if (selection.getOffset() == offset) {
                offset = selection.getOffset() + selection.getLength();
            }

            final String prefix = extractPrefix(viewer, offset);
            final Region region = new Region(offset - prefix.length(), prefix.length());
            final TemplateContext context = createContext(viewer, region);
            if (context == null) {
                return new ICompletionProposal[0];
            }

            context.setVariable("selection", selection.getText()); // name of the //$NON-NLS-1$
                                                                   // selection variables {line,
                                                                   // word}_selection
            final List<ICompletionProposal> proposals = new RedTemplateProposals(context, region)
                    .getSectionsProposals(userContent);

            return proposals.toArray(new ICompletionProposal[proposals.size()]);

        } catch (final BadLocationException e) {
            return new ICompletionProposal[0];
        }
    }

    private String extractUserContent(final IDocument document, final int offset) throws BadLocationException {
        final boolean isTsv = assist.isTsvFile();
        final Optional<IRegion> cellRegion = DocumentUtilities.findLiveCellRegion(document, isTsv, offset);
        return DocumentUtilities.getPrefix(document, cellRegion, offset);
    }

    protected abstract boolean shouldShowProposals(final IDocument document, final int offset, final String lineContent)
            throws BadLocationException;

    @Override
    public boolean isInApplicableContentType(final IDocument document, final int offset)
            throws BadLocationException {
        return getApplicableContentTypes().contains(getVirtualContentType(document, offset));
    }

    protected final String getVirtualContentType(final IDocument document, final int offset)
            throws BadLocationException {
        final String contentType = document.getContentType(offset);
        if (contentType != IDocument.DEFAULT_CONTENT_TYPE) {
            return contentType;
        } else if (offset > 0 && offset == document.getLength()) {
            return document.getContentType(offset - 1);
        }
        return contentType;
    }

    class RedTemplateProposals {

        private final TemplateContext context;

        private final Region region;

        private final ProposalMatcher matcher;

        RedTemplateProposals(final TemplateContext context, final Region region) {
            this(context, region, ProposalMatchers.substringMatcher());
        }

        RedTemplateProposals(final TemplateContext context, final Region region, final ProposalMatcher matcher) {
            this.context = context;
            this.region = region;
            this.matcher = matcher;
        }

        List<ICompletionProposal> getSectionsProposals(final String userContent) {
            final List<ICompletionProposal> proposals = new ArrayList<>();
            final Template[] templates = getTemplates(context.getContextType().getId());
            for (final Template template : templates) {
                try {
                    context.getContextType().validate(template.getPattern());
                } catch (final TemplateException e) {
                    continue;
                }
                final Optional<ProposalMatch> match = matcher.matches(userContent, template.getName());
                if (template.getContextTypeId().equals(context.getContextType().getId()) && match.isPresent()) {
                    proposals.add(new RedTemplateProposal(template, context, region, getImage(template), match.get()));
                }
            }
            return proposals;
        }
    }
}

/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.jface.text.link.RedEditorLinkedModeUI;
import org.robotframework.red.swt.SwtThread;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;

/**
 * @author Michal Anglart
 *
 */
public class VariablesDefinitionsAssistProcessor extends RedContentAssistProcessor {

    private static final Collection<VarDef> VARIABLE_DEFS = EnumSet.allOf(VarDef.class);

    private final SuiteSourceAssistantContext assist;

    public VariablesDefinitionsAssistProcessor(final SuiteSourceAssistantContext assist) {
        this.assist = assist;
    }

    @Override
    protected List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.VARIABLES_SECTION);
    }

    @Override
    protected String getProposalsTitle() {
        return "Variable definitions";
    }

    @Override
    public List<? extends ICompletionProposal> computeProposals(final ITextViewer viewer, final int offset) {
        final IDocument document = viewer.getDocument();
        try {
            final boolean isTsv = assist.isTsvFile();

            final IRegion lineInformation = document.getLineInformationOfOffset(offset);
            final boolean shouldShowProposal = shouldShowProposals(offset, document, isTsv, lineInformation);

            if (shouldShowProposal) {
                final String prefix = DocumentUtilities.getPrefix(document, Optional.of(lineInformation), offset);
                final Optional<IRegion> cellRegion = DocumentUtilities.findCellRegion(document, isTsv, offset);
                final String content = cellRegion.isPresent()
                        ? document.get(cellRegion.get().getOffset(), cellRegion.get().getLength()) : "";
                final String separator = assist.getSeparatorToFollow();

                final List<ICompletionProposal> proposals = newArrayList();
                for (final VarDef varDef : VARIABLE_DEFS) {
                    if (varDef.content.toLowerCase().startsWith(prefix.toLowerCase())) {
                        String textToInsert;
                        if (varDef.initialValues.isEmpty()) {
                            textToInsert = varDef.content + separator;
                        } else {
                            textToInsert = varDef.content + separator + Joiner.on(separator).join(varDef.initialValues)
                                    + separator;
                        }

                        final Collection<IRegion> regions = getLinkedModeRegions(lineInformation, varDef);
                        final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                                .will(assist.getAcceptanceMode())
                                .theText(textToInsert)
                                .atOffset(lineInformation.getOffset())
                                .givenThatCurrentPrefixIs(prefix)
                                .andWholeContentIs(content)
                                .secondaryPopupShouldBeDisplayed(varDef.info)
                                .performAfterAccepting(createOperationsToPerformAfterAccepting(viewer, regions))
                                .thenCursorWillStopAt(2, varDef.content.length() - 3)
                                .displayedLabelShouldBe(varDef.label)
                                .proposalsShouldHaveIcon(varDef.getImage())
                                .create();
                        proposals.add(proposal);
                    }
                }
                return proposals;

            }
            return null;
        } catch (final BadLocationException e) {
            return null;
        }
    }

    private boolean shouldShowProposals(final int offset, final IDocument document, final boolean isTsv,
            final IRegion lineInformation)
            throws BadLocationException {
        if (isInApplicableContentType(document, offset)) {
            // we only want to show those proposals in first cell of the line
            if (offset != lineInformation.getOffset()) {
                final Optional<IRegion> cellRegion = DocumentUtilities.findLiveCellRegion(document, isTsv, offset);
                return cellRegion.isPresent() && lineInformation.getOffset() == cellRegion.get().getOffset();
            } else {
                return true;
            }
        }
        return false;
    }

    private Collection<IRegion> getLinkedModeRegions(final IRegion lineInformation, final VarDef varDef) {
        final int startingOffset = lineInformation.getOffset();
        final int separatorLength = assist.getSeparatorToFollow().length();
        int offset = startingOffset + varDef.content.length() + separatorLength;

        final Region nameRegion = new Region(startingOffset + 2, varDef.content.length() - 3);

        final List<IRegion> linkedModeRegions = new ArrayList<>();
        linkedModeRegions.add(nameRegion);
        switch (varDef) {
            case SCALAR:
                linkedModeRegions.add(new Region(offset, 0));
                break;
            case LIST:
                for (final String initialValue : varDef.initialValues) {
                    linkedModeRegions.add(new Region(offset, initialValue.length()));
                    offset += initialValue.length() + separatorLength;
                }
                linkedModeRegions.add(new Region(offset, 0));
                break;
            case DICT:
                for (final String initialValue : varDef.initialValues) {
                    final List<String> splittedKeyVal = Splitter.on('=').limit(2).splitToList(initialValue);
                    linkedModeRegions.add(new Region(offset, splittedKeyVal.get(0).length()));
                    linkedModeRegions.add(
                            new Region(offset + splittedKeyVal.get(0).length() + 1, splittedKeyVal.get(1).length()));

                    offset += initialValue.length() + separatorLength;
                }
                linkedModeRegions.add(new Region(offset, 0));
                break;
            default:
                throw new IllegalStateException("Unknown variable def value: " + varDef.toString());
        }
        return linkedModeRegions;
    }

    private Collection<Runnable> createOperationsToPerformAfterAccepting(final ITextViewer viewer,
            final Collection<IRegion> regionsToLinkedEdit) {
        if (regionsToLinkedEdit.isEmpty()) {
            return new ArrayList<>();
        }
        final Runnable operation = new Runnable() {
            @Override
            public void run() {
                SwtThread.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        RedEditorLinkedModeUI.enableLinkedMode(viewer, regionsToLinkedEdit);
                    }
                });
            }
        };
        return newArrayList(operation);
    }

    private enum VarDef {
        SCALAR("${newScalar}", new ArrayList<String>(), "Fresh scalar",
                RedImages.getRobotScalarVariableImage(), "Creates fresh scalar variable"),
        LIST("@{newList}", newArrayList("item"), "Fresh list",
                RedImages.getRobotListVariableImage(), "Creates fresh list variable"),
        DICT("&{newDict}", newArrayList("key=value"), "Fresh dictionary",
                RedImages.getRobotDictionaryVariableImage(), "Creates fresh dictionary variable");

        private String content;

        private Collection<String> initialValues;

        private String label;

        private ImageDescriptor image;

        private String info;

        private VarDef(final String content, final Collection<String> initialValues, final String label,
                final ImageDescriptor image, final String info) {
            this.content = content;
            this.initialValues = initialValues;
            this.label = label;
            this.image = image;
            this.info = info;
        }

        Image getImage() {
            return ImagesManager.getImage(image);
        }
    }
}

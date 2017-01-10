/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Image;

/**
 * @author Michal Anglart
 *
 */
@SuppressWarnings("PMD.TooManyMethods")
public class RedCompletionBuilder {

    public static interface ProposalContentStep {
        LocationStep willPut(String string);
    }

    public static interface LocationStep {

        OptionalSettingsStep byInsertingAt(int offset);

        OptionalSettingsStep byReplacingRegion(IRegion region);

        OptionalSettingsStep byReplacingRegion(int offset, int length);
    }

    public static interface OptionalSettingsStep {

        OptionalSettingsStep secondaryPopupShouldBeDisplayedUsingHtml(String additionalInfo);

        OptionalSettingsStep activateAssistantAfterAccepting(boolean activate);

        OptionalSettingsStep performAfterAccepting(Collection<Runnable> operations);

        DecorationsStep thenCursorWillStopAt(int position);

        DecorationsStep thenCursorWillStopAtTheEndOfInsertion();

        DecorationsStep thenCursorWillStopBeforeEnd(int shift);
    }

    public static interface DecorationsStep {

        DecorationsStep displayedLabelShouldBe(String label);

        DecorationsStep proposalsShouldHaveIcon(Image image);

        RedCompletionProposal create();
    }

    private static class BuildingSteps
            implements ProposalContentStep, LocationStep, OptionalSettingsStep, DecorationsStep {

        private String contentToInsert;

        private int offset;

        private int length;

        private String additionalInfo;

        private int cursorPosition;

        private int cursorBackShift;

        private int selectionLength;

        private String labelToDisplay;

        private Image image;

        private boolean additionalInfoAsHtml;

        private boolean activateAssitant;

        private final Collection<Runnable> operationsAfterAccept = new ArrayList<>();

        @Override
        public LocationStep willPut(final String contentToInsert) {
            this.contentToInsert = contentToInsert;
            this.labelToDisplay = contentToInsert;
            return this;
        }

        @Override
        public OptionalSettingsStep byInsertingAt(final int offset) {
            return byReplacingRegion(offset, 0);
        }

        @Override
        public OptionalSettingsStep byReplacingRegion(final IRegion region) {
            return byReplacingRegion(region.getOffset(), region.getLength());
        }

        @Override
        public OptionalSettingsStep byReplacingRegion(final int offset, final int length) {
            this.offset = offset;
            this.length = length;
            return this;
        }

        @Override
        public OptionalSettingsStep secondaryPopupShouldBeDisplayedUsingHtml(final String additionalInfo) {
            this.additionalInfo = additionalInfo;
            this.additionalInfoAsHtml = true;
            return this;
        }

        @Override
        public OptionalSettingsStep activateAssistantAfterAccepting(final boolean activate) {
            this.activateAssitant = activate;
            return this;
        }

        @Override
        public OptionalSettingsStep performAfterAccepting(final Collection<Runnable> operations) {
            this.operationsAfterAccept.addAll(operations);
            return this;
        }

        @Override
        public DecorationsStep thenCursorWillStopAt(final int position) {
            this.cursorPosition = position;
            this.selectionLength = 0;
            return this;
        }

        @Override
        public DecorationsStep thenCursorWillStopAtTheEndOfInsertion() {
            this.cursorPosition = -1;
            this.selectionLength = 0;
            return this;
        }

        @Override
        public DecorationsStep thenCursorWillStopBeforeEnd(final int shift) {
            this.cursorPosition = -1;
            this.selectionLength = 0;
            this.cursorBackShift = shift;
            return this;
        }

        @Override
        public DecorationsStep displayedLabelShouldBe(final String label) {
            this.labelToDisplay = label;
            return this;
        }

        @Override
        public DecorationsStep proposalsShouldHaveIcon(final Image image) {
            this.image = image;
            return this;
        }

        @Override
        public RedCompletionProposal create() {
            final int cursorPos = cursorPosition == -1 ? (contentToInsert.length() - cursorBackShift) : cursorPosition;
            return new RedCompletionProposal(contentToInsert, offset, length, 0, cursorPos, selectionLength, image,
                    false, labelToDisplay, activateAssitant, operationsAfterAccept, additionalInfo,
                    additionalInfoAsHtml);
        }
    }

    public static ProposalContentStep newProposal() {
        return new BuildingSteps();
    }
}

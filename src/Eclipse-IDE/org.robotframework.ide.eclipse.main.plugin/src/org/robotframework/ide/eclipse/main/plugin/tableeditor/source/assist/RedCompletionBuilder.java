/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.text.Position;
import org.eclipse.swt.graphics.Image;

/**
 * @author Michal Anglart
 *
 */
@SuppressWarnings("PMD.TooManyMethods")
public class RedCompletionBuilder {

    public static interface ProposalAcceptanceModeStep {
        ProposalContentStep will(AcceptanceMode insert);
    }

    public static interface ProposalContentStep {
        LocationStep theText(String string);
    }

    public static interface LocationStep {
        CurrentPrefixStep atOffset(int offset);
    }

    public static interface CurrentPrefixStep {
        OptionalSettingsStep givenThatCurrentPrefixIs(String string);
    }

    public static interface OptionalSettingsStep {

        OptionalSettingsStep andWholeContentIs(String string);

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

        DecorationsStep currentPrefixShouldBeDecorated();

        RedCompletionProposal create();
    }

    private static class BuildingSteps implements ProposalAcceptanceModeStep, ProposalContentStep, LocationStep,
            CurrentPrefixStep, OptionalSettingsStep, DecorationsStep {

        private AcceptanceMode mode;

        private String contentToInsert;

        private int offset;

        private String currentPrefix;

        private String wholeContent;

        private String additionalInfo;

        private int cursorPosition;

        private int cursorBackShift;

        private int selectionLength;

        private String labelToDisplay;

        private Image image;

        private boolean additionalInfoAsHtml;

        private boolean decoratePrefix;

        private boolean activateAssitant;

        private final Collection<Runnable> operationsAfterAccept = new ArrayList<>();

        @Override
        public ProposalContentStep will(final AcceptanceMode mode) {
            this.mode = mode;
            return this;
        }

        @Override
        public LocationStep theText(final String contentToInsert) {
            this.contentToInsert = contentToInsert;
            this.labelToDisplay = contentToInsert;
            return this;
        }

        @Override
        public CurrentPrefixStep atOffset(final int offset) {
            this.offset = offset;
            return this;
        }

        @Override
        public OptionalSettingsStep givenThatCurrentPrefixIs(final String prefix) {
            this.currentPrefix = prefix;
            return this;
        }

        @Override
        public OptionalSettingsStep andWholeContentIs(final String wholeContent) {
            this.wholeContent = wholeContent;
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
        public DecorationsStep currentPrefixShouldBeDecorated() {
            this.decoratePrefix = true;
            return this;
        }

        @Override
        public RedCompletionProposal create() {
            final int cursorPos = cursorPosition == -1 ? (contentToInsert.length() - cursorBackShift) : cursorPosition;
            if (mode == AcceptanceMode.INSERT) {
                return new RedCompletionProposal(contentToInsert, offset, currentPrefix.length(),
                        currentPrefix.length(), cursorPos, selectionLength, image, decoratePrefix, labelToDisplay,
                        activateAssitant, operationsAfterAccept, additionalInfo, additionalInfoAsHtml);
            } else if (mode == AcceptanceMode.SUBSTITUTE) {
                if (wholeContent == null) {
                    throw new IllegalStateException("Unable to create proposal in substitution mode if there is no "
                            + "content to substitute specified");
                }
                return new RedCompletionProposal(contentToInsert, offset, wholeContent.length(), currentPrefix.length(),
                        cursorPos, selectionLength, image, decoratePrefix, labelToDisplay, activateAssitant,
                        operationsAfterAccept, additionalInfo, additionalInfoAsHtml);
            } else {
                throw new IllegalStateException("Unknown acceptance mode: " + mode.toString());
            }
        }
    }

    public static ProposalAcceptanceModeStep newProposal() {
        return new BuildingSteps();
    }

    public enum AcceptanceMode {
        INSERT {
            @Override
            public Position positionToReplace(final int offset, final int prefixLength, final int wholeLength) {
                return new Position(offset - prefixLength, prefixLength);
            }
        },
        SUBSTITUTE {
            @Override
            public Position positionToReplace(final int offset, final int prefixLength, final int wholeLength) {
                return new Position(offset - prefixLength, wholeLength);
            }
        };

        public abstract Position positionToReplace(final int offset, final int prefixLength, final int wholeLength);
    }
}

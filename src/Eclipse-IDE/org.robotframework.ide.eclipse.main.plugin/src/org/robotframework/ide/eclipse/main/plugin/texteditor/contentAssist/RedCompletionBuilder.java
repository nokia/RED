/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist;

import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;

/**
 * @author Michal Anglart
 *
 */
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

        OptionalSettingsStep secondaryPopupShouldBeDisplayed(String additionalInfo);

        OptionalSettingsStep contextInformationShouldBeShownAfterAccepting(IContextInformation contextInformation);

        OptionalSettingsStep activateAssistantAfterAccepting(boolean activate);

        DecorationsStep thenCursorWillStopAt(int position);

        DecorationsStep thenCursorWillStopAtTheEndOfInsertion();
    }

    public static interface DecorationsStep {

        DecorationsStep displayedLabelShouldBe(String label);

        DecorationsStep proposalsShouldHaveIcon(Image image);

        DecorationsStep currentPrefixShouldBeDecorated();

        DecorationsStep labelShouldBeAugmentedWith(String additionalInfoInLabel);

        RedCompletionProposal create();

        RedCompletionProposal createWithPriority(int priority);
    }

    private static class BuildingSteps implements ProposalAcceptanceModeStep, ProposalContentStep, LocationStep,
            CurrentPrefixStep, OptionalSettingsStep, DecorationsStep {

        private AcceptanceMode mode;

        private String contentToInsert;

        private int offset;

        private String currentPrefix;

        private String wholeContent;

        private String additionalInfo;

        private IContextInformation contextInformation;

        private int cursorPosition;

        private String labelToDisplay;

        private Image image;

        private String additionalInfoInLabel;

        private boolean decoratePrefix;

        private boolean activateAssitant;

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
        public OptionalSettingsStep secondaryPopupShouldBeDisplayed(final String additionalInfo) {
            this.additionalInfo = additionalInfo;
            return this;
        }

        @Override
        public OptionalSettingsStep contextInformationShouldBeShownAfterAccepting(
                final IContextInformation contextInformation) {
            this.contextInformation = contextInformation;
            return this;
        }

        @Override
        public OptionalSettingsStep activateAssistantAfterAccepting(final boolean activate) {
            this.activateAssitant = activate;
            return this;
        }

        @Override
        public DecorationsStep thenCursorWillStopAt(final int position) {
            this.cursorPosition = position;
            return this;
        }

        @Override
        public DecorationsStep thenCursorWillStopAtTheEndOfInsertion() {
            this.cursorPosition = -1;
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
        public DecorationsStep labelShouldBeAugmentedWith(final String additionalInfoInLabel) {
            this.additionalInfoInLabel = additionalInfoInLabel;
            return this;
        }

        @Override
        public RedCompletionProposal create() {
            return createWithPriority(0);
        }

        @Override
        public RedCompletionProposal createWithPriority(final int priority) {
            final int cursorPos = cursorPosition == -1 ? contentToInsert.length() : cursorPosition;
            if (mode == AcceptanceMode.INSERT) {
                return new RedCompletionProposal(priority, contentToInsert, offset, currentPrefix.length(),
                        currentPrefix.length(), cursorPos, image, decoratePrefix, labelToDisplay, activateAssitant,
                        contextInformation, additionalInfo, additionalInfoInLabel);
            } else if (mode == AcceptanceMode.SUBSTITUTE) {
                if (wholeContent == null) {
                    throw new IllegalStateException("Unable to create proposal in substitution mode if there is no "
                            + "content to substitute specified");
                }
                return new RedCompletionProposal(priority, contentToInsert, offset, wholeContent.length(),
                        currentPrefix.length(), cursorPos, image, decoratePrefix, labelToDisplay, activateAssitant,
                        contextInformation, additionalInfo, additionalInfoInLabel);
            } else {
                throw new IllegalStateException("Unknown acceptance mode: " + mode.toString());
            }
        }

    }

    public static ProposalAcceptanceModeStep newProposal() {
        return new BuildingSteps();
    }

    public enum AcceptanceMode {
        INSERT,
        SUBSTITUTE
    }
}

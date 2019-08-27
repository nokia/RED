/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.text.link;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.LinkedModeStrategy;
import org.robotframework.red.swt.SwtThread;

import com.google.common.collect.Iterables;

/**
 * @author Michal Anglart
 */
public class RedEditorLinkedModeUI extends LinkedModeUI {

    public static void enableLinkedModeWithEmptyCellReplacing(final ITextViewer viewer,
            final LinkedModeStrategy strategy, final String separator, final Collection<IRegion> regionsToLinkedEdit,
            final int numberOfEmptyRegionsToAdd, final boolean hasUpperBound) {
        enableLinkedModeWithEmptyCellReplacing(viewer, strategy, separator, regionsToLinkedEdit, 0,
                numberOfEmptyRegionsToAdd, hasUpperBound);
    }

    private static void enableLinkedModeWithEmptyCellReplacing(final ITextViewer viewer,
            final LinkedModeStrategy strategy, final String separator, final Collection<IRegion> regionsToLinkedEdit,
            final int startingPos, final int numberOfEmptyRegionsToAdd, final boolean hasUpperBound) {


        final LinkedModeUI linkedModeUi = createLinkedMode(viewer, regionsToLinkedEdit, startingPos);
        if (strategy == LinkedModeStrategy.EXIT_ON_LAST) {
            linkedModeUi.setCyclingMode(CYCLE_NEVER);
            linkedModeUi.setExitPolicy(new CompundExitPolicy(new EmptyRegionsReplacingPolicy(viewer, strategy,
                    separator, numberOfEmptyRegionsToAdd, hasUpperBound), new JumpToExitPositionOnEscPolicy()));
            try {
                final IRegion lastRegion = Iterables.getLast(regionsToLinkedEdit);
                linkedModeUi.setExitPosition(viewer, lastRegion.getOffset() + lastRegion.getLength(), 0,
                        Integer.MAX_VALUE);
            } catch (final BadLocationException e) {
                // shouldn't happen
            }
        } else if (strategy == LinkedModeStrategy.CYCLE) {
            linkedModeUi.setCyclingMode(CYCLE_ALWAYS);
            linkedModeUi.setExitPolicy(new EmptyRegionsReplacingPolicy(viewer, strategy, separator,
                    numberOfEmptyRegionsToAdd, hasUpperBound));
        } else {
            throw new IllegalStateException("Unrecognized mode: " + strategy.toString());
        }
        linkedModeUi.enter();
    }

    public static void enableLinkedMode(final ITextViewer viewer, final Collection<IRegion> regionsToLinkedEdit,
            final LinkedModeStrategy strategy) {
        
        final LinkedModeUI linkedModeUi = createLinkedMode(viewer, regionsToLinkedEdit, 0);
        if (strategy == LinkedModeStrategy.EXIT_ON_LAST) {
            linkedModeUi.setCyclingMode(CYCLE_NEVER);
            linkedModeUi.setExitPolicy((model, event, offset, length) -> {
                if (event.character == SWT.ESC) {
                    return new ExitFlags(ILinkedModeListener.UPDATE_CARET, false);
                }
                return null;
            });
            try {
                final IRegion lastRegion = Iterables.getLast(regionsToLinkedEdit);
                linkedModeUi.setExitPosition(viewer, lastRegion.getOffset() + lastRegion.getLength(), 0,
                        Integer.MAX_VALUE);
            } catch (final BadLocationException e) {
                // shouldn't happen
            }
        } else if (strategy == LinkedModeStrategy.CYCLE) {
            linkedModeUi.setCyclingMode(CYCLE_ALWAYS);
            linkedModeUi.setExitPolicy((model, event, offset, length) -> null);
        } else {
            throw new IllegalStateException("Unrecognized mode: " + strategy.toString());
        }

        linkedModeUi.enter();
    }

    private static boolean insert(final IDocument document, final String text, final int offset) {
        try {
            document.replace(offset, 0, text);
            return true;
        } catch (final BadLocationException e) {
            // nothing will happen in such case
        }
        return false;
    }

    private static LinkedModeUI createLinkedMode(final ITextViewer viewer,
            final Collection<IRegion> regionsToLinkedEdit, final int startingPos) {
        try {
            final LinkedModeModel model = new LinkedModeModel();
            int i = 0;
            for (final IRegion region : regionsToLinkedEdit) {
                final LinkedPositionGroup group = new LinkedPositionGroup();
                final int rank = (i + regionsToLinkedEdit.size() - startingPos) % regionsToLinkedEdit.size();
                group.addPosition(
                        new LinkedPosition(viewer.getDocument(), region.getOffset(), region.getLength(), rank));
                model.addGroup(group);
                i++;
            }
            model.forceInstall();
            return new RedEditorLinkedModeUI(model, viewer);

        } catch (final BadLocationException e) {
            throw new IllegalStateException("Invalid location for linked mode regions", e);
        }
    }

    private RedEditorLinkedModeUI(final LinkedModeModel model, final ITextViewer... viewers) {
        super(model, viewers);
    }

    private static class CompundExitPolicy implements IExitPolicy {

        private final List<IExitPolicy> policies;

        public CompundExitPolicy(final IExitPolicy... policies) {
            this.policies = Arrays.asList(policies);
        }

        @Override
        public ExitFlags doExit(final LinkedModeModel model, final VerifyEvent event, final int offset,
                final int length) {
            for (final IExitPolicy policy : policies) {
                final ExitFlags flags = policy.doExit(model, event, offset, length);
                if (flags != null) {
                    return flags;
                }
            }
            return null;
        }
    }

    private static class EmptyRegionsReplacingPolicy implements IExitPolicy {

        private final ITextViewer viewer;

        private final LinkedModeStrategy strategy;

        private final int numberOfEmptyRegionsToAdd;

        private final String emptyCellReplacement;

        private final String separator;

        private final boolean hasUpperBound;

        private EmptyRegionsReplacingPolicy(final ITextViewer viewer, final LinkedModeStrategy strategy,
                final String separator, final int numberOfEmptyRegionsToAdd, final boolean hasUpperBound) {
            this.viewer = viewer;
            this.strategy = strategy;
            this.separator = separator;
            this.emptyCellReplacement = "\\";
            this.numberOfEmptyRegionsToAdd = numberOfEmptyRegionsToAdd;
            this.hasUpperBound = hasUpperBound;
        }

        @Override
        public ExitFlags doExit(final LinkedModeModel model, final VerifyEvent event, final int offset,
                final int length) {
            final boolean shouldOpenNewLinkedMode = replaceEmptyRegion(model, event.character, offset);
            if (shouldOpenNewLinkedMode) {
                final List<IRegion> newRegions = model.getTabStopSequence()
                        .stream()
                        .map(p -> new Region(p.getOffset(), p.getLength()))
                        .collect(toList());
                // the last region is now long as it contains separator, we need to substract its
                // length
                final IRegion lastRegion = newRegions.get(newRegions.size() - 1);
                final Region newLastRegions = new Region(lastRegion.getOffset(),
                        lastRegion.getLength() - separator.length());

                newRegions.set(newRegions.size() - 1, newLastRegions);
                newRegions.add(
                        new Region(newLastRegions.getOffset() + newLastRegions.getLength() + separator.length(), 0));

                final int startingIndex = newRegions.size() - 1;
                SwtThread.asyncExec(() -> {
                    enableLinkedModeWithEmptyCellReplacing(viewer, strategy, separator, newRegions, startingIndex,
                            numberOfEmptyRegionsToAdd - 1, hasUpperBound);
                });
                return new ExitFlags(ILinkedModeListener.NONE, false);

            } else if (strategy == LinkedModeStrategy.EXIT_ON_LAST) {
                final char character = event.character;
                if (character == SWT.CR || character == SWT.LF || character == SWT.TAB) {
                    final List<LinkedPosition> positions = model.getTabStopSequence();

                    final boolean isLastPosition = positions.stream()
                            .filter(pos -> pos.includes(offset))
                            .findFirst()
                            .orElse(null) == positions.get(positions.size() - 1);
                    if (isLastPosition) {
                        return new ExitFlags(ILinkedModeListener.UPDATE_CARET, false);
                    }
                }
                return null;

            } else {
                return null;
            }
        }

        private boolean replaceEmptyRegion(final LinkedModeModel model, final char character,
                final int offset) {
            if (character == SWT.CR || character == SWT.LF || character == SWT.TAB) {
                final List<LinkedPosition> positions = model.getTabStopSequence();

                final Optional<LinkedPosition> currentPostion = positions.stream()
                        .filter(pos -> pos.includes(offset))
                        .findFirst();
                final boolean isAtZeroLengthPosition = currentPostion.map(LinkedPosition::getLength).orElse(-1) == 0;
                final boolean isLastPosition = currentPostion.orElse(null) == positions.get(positions.size() - 1);

                boolean emptyReplaced = false;
                if (isAtZeroLengthPosition && (!isLastPosition || hasUpperBound || numberOfEmptyRegionsToAdd > 0)) {
                    insert(viewer.getDocument(), emptyCellReplacement, offset);
                    emptyReplaced = true;
                }

                if (isLastPosition && numberOfEmptyRegionsToAdd > 0
                        && !(numberOfEmptyRegionsToAdd == 1 && hasUpperBound)) {
                    insert(viewer.getDocument(), separator,
                            offset + (emptyReplaced ? emptyCellReplacement.length() : 0));
                    return true;
                }
            }
            return false;
        }
    }

    private static class JumpToExitPositionOnEscPolicy implements IExitPolicy {

        @Override
        public ExitFlags doExit(final LinkedModeModel model, final VerifyEvent event, final int offset,
                final int length) {
            return event.character == SWT.ESC ? new ExitFlags(ILinkedModeListener.UPDATE_CARET, false) : null;
        }
    }

    private enum ReplacingResult {
        NOTHING,
        REPLACED_EMPTY,
        SEPARATOR_ADDED,
        REPLACED_EMPTY_SEPARATOR_ADDED
    }
}

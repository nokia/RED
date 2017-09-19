/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug;

import static com.google.common.collect.Lists.reverse;
import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.rf.ide.core.execution.agent.event.Variable;
import org.rf.ide.core.execution.agent.event.VariableTypedValue;
import org.rf.ide.core.execution.debug.StackFrame.FrameCategory;

public class Stacktrace implements Iterable<StackFrame> {

    private final Deque<StackFrame> frames = new ArrayDeque<>();

    void push(final StackFrame frame) {
        frames.push(frame);
    }

    void pop() {
        frames.pop();
    }

    @Override
    public Iterator<StackFrame> iterator() {
        return frames.iterator();
    }

    public Stream<StackFrame> stream() {
        return frames.stream();
    }

    Optional<StackFrame> peekCurrentFrame() {
        return Optional.ofNullable(frames.peek());
    }

    Optional<StackFrame> getFirstFrameSatisfying(final Predicate<StackFrame> framePredicate) {
        return frames.stream()
                .filter(framePredicate)
                .findFirst();
    }

    StackFrame findParentFrame(final StackFrame frame) {
        StackFrame previous = null;
        final Iterator<StackFrame> iterator = frames.descendingIterator();
        while (iterator.hasNext()) {
            final StackFrame f = iterator.next();
            if (f == frame) {
                break;
            }
            previous = f;
        }
        return previous;
    }

    Optional<URI> getCurrentPath() {
        return peekCurrentFrame().flatMap(StackFrame::getCurrentSourcePath);
    }

    Optional<URI> getContextPath() {
        return peekCurrentFrame().flatMap(StackFrame::getContextPath);
    }

    public boolean isEmpty() {
        return frames.isEmpty();
    }

    int size() {
        return frames.size();
    }

    boolean hasCategoryOnTop(final FrameCategory category) {
        return peekCurrentFrame().map(frame -> frame.hasCategory(category)).orElse(false);
    }

    void destroy() {
        frames.clear();
    }

    void updateVariables(final List<Map<Variable, VariableTypedValue>> variables) {
        final List<StackFrame> reversedFrames = reverse(stream().collect(toList()));
        final List<Map<Variable, VariableTypedValue>> reversedVariables = reverse(variables);

        StackFrameVariables parentVars = StackFrameVariables.newNonLocalVariables(reversedVariables.get(0));
        for (final StackFrame frame : reversedFrames) {
            final int index = frame.getLevel() + 1;
            // when there is a keyword starting there is not variables yet send from agent for its
            // frame, so index may be equal to size of the list
            final Map<Variable, VariableTypedValue> vars = index < reversedVariables.size()
                    ? reversedVariables.get(index)
                    : null;

            if (frame.getVariables() == null) {
                StackFrameVariables newFrameVariables;
                if (frame.hasCategory(FrameCategory.SUITE)) {
                    newFrameVariables = StackFrameVariables.newNonLocalVariables(vars);
                } else if (frame.hasCategory(FrameCategory.TEST)) {
                    newFrameVariables = StackFrameVariables.newNonLocalVariables(vars);
                } else {
                    newFrameVariables = StackFrameVariables.newLocalVariables(parentVars,
                            frame.hasCategory(FrameCategory.FOR) || frame.hasCategory(FrameCategory.FOR_ITEM));
                }
                frame.setVariables(newFrameVariables);
            }
            if (vars != null) {
                frame.updateVariables(vars);
            }
            parentVars = frame.getVariables();
        }
    }

    @Override
    public String toString() {
        return frames.toString();
    }
}

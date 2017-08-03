/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.rf.ide.core.execution.debug.StackFrame.FrameCategory;

public class Stacktrace implements Iterable<StackFrame> {

    private final Deque<StackFrame> frames = new ArrayDeque<>();

    private final List<StacktraceListener> listeners = new ArrayList<>();

    private StackFrameVariables globalVars;


    void setGlobalVariables(final StackFrameVariables globalVars) {
        this.globalVars = globalVars;
    }

    StackFrameVariables getGlobalVariables() {
        return globalVars;
    }

    public void addListener(final StacktraceListener listener) {
        listeners.add(listener);
    }

    public void removeListener(final StacktraceListener listener) {
        listeners.remove(listener);
    }

    void push(final StackFrame frame) {
        frames.push(frame);

        listeners.stream().forEach(listener -> listener.framePushed(this, frame));
    }

    void pop() {
        final StackFrame frame = frames.pop();

        listeners.stream().forEach(listener -> listener.framePopped(this, frame));
        frame.destroy();
    }

    @Override
    public Iterator<StackFrame> iterator() {
        return frames.iterator();
    }

    Stream<StackFrame> stream() {
        return frames.stream();
    }

    Optional<StackFrame> peekCurrentFrame() {
        return Optional.ofNullable(frames.peek());
    }

    Optional<StackFrame> getFirstTestOrSuiteFrame() {
        return frames.stream()
                .filter(frame -> frame.hasCategory(FrameCategory.TEST) || frame.hasCategory(FrameCategory.SUITE))
                .findFirst();
    }

    StackFrame findParentFrame(final StackFrame frameToStepOver) {
        StackFrame previous = null;
        final Iterator<StackFrame> iterator = frames.descendingIterator();
        while (iterator.hasNext()) {
            final StackFrame frame = iterator.next();
            if (frame == frameToStepOver) {
                break;
            }
            previous = frame;
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
        listeners.clear();
    }

    @Override
    public String toString() {
        return frames.toString();
    }

    public static interface StacktraceListener {

        public void framePushed(Stacktrace stack, StackFrame frame);

        public void framePopped(Stacktrace stack, StackFrame frame);
    }
}

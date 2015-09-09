/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;

/**
 * @author mmarzec
 *
 */
public class KeywordFinder {

    public boolean isKeywordInBreakpointLine(final IBreakpoint breakpoint, final int breakpointLine, String keyword,
            final List<String> args, final int currentExecutionLine) {
        final IFile editedFile = (IFile) ((ILineBreakpoint) breakpoint).getMarker().getResource();
        if (editedFile == null) {
            throw new IllegalArgumentException();
        }

        final String[] s = keyword.split("\\.");
        keyword = s[s.length - 1];

        boolean result = false;
        int lineNum = 1;
        Scanner scanner = null;
        try (InputStream is = editedFile.getContents()) {
            scanner = new Scanner(is).useDelimiter("\\n");
            while (scanner.hasNext()) {
                final String line = scanner.next();
                if (line.contains(keyword)) {
                    int count = 0;
                    for (final String arg : args) {
                        if (line.contains(arg)) {
                            count++;
                        }
                    }
                    if (count == args.size() && breakpointLine == lineNum && lineNum == currentExecutionLine) {
                        result = true;
                        break;
                    }
                }
                lineNum++;
            }
        } catch (CoreException | IOException e) {
            e.printStackTrace();
        }

        if (scanner != null) {
            scanner.close();
        }

        return result;
    }

    public int getKeywordLine(final IFile editedFile, String keyword, final List<String> args,
            final List<Integer> executedLines) {

        final String[] s = keyword.split("\\.");
        keyword = s[s.length - 1];

        int result = -1;
        int lineNum = 1;
        Scanner scanner = null;
        try (InputStream is = editedFile.getContents()) {
            scanner = new Scanner(is).useDelimiter("\\n");
            while (scanner.hasNext()) {
                final String line = scanner.next();
                if (line.contains(keyword)) {
                    int count = 0;
                    for (final String arg : args) {
                        if (line.contains(arg)) {
                            count++;
                        }
                    }
                    if (count == args.size() && (executedLines==null || !executedLines.contains(lineNum))) {
                        result = lineNum;
                        break;
                    }
                }
                lineNum++;
            }
        } catch (CoreException | IOException e) {
            e.printStackTrace();
        }

        if (scanner != null) {
            scanner.close();
        }

        return result;
    }
}

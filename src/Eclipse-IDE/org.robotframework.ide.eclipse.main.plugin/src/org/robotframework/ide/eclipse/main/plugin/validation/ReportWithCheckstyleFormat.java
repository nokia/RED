/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.validation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IPath;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Table;
import com.google.common.escape.Escaper;
import com.google.common.io.Files;
import com.google.common.xml.XmlEscapers;

/**
 * @author Michal Anglart
 *
 */
public class ReportWithCheckstyleFormat implements AutoCloseable {

    private final Escaper xmlAttrEscaper = XmlEscapers.xmlAttributeEscaper();

    private final Writer writer;

    public ReportWithCheckstyleFormat(final File file) throws FileNotFoundException {
        writer = Files.newWriter(file, Charsets.UTF_8);
    }

    void writeHeader() throws IOException {
        writer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        writer.append("<checkstyle version=\"6.14\">\n");
    }

    void writeEntries(final Table<IPath, ProblemPosition, RobotProblem> problems)
            throws IOException {
        for (final IPath path : problems.rowKeySet()) {
            writer.append(Strings.repeat(" ", 2) + "<file name=\"" + xmlAttrEscaper.escape(path.toString()) + "\">\n");
            writeProblems(problems.row(path));
            writer.append(Strings.repeat(" ", 2) + "</file>\n");
        }
    }

    private void writeProblems(final Map<ProblemPosition, RobotProblem> problems) throws IOException {
        for (final Entry<ProblemPosition, RobotProblem> problemEntry : problems.entrySet()) {
            final ProblemPosition position = problemEntry.getKey();
            final RobotProblem problem = problemEntry.getValue();
            final IProblemCause cause = problem.getCause();
            writer.append(Strings.repeat(" ", 4) + "<error line=\"" + position.getLine() + "\" message=\""
                    + xmlAttrEscaper.escape(problem.getMessage()) + "\" severity=\""
                    + cause.getSeverity().getName().toLowerCase() + "\""
                    + "/>\n");
        }
    }

    void writeFooter() throws IOException {
        writer.append("</checkstyle>");
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}

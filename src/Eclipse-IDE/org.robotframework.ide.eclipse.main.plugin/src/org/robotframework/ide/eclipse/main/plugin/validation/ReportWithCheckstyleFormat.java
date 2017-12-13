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
import java.util.Collection;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProblemCategory.Severity;
import org.robotframework.ide.eclipse.main.plugin.validation.CheckstyleReportingStrategy.RobotProblemWithPosition;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
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

    void write(final Map<IPath, Collection<RobotProblemWithPosition>> problems) throws IOException {
        writeHeader();
        writeEntries(problems);
        writeFooter();
    }

    private void writeHeader() throws IOException {
        writer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        writer.append("<checkstyle version=\"6.14\">\n");
    }

    private void writeEntries(final Map<IPath, Collection<RobotProblemWithPosition>> problems)
            throws IOException {
        for (final IPath path : problems.keySet()) {
            writer.append(Strings.repeat(" ", 2) + "<file name=\"" + xmlAttrEscaper.escape(path.toString()) + "\">\n");
            writeProblems(problems.get(path));
            writer.append(Strings.repeat(" ", 2) + "</file>\n");
        }
    }

    private void writeProblems(final Collection<RobotProblemWithPosition> problems) throws IOException {
        for (final RobotProblemWithPosition problemEntry : problems) {
            final ProblemPosition position = problemEntry.getPosition();
            final RobotProblem problem = problemEntry.getProblem();
            final Severity severity = problem.getSeverity();
            writer.append(Strings.repeat(" ", 4) + "<error line=\"" + position.getLine() + "\" message=\""
                    + xmlAttrEscaper.escape(problem.getMessage()) + "\" severity=\""
                    + severity.getName().toLowerCase() + "\""
                    + "/>\n");
        }
    }

    private void writeFooter() throws IOException {
        writer.append("</checkstyle>");
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}

/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.ITextContentDescriber;
import org.rf.ide.core.testdata.mapping.table.ElementPositionResolver;
import org.rf.ide.core.testdata.mapping.table.ElementPositionResolver.PositionExpected;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.header.TestCasesTableHeaderRecognizer;
import org.rf.ide.core.testdata.text.read.separators.ALineSeparator;
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder;

import com.google.common.base.Charsets;

public abstract class ASuiteFileDescriber implements ITextContentDescriber {

    public static final String SUITE_FILE_CONTENT_ID = "org.robotframework.red.robotsuitefile";
    public static final String SUITE_FILE_TSV_CONTENT_ID = "org.robotframework.red.robotsuitefile_tsv";
    public static final String RESOURCE_FILE_CONTENT_ID = "org.robotframework.red.robotfile";
    public static final String INIT_FILE_CONTENT_ID = "org.robotframework.red.robotsuiteinitfile";

    private final ElementPositionResolver positionResolver = new ElementPositionResolver();

    private final Pattern newFullComplex = Pattern
            .compile("^" + TestCasesTableHeaderRecognizer.EXPECTED.pattern() + "(\\s)?$");

    private final TokenSeparatorBuilder tokenSeparatorBuilder;

    public ASuiteFileDescriber(final TokenSeparatorBuilder tokenSeparatorBuilder) {
        this.tokenSeparatorBuilder = tokenSeparatorBuilder;
    }

    public static boolean isSuiteFile(final IFile resource) {
        return hasContentType(resource, SUITE_FILE_CONTENT_ID);
    }

    public static boolean isResourceFile(final IFile resource) {
        return hasContentType(resource, RESOURCE_FILE_CONTENT_ID);
    }

    public static boolean isInitializationFile(final IFile resource) {
        return hasContentType(resource, INIT_FILE_CONTENT_ID);
    }

    private static boolean hasContentType(final IFile resource, final String id) {
        try {
            final IContentDescription contentDescription = resource.getContentDescription();
            return contentDescription != null && contentDescription.getContentType().getId().startsWith(id);
        } catch (final CoreException e) {
            return false;
        }
    }

    @Override
    public int describe(final InputStream contents, final IContentDescription description) throws IOException {
        return describe(new InputStreamReader(contents, getTextFileCharset()), description);
    }

    private Charset getTextFileCharset() {
        try {
            return Charset.forName(ResourcesPlugin.getEncoding());
        } catch (final Exception e) {
            return Charsets.UTF_8;
        }
    }

    @Override
    public QualifiedName[] getSupportedOptions() {
        return new QualifiedName[0];
    }

    @Override
    public int describe(final Reader contents, final IContentDescription description) throws IOException {
        final BufferedReader br = new BufferedReader(contents);
        String line = br.readLine();
        while (line != null) {
            final ALineSeparator separator = tokenSeparatorBuilder.createSeparator(-1, line);
            final List<IRobotLineElement> splittedLine = separator.getSplittedLine();

            final RobotLine currentLine = new RobotLine(0, null);
            final RobotToken currentToken = packSeparatorsUntilRobotTokenFound(currentLine, splittedLine);
            if (currentToken != null && hasCorrectPosition(currentLine, currentToken)
                    && containsTestCaseTableHeader(currentToken)) {
                return VALID;
            }
            line = br.readLine();
        }
        return INVALID;
    }

    private boolean hasCorrectPosition(final RobotLine currentLine, final RobotToken currentToken) {
        return positionResolver.isCorrectPosition(PositionExpected.TABLE_HEADER, null,
                currentLine, currentToken);
    }

    private boolean containsTestCaseTableHeader(final RobotToken currentToken) {
        return newFullComplex.matcher(currentToken.getText()).find();
    }

    private RobotToken packSeparatorsUntilRobotTokenFound(final RobotLine currentLine,
            final List<IRobotLineElement> splittedLine) {

        for (final IRobotLineElement elem : splittedLine) {
            if (elem instanceof Separator) {
                currentLine.addLineElement(elem);
            } else {
                return (RobotToken) elem;
            }
        }
        return null;
    }
}

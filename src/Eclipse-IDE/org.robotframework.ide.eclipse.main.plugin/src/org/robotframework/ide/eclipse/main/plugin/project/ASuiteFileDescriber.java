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
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.ITextContentDescriber;
import org.rf.ide.core.testdata.mapping.table.ElementPositionResolver;
import org.rf.ide.core.testdata.mapping.table.ElementPositionResolver.PositionExpected;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.ALineSeparator;
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder;

import com.google.common.base.Charsets;

public abstract class ASuiteFileDescriber implements ITextContentDescriber {

    private static final String SUITE_FILE_CONTENT_ID_PREFIX = "org.robotframework.red.robotsuitefile";
    static final String SUITE_FILE_ROBOT_CONTENT_ID = "org.robotframework.red.robotsuitefile_robot";
    static final String SUITE_FILE_TSV_CONTENT_ID = "org.robotframework.red.robotsuitefile_tsv";

    private static final String RPA_SUITE_FILE_CONTENT_ID_PREFIX = "org.robotframework.red.rpa.robotsuitefile";
    static final String RPA_SUITE_FILE_ROBOT_CONTENT_ID = "org.robotframework.red.rpa.robotsuitefile_robot";
    static final String RPA_SUITE_FILE_TSV_CONTENT_ID = "org.robotframework.red.rpa.robotsuitefile_tsv";

    private static final String RESOURCE_FILE_CONTENT_ID = "org.robotframework.red.robotfile";

    private static final String INIT_FILE_CONTENT_ID = "org.robotframework.red.robotsuiteinitfile";


    private final ElementPositionResolver positionResolver;

    private final TokenSeparatorBuilder tokenSeparatorBuilder;

    private final Pattern requiredHeader;

    private final Pattern forbiddenHeader;

    public ASuiteFileDescriber(final TokenSeparatorBuilder tokenSeparatorBuilder, final Pattern requiredHeader,
            final Pattern forbiddenHeader) {
        this.positionResolver = new ElementPositionResolver();
        this.tokenSeparatorBuilder = tokenSeparatorBuilder;
        this.requiredHeader = requiredHeader;
        this.forbiddenHeader = forbiddenHeader;
    }

    public static String getContentType(final String filename, final String content) {
        if (content == null) {
            return null;
        } else if (RobotFile.INIT_NAMES.stream().anyMatch(filename::equalsIgnoreCase)) {
            return INIT_FILE_CONTENT_ID;
        }

        final String filenameExtension = filename.contains(".") ? filename.substring(filename.lastIndexOf('.') + 1)
                : null;
        final List<Supplier<ASuiteFileDescriber>> describers = new ArrayList<>();
        if ("tsv".equals(filenameExtension)) {
            describers.add(() -> new TsvSuiteFileDescriber());
            describers.add(() -> new TsvRpaSuiteFileDescriber());

        } else {
            describers.add(() -> new RobotSuiteFileDescriber());
            describers.add(() -> new RobotRpaSuiteFileDescriber());
        }

        for (final Supplier<ASuiteFileDescriber> describerSupplier : describers) {
            final ASuiteFileDescriber describer = describerSupplier.get();
            try {
                final StringReader reader = new StringReader(content);
                if (describer.describe(reader, null) == IContentDescriber.VALID) {
                    return describer.getContentTypeId();
                }
            } catch (final IOException e) {
                return null;
            }
        }
        return RESOURCE_FILE_CONTENT_ID;
    }

    public static boolean isSuiteFile(final IFile resource) {
        return hasContentType(resource, SUITE_FILE_CONTENT_ID_PREFIX);
    }

    public static boolean isSuiteFile(final String contentTypeId) {
        return contentTypeId != null && contentTypeId.startsWith(SUITE_FILE_CONTENT_ID_PREFIX);
    }

    public static boolean isRpaSuiteFile(final IFile resource) {
        return hasContentType(resource, RPA_SUITE_FILE_CONTENT_ID_PREFIX);
    }

    public static boolean isRpaSuiteFile(final String contentTypeId) {
        return contentTypeId != null && contentTypeId.startsWith(RPA_SUITE_FILE_CONTENT_ID_PREFIX);
    }

    public static boolean isResourceFile(final IFile resource) {
        return hasContentType(resource, RESOURCE_FILE_CONTENT_ID);
    }

    public static boolean isResourceFile(final String contentTypeId) {
        return RESOURCE_FILE_CONTENT_ID.equals(contentTypeId);
    }

    public static boolean isInitializationFile(final IFile resource) {
        return hasContentType(resource, INIT_FILE_CONTENT_ID);
    }

    public static boolean isInitializationFile(final String contentTypeId) {
        return INIT_FILE_CONTENT_ID.equals(contentTypeId);
    }

    private static boolean hasContentType(final IFile resource, final String id) {
        try {
            final IContentDescription contentDescription = resource.getContentDescription();
            return contentDescription != null && contentDescription.getContentType().getId().startsWith(id);
        } catch (final CoreException e) {
            return false;
        }
    }

    protected abstract String getContentTypeId();

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
            if (currentToken != null && hasCorrectPosition(currentLine, currentToken)) {
                if (containsRequiredTableHeader(currentToken)) {
                    return VALID;

                } else if (containsForbiddenTableHeader(currentToken)) {
                    return INVALID;
                }
            }
            line = br.readLine();
        }
        return INVALID;
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

    private boolean hasCorrectPosition(final RobotLine currentLine, final RobotToken currentToken) {
        return positionResolver.isCorrectPosition(PositionExpected.TABLE_HEADER, currentLine, currentToken);
    }

    private boolean containsRequiredTableHeader(final RobotToken currentToken) {
        return requiredHeader.matcher(currentToken.getText()).find();
    }

    private boolean containsForbiddenTableHeader(final RobotToken currentToken) {
        return forbiddenHeader.matcher(currentToken.getText()).find();
    }
}

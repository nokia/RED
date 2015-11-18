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
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.ITextContentDescriber;
import org.robotframework.ide.core.testData.model.table.mapping.ElementPositionResolver;
import org.robotframework.ide.core.testData.model.table.mapping.ElementPositionResolver.PositionExpected;
import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.columnSeparators.ALineSeparator;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator;
import org.robotframework.ide.core.testData.text.read.columnSeparators.TokenSeparatorBuilder;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.header.TestCasesTableHeaderRecognizer;

public class RobotSuiteFileDescriber implements ITextContentDescriber {

	public static final String SUITE_FILE_CONTENT_ID = "org.robotframework.red.robotsuitefile";

	public static final String RESOURCE_FILE_CONTENT_ID = "org.robotframework.red.robotfile";

	public static final String INIT_FILE_CONTENT_ID = "org.robotframework.red.robotsuiteinitfile";

	private static final TokenSeparatorBuilder tokenSeparatorBuilder = new TokenSeparatorBuilder();
	private static final ElementPositionResolver positionResolver = new ElementPositionResolver();
	private static final Pattern newFullComplex = Pattern
			.compile("^" + TestCasesTableHeaderRecognizer.EXPECTED.pattern() + "((\\s){2,})?$");

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
			return contentDescription != null && id.equals(contentDescription.getContentType().getId());
		} catch (final CoreException e) {
			return false;
		}
	}

	@Override
	public int describe(final InputStream contents, final IContentDescription description) throws IOException {
		return describe(new InputStreamReader(contents), description);
	}

	@Override
	public QualifiedName[] getSupportedOptions() {
		return new QualifiedName[0];
	}

	@Override
	public int describe(final Reader contents, final IContentDescription description) throws IOException {
		int describedResult = INVALID;

		final BufferedReader br = new BufferedReader(contents);
		String line = null;
		while ((line = br.readLine()) != null) {
			ALineSeparator separator = tokenSeparatorBuilder.createSeparator(-1, line);
			List<IRobotLineElement> splittedLine = separator.getSplittedLine();
			RobotLine currentLine = new RobotLine(0, null);
			RobotToken currentToken = packSeparatorsUntilRobotTokenFound(currentLine, splittedLine);
			if (currentToken != null) {
				if (positionResolver.isCorrectPosition(PositionExpected.TABLE_HEADER, null, currentLine,
						currentToken)) {
					if (newFullComplex.matcher(currentToken.getRaw().toString()).find()) {
						describedResult = VALID;
						break;
					}
				}
			}
		}

		return describedResult;
	}

	private RobotToken packSeparatorsUntilRobotTokenFound(final RobotLine currentLine,
			final List<IRobotLineElement> splittedLine) {
		RobotToken currentToken = null;
		for (IRobotLineElement elem : splittedLine) {
			if (elem instanceof Separator) {
				currentLine.addLineElement(elem);
			} else {
				currentToken = (RobotToken) elem;
				break;
			}
		}
		return currentToken;
	}

}

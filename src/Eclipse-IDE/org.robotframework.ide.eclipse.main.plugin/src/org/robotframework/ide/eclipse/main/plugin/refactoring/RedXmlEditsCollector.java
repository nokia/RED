/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.robotframework.ide.eclipse.main.plugin.refactoring.MatchingEngine.MatchAccess;

import com.google.common.io.CharStreams;

/**
 * @author Michal Anglart
 */
class RedXmlEditsCollector {

    private final IPath pathBeforeRefactoring;

    private final Optional<IPath> pathAfterRefactoring;

    RedXmlEditsCollector(final IPath pathBeforeRefactoring, final Optional<IPath> pathAfterRefactoring) {
        this.pathBeforeRefactoring = pathBeforeRefactoring;
        this.pathAfterRefactoring = pathAfterRefactoring;
    }

    List<TextEdit> collectEditsInExcludedPaths(final String projectName, final IDocument redXmlDocument) {
        return collectEditsInExcludedPaths(projectName, new MatchesInDocumentEngine(redXmlDocument));
    }

    List<TextEdit> collectEditsInExcludedPaths(final String projectName, final IFile redXmlFile) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(redXmlFile.getContents()))) {

            final IDocument document = new Document(CharStreams.toString(fileReader));
            return collectEditsInExcludedPaths(projectName, new MatchesInDocumentEngine(document));
        } catch (IOException | CoreException e) {
            return new ArrayList<>();
        }
    }

    List<TextEdit> collectEditsInMovedLibraries(final String projectName, final IDocument redXmlDocument) {
        return collectEditsInMovedLibraries(projectName, new MatchesInDocumentEngine(redXmlDocument));
    }

    List<TextEdit> collectEditsInMovedLibraries(final String projectName, final IFile redXmlFile) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(redXmlFile.getContents()))) {

            final IDocument document = new Document(CharStreams.toString(fileReader));
            return collectEditsInMovedLibraries(projectName, new MatchesInDocumentEngine(document));
        } catch (IOException | CoreException e) {
            return new ArrayList<>();
        }
    }

    private List<TextEdit> collectEditsInMovedLibraries(final String projectName, final MatchingEngine engine) {
        if (!projectName.equals(pathBeforeRefactoring.segment(0))) {
            return new ArrayList<>();
        }

        String namePattern;
        if (pathBeforeRefactoring.lastSegment().contains(".py")
                || pathBeforeRefactoring.lastSegment().contains(".java")) {
            namePattern = pathBeforeRefactoring.lastSegment().substring(0,
                    pathBeforeRefactoring.lastSegment().lastIndexOf("."));
        } else {
            namePattern = "^\"";
        }
        final String toMatch = "\\s*(<referencedLibrary\\s*type=\"([^\"]*)\"\\s*name=\"([" + namePattern
                + "]*)\"\\s*path=\"([^\"]*)\"/>)\\s*";

        final Pattern toMatchPattern = Pattern.compile(toMatch);

        final LibraryMovedMatchesAccess matchesAccess = new LibraryMovedMatchesAccess(toMatchPattern);
        // FIXME: Search for matches temporary disabled in RED-871 because of RED-860 & RED-864 bugs
        // engine.searchForMatches(toMatch, matchesAccess);
        return matchesAccess.getEdits();
    }

    private List<TextEdit> collectEditsInExcludedPaths(final String projectName, final MatchingEngine engine) {
        if (!projectName.equals(pathBeforeRefactoring.segment(0))) {
            // the change affected something from different project, so excluded paths in the
            // project of our red.xml files are not affected at all
            return new ArrayList<>();
        }

        final String toMatch = "\\.*(<excludedPath\\s*path=\"([^\"]*)\"/>)\\s*";

        final Pattern toMatchPattern = Pattern.compile(toMatch);

        final ExcludedPathsMatchesAccess matchesAccess = new ExcludedPathsMatchesAccess(toMatchPattern);
        engine.searchForMatches(toMatch, matchesAccess);
        return matchesAccess.getEdits();
    }

    private final class ExcludedPathsMatchesAccess implements MatchAccess {

        private final Pattern toMatchPattern;

        private final List<TextEdit> edits;

        private ExcludedPathsMatchesAccess(final Pattern toMatchPattern) {
            this.toMatchPattern = toMatchPattern;
            this.edits = new ArrayList<>();
        }

        public List<TextEdit> getEdits() {
            return edits;
        }

        @Override
        public void onMatch(final String matchingContent, final Position matchPosition) {
            final Matcher matcher = toMatchPattern.matcher(matchingContent);
            matcher.find();

            final IPath potentiallyAffectedPath = Path.fromPortableString(matcher.group(2));
            final IPath adjustedPathBeforeRefactoring = Changes
                    .escapeXmlCharacters(pathBeforeRefactoring.removeFirstSegments(1));
            if (pathAfterRefactoring.isPresent()) {
                final IPath adjustedPathAfterRefactoring = Changes
                        .escapeXmlCharacters(pathAfterRefactoring.get().removeFirstSegments(1));

                final Optional<IPath> transformedPath = Changes.transformAffectedPath(adjustedPathBeforeRefactoring,
                        adjustedPathAfterRefactoring, potentiallyAffectedPath);

                if (transformedPath.isPresent()) {
                    final String toInsert = "<excludedPath path=\"" + transformedPath.get().toPortableString() + "\"/>";
                    final int startShift = matcher.start(1);
                    final int endShift = matchingContent.length() - matcher.end(1) + startShift;
                    edits.add(new ReplaceEdit(matchPosition.getOffset() + startShift,
                            matchPosition.getLength() - endShift, toInsert));
                }
            } else if (adjustedPathBeforeRefactoring.isPrefixOf(potentiallyAffectedPath)) {
                edits.add(new DeleteEdit(matchPosition.getOffset(), matchPosition.getLength()));
            }
        }
    }

    private final class LibraryMovedMatchesAccess implements MatchAccess {

        private final Pattern toMatchPattern;

        private final List<TextEdit> edits;

        private LibraryMovedMatchesAccess(final Pattern toMatchPattern) {
            this.toMatchPattern = toMatchPattern;
            this.edits = new ArrayList<>();
        }

        public List<TextEdit> getEdits() {
            return edits;
        }

        @Override
        public void onMatch(final String matchingContent, final Position matchPosition) {
            final Matcher matcher = toMatchPattern.matcher(matchingContent);
            matcher.find();
            final String type = matcher.group(2);
            final String name = matcher.group(3);

            final IPath potentiallyAffectedPath = Path.fromPortableString(matcher.group(4));
            final IPath adjustedPathBeforeRefactoring = Changes.escapeXmlCharacters(pathBeforeRefactoring);
            if (pathAfterRefactoring.isPresent()) {
                final IPath adjustedPathAfterRefactoring = Changes.escapeXmlCharacters(pathAfterRefactoring.get());
                final Optional<IPath> transformedPath = Changes.transformAffectedPath(adjustedPathBeforeRefactoring,
                        adjustedPathAfterRefactoring, potentiallyAffectedPath);

                String toInsert;
                if (transformedPath.isPresent()) {
                    final String finalPath = transformedPath.get().toPortableString();
                    toInsert = "<referencedLibrary type=\"" + type + "\" name=\"" + name + "\" path=\""
                            + finalPath.substring(1);

                } else {
                    final String finalPath = adjustedPathAfterRefactoring.removeLastSegments(1).toPortableString();
                    toInsert = "<referencedLibrary type=\"" + type + "\" name=\""
                            + adjustedPathAfterRefactoring.lastSegment().substring(0,
                                    adjustedPathAfterRefactoring.lastSegment().lastIndexOf('.'))
                            + "\" path=\"" + finalPath.substring(1, finalPath.length());
                }

                final int startShift = matcher.start(1);
                final int endShift = matchingContent.length() - matcher.end(4) + startShift;
                edits.add(new ReplaceEdit(matchPosition.getOffset() + startShift, matchPosition.getLength() - endShift,
                        toInsert));

            } else if (adjustedPathBeforeRefactoring.isPrefixOf(potentiallyAffectedPath)) {
                edits.add(new DeleteEdit(matchPosition.getOffset(), matchPosition.getLength()));
            }

        }
    }

}

/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.robotframework.ide.eclipse.main.plugin.refactoring.MatchingEngine.MatchAccess;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 *
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
        return collectEditsInExcludedPaths(projectName, new MatchesInFileEngine(redXmlFile));
    }

    private List<TextEdit> collectEditsInExcludedPaths(final String projectName, final MatchingEngine engine) {
        if (!projectName.equals(pathBeforeRefactoring.segment(0))) {
            // the change affected something from different project, so excluded paths in the
            // project of our red.xml files are not affected at all
            return new ArrayList<>();
        }

        final String toMatch = "\\s*(<excludedPath\\s*path=\"([^\"]*)\"/>)\\s*";
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
                    .excapeXmlCharacters(pathBeforeRefactoring.removeFirstSegments(1));
            if (pathAfterRefactoring.isPresent()) {
                final IPath adjustedPathAfterRefactoring = Changes
                        .excapeXmlCharacters(pathAfterRefactoring.get().removeFirstSegments(1));

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
}

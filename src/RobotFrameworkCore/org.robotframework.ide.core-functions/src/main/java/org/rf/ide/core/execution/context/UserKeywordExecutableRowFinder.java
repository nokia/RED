/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.context;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.rf.ide.core.execution.context.RobotDebugExecutionContext.KeywordContext;
import org.rf.ide.core.testdata.RobotParser;
import org.rf.ide.core.testdata.importer.ResourceImportReference;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotFileOutput.RobotFileType;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.rf.ide.core.testdata.model.search.keyword.KeywordSearcher;
import org.rf.ide.core.testdata.model.search.keyword.KeywordSearcher.Extractor;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.io.Files;

/**
 * @author mmarzec
 */
public class UserKeywordExecutableRowFinder implements IRobotExecutableRowFinder {

    private List<UserKeyword> userKeywords;

    private List<ResourceImportReference> resourceImportReferences = new ArrayList<>();

    private ListMultimap<String, UserKeyword> accessibleKeywords = ArrayListMultimap.create();

    private KeywordSearcher keywordSearcher;

    private UserKeywordExtractor userKeywordExtractor;

    private RobotParser robotParser;

    public UserKeywordExecutableRowFinder(final RobotParser robotParser, final List<UserKeyword> userKeywords,
            final List<ResourceImportReference> resourceImportReferencesTestSuite) {
        this.robotParser = robotParser;
        this.userKeywords = userKeywords;
        this.userKeywordExtractor = new UserKeywordExtractor();
        this.keywordSearcher = new KeywordSearcher();
        collectAllReferences(new HashSet<Path>(0), resourceImportReferencesTestSuite);
        fillAllKeywordsMap(null, getLocalTestSuiteKeywords());
        addKeywordsFromReferences(resourceImportReferences);
    }

    @Override
    public RobotExecutableRow<?> findExecutableRow(final List<KeywordContext> currentKeywords) {
        RobotExecutableRow<UserKeyword> executionRow = null;
        final KeywordContext parentKeywordContext = currentKeywords.get(currentKeywords.size() - 2);

        UserKeyword newUserKeyword = parentKeywordContext.getUserKeyword();

        if (newUserKeyword == null) {
            final String keywordName = extractIfNameIsFromVariableDeclaration(parentKeywordContext.getName());
            ListMultimap<String, UserKeyword> foundKeywords = keywordSearcher.findKeywords(accessibleKeywords.asMap(),
                    accessibleKeywords.values(), userKeywordExtractor, keywordName, true);
            List<UserKeyword> bestMatchingKeywords = keywordSearcher.getBestMatchingKeyword(foundKeywords,
                    userKeywordExtractor, keywordName);

            if (bestMatchingKeywords.size() == 1) {
                newUserKeyword = bestMatchingKeywords.get(0);
            } else if (bestMatchingKeywords.size() > 1) {
                // find local
                for (final UserKeyword currentUserKeyword : bestMatchingKeywords) {
                    if (userKeywordExtractor.scope(currentUserKeyword) == KeywordScope.LOCAL) {
                        newUserKeyword = currentUserKeyword;
                        break;
                    }
                }
            }
        }

        if (newUserKeyword != null) {
            if (userKeywordExtractor.scope(newUserKeyword) == KeywordScope.RESOURCE) {
                ResourceImportReference reference = findResource(parentKeywordContext, newUserKeyword);

                if (reference != null) {
                    parentKeywordContext.setResourceImportReference(reference);
                }
            }

            parentKeywordContext.setUserKeyword(newUserKeyword);
            executionRow = findKeywordExecutionRow(newUserKeyword, parentKeywordContext);
        }

        return executionRow;
    }

    private ResourceImportReference findResource(final KeywordContext parentKeywordContext,
            UserKeyword newUserKeyword) {
        ResourceImportReference reference = null;
        if (parentKeywordContext.getResourceImportReference() != null) {
            reference = parentKeywordContext.getResourceImportReference();
        } else {
            for (final ResourceImportReference refImp : resourceImportReferences) {
                if (refImp.getReference().getFileModel().getKeywordTable().getKeywords().contains(newUserKeyword)) {
                    reference = refImp;
                    break;
                }
            }
        }
        return reference;
    }

    private RobotExecutableRow<UserKeyword> findKeywordExecutionRow(final UserKeyword userKeyword,
            final KeywordContext parentKeywordContext) {
        final List<RobotExecutableRow<UserKeyword>> executableRows = userKeyword.getKeywordExecutionRows();
        if (parentKeywordContext.getKeywordExecutableRowCounter() < executableRows.size()) {
            final RobotExecutableRow<UserKeyword> executableRow = executableRows
                    .get(parentKeywordContext.getKeywordExecutableRowCounter());
            parentKeywordContext.incrementKeywordExecutableRowCounter();
            if (executableRow.isExecutable()) {
                return executableRow;
            } else {
                return findKeywordExecutionRow(userKeyword, parentKeywordContext);
            }
        }
        return null;
    }

    private String extractIfNameIsFromVariableDeclaration(final String keywordOrResourceName) {
        String name = keywordOrResourceName;
        if (name.charAt(0) == '$' || name.charAt(0) == '@' || name.charAt(0) == '&' || name.charAt(0) == '%') {
            // e.g. ${var}= resource1.MyKeyword
            final int variableDeclarationIndex = name.lastIndexOf("=");
            if (variableDeclarationIndex >= 0) {
                name = name.substring(variableDeclarationIndex + 1).trim();
            }
        }
        return name;
    }

    private void collectAllReferences(final Set<Path> visitedPaths,
            final List<ResourceImportReference> currentReferences) {
        for (final ResourceImportReference ref : currentReferences) {
            final File processedFile = ref.getReference().getProcessedFile();
            Path referencePath = processedFile.toPath().toAbsolutePath();
            if (!visitedPaths.contains(referencePath)) {
                visitedPaths.add(referencePath);
                resourceImportReferences.add(ref);
                if (this.robotParser != null
                        && processedFile.lastModified() != ref.getReference().getLastModificationEpochTime()) {
                    final List<RobotFileOutput> parse = this.robotParser.parse(processedFile);
                    if (!parse.isEmpty()) {
                        ref.updateReference(parse.get(0));
                    }
                }
                List<ResourceImportReference> referencesOfReference = ref.getReference().getResourceImportReferences();
                if (!referencesOfReference.isEmpty()) {
                    collectAllReferences(visitedPaths, referencesOfReference);
                }
            }
        }
    }

    private void addKeywordsFromReferences(final List<ResourceImportReference> resourceImportReferences) {
        for (final ResourceImportReference refImport : resourceImportReferences) {
            final String fullFileName = refImport.getReference().getProcessedFile().getName();
            fillAllKeywordsMap(Files.getNameWithoutExtension(fullFileName).toLowerCase(),
                    refImport.getReference().getFileModel().getKeywordTable().getKeywords());
        }
    }

    private void fillAllKeywordsMap(final String fileNamePrefixForResources, final List<UserKeyword> userKeywords) {
        for (final UserKeyword userKeyword : userKeywords) {
            final String keywordName = userKeyword.getName().getText();
            accessibleKeywords.put(QualifiedKeywordName.unifyDefinition(keywordName), userKeyword);
            if (fileNamePrefixForResources != null && !fileNamePrefixForResources.trim().isEmpty()) {
                accessibleKeywords.put(
                        QualifiedKeywordName.unifyDefinition(fileNamePrefixForResources + "." + keywordName),
                        userKeyword);
            }
        }
    }

    private List<UserKeyword> getLocalTestSuiteKeywords() {
        return Collections.unmodifiableList(this.userKeywords);
    }

    public void setUserKeywords(final List<UserKeyword> userKeywords) {
        this.userKeywords = userKeywords;
    }

    public void setResourceImportReferences(final List<ResourceImportReference> resourceImportReferences) {
        this.resourceImportReferences = resourceImportReferences;
    }

    public void setRobotParser(final RobotParser robotParser) {
        this.robotParser = robotParser;
    }

    private class UserKeywordExtractor implements Extractor<UserKeyword> {

        @Override
        public KeywordScope scope(final UserKeyword keyword) {
            return (getRobotFileOutputFrom(keyword).getType() == RobotFileType.RESOURCE) ? KeywordScope.RESOURCE
                    : KeywordScope.LOCAL;
        }

        @Override
        public Path path(final UserKeyword keyword) {
            return getRobotFileOutputFrom(keyword).getProcessedFile().toPath();
        }

        @Override
        public String alias(final UserKeyword keyword) {
            return "";
        }

        @Override
        public String keywordName(final UserKeyword keyword) {
            return keyword.getName().getText();
        }

        @Override
        public String sourceName(final UserKeyword keyword) {
            return Files.getNameWithoutExtension(path(keyword).toFile().getName()).toLowerCase();
        }

        private RobotFileOutput getRobotFileOutputFrom(final UserKeyword keyword) {
            return keyword.getParent().getParent().getParent();
        }
    }
}

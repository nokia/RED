/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.execution.context;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.execution.context.IRobotExecutableRowFinder;
import org.robotframework.ide.core.execution.context.RobotDebugExecutionContext.KeywordContext;
import org.robotframework.ide.core.testData.importer.ResourceImportReference;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;

import com.google.common.io.Files;

/**
 * @author mmarzec
 *
 */
public class UserKeywordExecutableRowFinder implements IRobotExecutableRowFinder {

    private List<UserKeyword> userKeywords;

    private List<ResourceImportReference> resourceImportReferences;

    public UserKeywordExecutableRowFinder(final List<UserKeyword> userKeywords,
            final List<ResourceImportReference> resourceImportReferences) {
        this.userKeywords = userKeywords;
        this.resourceImportReferences = resourceImportReferences;
    }

    @Override
    public RobotExecutableRow<?> findExecutableRow(final LinkedList<KeywordContext> currentKeywords) {
        RobotExecutableRow<UserKeyword> executionRow = null;
        final KeywordContext parentKeywordContext = currentKeywords.get(currentKeywords.size() - 2);
        // search in keywords from Keywords section
        final UserKeyword userKeyword = findUserKeyword(parentKeywordContext);
        if (userKeyword != null) {
            if (parentKeywordContext.getUserKeyword() == null) {
                parentKeywordContext.setUserKeyword(userKeyword);
            }
            executionRow = findKeywordExecutionRow(userKeyword, parentKeywordContext);
        } else {
            // search in resources from Settings section
            ResourceImportReference resourceImportReference = findResource(parentKeywordContext);
            if (resourceImportReference == null) {
                // if keyword is in other keyword in resource file
                resourceImportReference = findLastResourceImportReferenceInCurrentKeywords(currentKeywords);
            }
            if (resourceImportReference != null) {
                if (parentKeywordContext.getResourceImportReference() == null) {
                    parentKeywordContext.setResourceImportReference(resourceImportReference);
                }
                final UserKeyword userResourceKeyword = findResourceKeyword(resourceImportReference,
                        parentKeywordContext); // search in keywords from Keywords section in
                                               // resource file
                if (userResourceKeyword != null) {
                    if (parentKeywordContext.getUserKeyword() == null) {
                        parentKeywordContext.setUserKeyword(userResourceKeyword);
                    }
                    executionRow = findKeywordExecutionRow(userResourceKeyword, parentKeywordContext);
                }
            }
        }
        return executionRow;
    }

    private UserKeyword findUserKeyword(final KeywordContext keywordContext) {
        if (keywordContext.getUserKeyword() != null) {
            return keywordContext.getUserKeyword();
        }
        final String keywordName = extractNameFromVariableDeclaration(keywordContext.getName());
        for (final UserKeyword userKeyword : userKeywords) {
            if (userKeyword.getKeywordName().getText().toString().equalsIgnoreCase(keywordName)) {
                return userKeyword;
            }
        }
        return null;
    }

    private RobotExecutableRow<UserKeyword> findKeywordExecutionRow(final UserKeyword userKeyword,
            final KeywordContext parentKeywordContext) {
        final List<RobotExecutableRow<UserKeyword>> executableRows = userKeyword.getKeywordExecutionRows();
        if (parentKeywordContext.getKeywordExecutableRowCounter() < executableRows.size()) {
            final RobotExecutableRow<UserKeyword> executableRow = executableRows.get(parentKeywordContext.getKeywordExecutableRowCounter());
            parentKeywordContext.incrementKeywordExecutableRowCounter();
            if (executableRow.isExecutable()) {
                return executableRow;
            } else {
                return findKeywordExecutionRow(userKeyword, parentKeywordContext);
            }
        }
        return null;
    }

    private ResourceImportReference findResource(final KeywordContext keywordContext) {
        if (keywordContext.getResourceImportReference() != null) {
            return keywordContext.getResourceImportReference();
        }
        final String[] nameElements = keywordContext.getName().split("\\.");
        if (nameElements.length > 0) {
            final String resourceName = extractNameFromVariableDeclaration(nameElements[0]);
            final List<ResourceImportReference> referencesByFileName = new ArrayList<>();
            findImportReferencesByFileName(resourceName, resourceImportReferences, referencesByFileName);
            return findImportReferenceByKeywordName(keywordContext.getName(), referencesByFileName);
        }
        return null;
    }

    private String extractNameFromVariableDeclaration(final String keywordOrResourceName) {
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

    private void findImportReferencesByFileName(final String name, final List<ResourceImportReference> references,
            final List<ResourceImportReference> resultReferences) {
        for (final ResourceImportReference resourceImportReference : references) {
            if (name.equalsIgnoreCase(Files.getNameWithoutExtension(resourceImportReference.getReference()
                    .getProcessedFile()
                    .getAbsolutePath()))) {
                resultReferences.add(resourceImportReference);
            }
            //try to find in nested resource files
            findImportReferencesByFileName(name, resourceImportReference.getReference().getResourceImportReferences(),
                    resultReferences);
        }
    }
    
    private ResourceImportReference findImportReferenceByKeywordName(final String name,
            final List<ResourceImportReference> references) {
        if (references.size() == 1) {
            return references.get(0);
        } else { //resource files with the same name
            for (ResourceImportReference resourceImportReference : references) {
                if (findResourceKeyword(resourceImportReference, new KeywordContext(name, "")) != null) {
                    return resourceImportReference;
                }
            }
        }
        return null;
    }

    private ResourceImportReference findLastResourceImportReferenceInCurrentKeywords(
            final LinkedList<KeywordContext> currentKeywords) {
        for (int i = currentKeywords.size() - 1; i >= 0; i--) {
            if (currentKeywords.get(i).getResourceImportReference() != null) {
                return currentKeywords.get(i).getResourceImportReference();
            }
        }

        return null;
    }

    private UserKeyword findResourceKeyword(final ResourceImportReference resourceImportReference,
            final KeywordContext keywordContext) {
        if (keywordContext.getUserKeyword() != null) {
            return keywordContext.getUserKeyword();
        }
        String name = keywordContext.getName();
        final String[] nameElements = keywordContext.getName().split("\\.");    //e.g. resource1.MyKeyword
        if (nameElements.length > 1) {
            name = nameElements[1];
        }

        final List<UserKeyword> keywords = resourceImportReference.getReference()
                .getFileModel()
                .getKeywordTable()
                .getKeywords();
        for (final UserKeyword userKeyword : keywords) {
            if (userKeyword.getKeywordName().getText().toString().equalsIgnoreCase(name)) {
                return userKeyword;
            }
        }

        return null;
    }

    public void setUserKeywords(List<UserKeyword> userKeywords) {
        this.userKeywords = userKeywords;
    }

    public void setResourceImportReferences(List<ResourceImportReference> resourceImportReferences) {
        this.resourceImportReferences = resourceImportReferences;
    }

}

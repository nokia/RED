/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.context;

import org.rf.ide.core.testdata.importer.ResourceImportReference;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;

public class KeywordContext {

    private final String name;

    private final String type;

    private int keywordExecutableRowCounter = 0;

    private ResourceImportReference resourceImportReference;

    private UserKeyword userKeyword;

    public KeywordContext(final String name, final String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public int getKeywordExecutableRowCounter() {
        return keywordExecutableRowCounter;
    }

    public void incrementKeywordExecutableRowCounter() {
        keywordExecutableRowCounter++;
    }

    public ResourceImportReference getResourceImportReference() {
        return resourceImportReference;
    }

    public void setResourceImportReference(final ResourceImportReference resourceImportReference) {
        this.resourceImportReference = resourceImportReference;
    }

    public UserKeyword getUserKeyword() {
        return userKeyword;
    }

    public void setUserKeyword(final UserKeyword userKeyword) {
        this.userKeyword = userKeyword;
    }

    public String getType() {
        return type;
    }

}

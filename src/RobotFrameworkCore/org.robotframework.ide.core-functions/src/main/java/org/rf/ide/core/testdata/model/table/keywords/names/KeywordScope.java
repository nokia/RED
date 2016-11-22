/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.keywords.names;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

public enum KeywordScope {
    LOCAL,
    RESOURCE,
    REF_LIBRARY,
    STD_LIBRARY;

    public static List<KeywordScope> defaultOrder() {
        return newArrayList(LOCAL, RESOURCE, REF_LIBRARY, STD_LIBRARY);
    }
}
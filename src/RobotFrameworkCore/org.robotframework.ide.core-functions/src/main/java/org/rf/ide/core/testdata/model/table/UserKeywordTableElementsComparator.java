/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.LinkedHashMap;
import java.util.Map;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;

public class UserKeywordTableElementsComparator extends AModelTypeComparator<AModelElement<UserKeyword>> {

    private final static Map<ModelType, Integer> POSITION = new LinkedHashMap<>();

    static {
        int startPosition = 1;
        POSITION.put(ModelType.USER_KEYWORD, startPosition);
        POSITION.put(ModelType.USER_KEYWORD_ARGUMENTS, ++startPosition);
        POSITION.put(ModelType.USER_KEYWORD_DOCUMENTATION, ++startPosition);
        POSITION.put(ModelType.USER_KEYWORD_TAGS, ++startPosition);
        POSITION.put(ModelType.USER_KEYWORD_TIMEOUT, ++startPosition);
        POSITION.put(ModelType.USER_KEYWORD_EXECUTABLE_ROW, ++startPosition);
        POSITION.put(ModelType.USER_KEYWORD_TEARDOWN, ++startPosition);
        POSITION.put(ModelType.USER_KEYWORD_RETURN, ++startPosition);
        POSITION.put(ModelType.USER_KEYWORD_SETTING_UNKNOWN, ++startPosition);
    }

    public UserKeywordTableElementsComparator() {
        super(POSITION);
    }
}

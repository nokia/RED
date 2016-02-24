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

public class SettingTableElementsComparator extends AModelTypeComparator<AModelElement<SettingTable>> {

    private final static Map<ModelType, Integer> POSITION = new LinkedHashMap<>();

    static {
        int startPosition = 1;
        POSITION.put(ModelType.SUITE_DOCUMENTATION, startPosition);
        POSITION.put(ModelType.SUITE_SETUP, ++startPosition);
        POSITION.put(ModelType.SUITE_TEARDOWN, ++startPosition);
        POSITION.put(ModelType.SUITE_TEST_SETUP, ++startPosition);
        POSITION.put(ModelType.SUITE_TEST_TEARDOWN, ++startPosition);
        POSITION.put(ModelType.FORCE_TAGS_SETTING, ++startPosition);
        POSITION.put(ModelType.DEFAULT_TAGS_SETTING, ++startPosition);
        POSITION.put(ModelType.SUITE_TEST_TEMPLATE, ++startPosition);
        POSITION.put(ModelType.SUITE_TEST_TIMEOUT, ++startPosition);
        POSITION.put(ModelType.METADATA_SETTING, ++startPosition);
        POSITION.put(ModelType.LIBRARY_IMPORT_SETTING, ++startPosition);
        POSITION.put(ModelType.RESOURCE_IMPORT_SETTING, ++startPosition);
        POSITION.put(ModelType.VARIABLES_IMPORT_SETTING, ++startPosition);
        POSITION.put(ModelType.SETTINGS_UNKNOWN, ++startPosition);
    }

    public SettingTableElementsComparator() {
        super(POSITION);
    }
}

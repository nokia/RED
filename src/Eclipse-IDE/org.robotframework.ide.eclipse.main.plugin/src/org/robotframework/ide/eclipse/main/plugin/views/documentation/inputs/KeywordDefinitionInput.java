/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import static com.google.common.collect.Lists.newArrayList;

import java.net.URI;
import java.util.Optional;

import org.rf.ide.core.libraries.Documentation;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;

public class KeywordDefinitionInput extends InternalElementInput<RobotKeywordDefinition> {

    public KeywordDefinitionInput(final RobotKeywordDefinition keyword) {
        super(keyword);
    }

    @Override
    protected String createHeader() {
        return keywordHeader(element);
    }

    static String keywordHeader(final RobotKeywordDefinition keyword) {
        final Optional<URI> imgUri = RedImages.getUserKeywordImageUri();

        return Headers.formatSimpleHeader(imgUri, keyword.getName(),
                newArrayList("Source", keyword.getSuiteFile().getFile().getFullPath().toOSString()),
                newArrayList("Arguments", keyword.createArgumentsDescriptor().getDescription()));
    }

    @Override
    protected Documentation createDocumentation() {
        return element.createDocumentation();
    }
}

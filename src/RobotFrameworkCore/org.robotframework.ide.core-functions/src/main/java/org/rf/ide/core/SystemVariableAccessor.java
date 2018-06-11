/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core;

import java.io.File;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

public class SystemVariableAccessor {

    public Optional<String> getValue(final String variableName) {
        return Optional.ofNullable(System.getenv(variableName));
    }

    public List<String> getPaths(final String variableName) {
        return Splitter.on(File.pathSeparatorChar).omitEmptyStrings().splitToList(
                Strings.nullToEmpty(System.getenv(variableName)));
    }
}

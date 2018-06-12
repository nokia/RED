/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.SystemVariableAccessor;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;

public class EnvironmentVariableReplacer {

    private static final Pattern ENV_VAR_PATTERN = Pattern.compile("\\%\\{[^\\%\\{\\}]+\\}");

    private final SystemVariableAccessor variableAccessor;

    private final Consumer<String> problemHandler;

    public EnvironmentVariableReplacer() {
        this(new SystemVariableAccessor());
    }

    public EnvironmentVariableReplacer(final SystemVariableAccessor variableAccessor) {
        this(variableAccessor, varName -> {
            // ignore it
        });
    }

    public EnvironmentVariableReplacer(final SystemVariableAccessor variableAccessor,
            final Consumer<String> problemHandler) {
        this.variableAccessor = variableAccessor;
        this.problemHandler = problemHandler;
    }

    public IPath replaceKnownSystemVariables(final SearchPath path) {
        final Set<String> undefinedVarNames = new LinkedHashSet<>();

        String before = path.getLocation();
        String after = replaceWithProblemReporting(before, undefinedVarNames);
        while (!before.equals(after)) {
            before = after;
            after = replaceWithProblemReporting(before, undefinedVarNames);
        }

        undefinedVarNames.forEach(problemHandler);

        return new Path(after);
    }

    private String replaceWithProblemReporting(final String path, final Set<String> undefinedVarNames) {
        final Matcher matcher = ENV_VAR_PATTERN.matcher(path);
        final StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            final String matched = matcher.group();
            final String matchedName = matched.substring(2, matched.length() - 1);
            final Optional<String> value = variableAccessor.getValue(matchedName);
            if (!value.isPresent()) {
                undefinedVarNames.add(matchedName);
            }
            final String replacement = value.orElse(matched);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

}

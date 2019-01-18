/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.postfixes;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.rf.ide.core.testdata.model.RobotFileOutput;

/**
 * @author wypych
 */
public class PostProcessingFixActions {

    private final List<IPostProcessFixer> fixers = newArrayList(
            new UnknownSettingsInExecutableTablesFixer(),
            new EndTerminatedForLoopsFixer(),
            new ForContinueForItemIssueFixer(),
            new DocumentationLineContinueMissingFixer(),
            new EmptyLinesInExecutableTablesFixer());

    public void applyFixes(final RobotFileOutput parsingOutput) {
        fixers.forEach(fixer -> fixer.applyFix(parsingOutput));
    }

    @FunctionalInterface
    static interface IPostProcessFixer {

        public void applyFix(final RobotFileOutput parsingOutput);
    }
}

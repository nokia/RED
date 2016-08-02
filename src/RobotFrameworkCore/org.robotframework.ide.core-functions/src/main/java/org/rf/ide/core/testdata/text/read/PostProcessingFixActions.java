/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.text.read.postfixes.DocumentationLineContinueMissingFixer;
import org.rf.ide.core.testdata.text.read.postfixes.FixerForForContinueForItemIssue;
import org.rf.ide.core.testdata.text.read.postfixes.IPostProcessFixAction;
import org.rf.ide.core.testdata.text.read.postfixes.UnknownSettingsInExecutableTablesFixer;

/**
 * @author wypych
 */
public class PostProcessingFixActions {

    private final List<IPostProcessFixAction> fixers = new ArrayList<>();

    public PostProcessingFixActions() {
        this.fixers.addAll(Arrays.asList(new UnknownSettingsInExecutableTablesFixer(),
                new FixerForForContinueForItemIssue(), new DocumentationLineContinueMissingFixer()));
    }

    public void applyFixes(final RobotFileOutput parsingOutput) {
        for (final IPostProcessFixAction fixer : fixers) {
            fixer.applyFix(parsingOutput);
        }
    }
}

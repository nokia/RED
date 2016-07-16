/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.collect;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

/**
 * @author wypych
 */
public class AModelElementElementsHelper {

    public static List<RobotToken> collect(final List<? extends AModelElement<?>> anyModelElements) {
        final List<RobotToken> toks = new ArrayList<RobotToken>(0);
        for (final AModelElement<?> e : anyModelElements) {
            toks.addAll(collectFromAModel(e));
        }

        return toks;
    }

    public static List<RobotToken> collectFromAModel(final AModelElement<?> e) {
        return e.getElementTokens();
    }
}

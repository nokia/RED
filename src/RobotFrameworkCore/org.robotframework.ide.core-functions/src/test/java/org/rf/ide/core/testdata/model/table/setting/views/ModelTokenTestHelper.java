/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting.views;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

class ModelTokenTestHelper {

    public static RobotToken createToken(final String text) {
        RobotToken tok = new RobotToken();
        tok.setText(text);

        return tok;
    }

    public static List<String> getText(final AKeywordBaseSetting<?> elem) {
        List<String> text = new ArrayList<>();

        if (elem.getKeywordName() != null) {
            text.add(elem.getKeywordName().getText());
        }

        text.addAll(getText(elem.getArguments()));
        text.addAll(getText(elem.getComment()));

        return text;
    }

    public static List<String> getText(List<RobotToken> tokens) {
        List<String> text = new ArrayList<>();
        for (final RobotToken tok : tokens) {
            text.add(tok.getText());
        }
        return text;
    }
}

/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

/**
 * @author Michal Anglart
 *
 */
class RobotTokens {

    static RobotToken create(final String content, final RobotTokenType... types) {
        final RobotToken token = new RobotToken();
        token.setText(content);
        token.setRaw(content);
        token.getTypes().clear();
        token.getTypes().addAll(newArrayList(types));
        return token;
    }

    static DictionaryKeyValuePair toKeyValuePair(final RobotToken rawToken) {
        final List<String> splitted = Splitter.on('=').splitToList(rawToken.getRaw());
        final String key = splitted.get(0);
        final String value = Joiner.on('=').join(splitted.subList(1, splitted.size()));

        rawToken.setType(RobotTokenType.VARIABLES_VARIABLE_VALUE);
        final RobotToken keyToken = create(key, RobotTokenType.VARIABLES_DICTIONARY_KEY);
        final RobotToken valueToken = create(value, RobotTokenType.VARIABLES_DICTIONARY_VALUE);

        return new DictionaryKeyValuePair(rawToken, keyToken, valueToken);
    }
}


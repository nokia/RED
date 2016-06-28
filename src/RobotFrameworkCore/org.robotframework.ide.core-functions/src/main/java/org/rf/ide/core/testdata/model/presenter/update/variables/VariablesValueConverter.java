/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.variables;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class VariablesValueConverter {

    public static RobotToken toRobotToken(final String c) {
        RobotToken t = new RobotToken();
        t.setText(c);

        return t;
    }

    public static DictionaryKeyValuePair fromString(final String c) {
        return DictionaryKeyValuePair.createFromRaw(c);
    }

    public static DictionaryKeyValuePair fromRobotToken(final RobotToken c) {
        return fromString(c.getText());
    }

    public static RobotToken fromDictionaryKeyValuePair(final DictionaryKeyValuePair pair) {
        return toRobotToken(pair.getRaw().getText());
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> convert(final List<?> elems, final Class<T> toConversion) {
        final List<T> c = new ArrayList<T>();
        for (final Object o : elems) {
            final Class<?> oClass = o.getClass();
            if (oClass == toConversion) {
                c.add((T) toConversion.cast(o));
            } else if (toConversion == RobotToken.class) {
                if (oClass == String.class) {
                    c.add((T) toConversion.cast(toRobotToken((String) o)));
                } else if (oClass == DictionaryKeyValuePair.class) {
                    c.add((T) toConversion.cast(fromDictionaryKeyValuePair((DictionaryKeyValuePair) o)));
                }
            } else if (toConversion == DictionaryKeyValuePair.class) {
                if (oClass == String.class) {
                    c.add((T) toConversion.cast(fromString((String) o)));
                } else if (oClass == RobotToken.class) {
                    c.add((T) toConversion.cast(fromRobotToken((RobotToken) o)));
                }
            } else {
                // possible class exception
                c.add((T) toConversion.cast(o));
            }
        }

        return c;
    }
}

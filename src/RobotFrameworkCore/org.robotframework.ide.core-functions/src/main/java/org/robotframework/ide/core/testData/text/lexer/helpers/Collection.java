package org.robotframework.ide.core.testData.text.lexer.helpers;

import java.util.LinkedList;
import java.util.List;


public class Collection {

    public static <T> List<T> createOfType(Class<T> c, T... t) {
        List<T> r = new LinkedList<>();
        for (T k : t) {
            r.add(k);
        }
        return r;
    }
}

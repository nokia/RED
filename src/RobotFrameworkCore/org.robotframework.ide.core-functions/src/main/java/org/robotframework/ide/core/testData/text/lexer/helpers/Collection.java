package org.robotframework.ide.core.testData.text.lexer.helpers;

import java.util.LinkedList;
import java.util.List;


/**
 * Extracted utility class for collection operations.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class Collection {

    /**
     * 
     * @param c
     *            type of list to create
     * @param t
     *            chain of elements to include
     * @return
     */
    public static <T> List<T> createOfType(Class<T> c,
            @SuppressWarnings("unchecked") T... elems) {
        List<T> r = new LinkedList<>();
        for (T k : elems) {
            r.add(k);
        }
        return r;
    }
}

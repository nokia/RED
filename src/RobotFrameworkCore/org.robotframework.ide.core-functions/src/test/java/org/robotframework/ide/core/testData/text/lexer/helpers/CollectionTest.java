package org.robotframework.ide.core.testData.text.lexer.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see Collection
 */
public class CollectionTest {

    @Test
    public void tesT_creationCollectionOfType_forEmptyList() {
        // execute
        List<X> p = Collection.createOfType(X.class);

        // verify
        assertThat(p).isEmpty();
    }


    @Test
    public void test_createCollectionOfType_X() {
        // prepare
        D d = new D();
        C c = new C();

        // execute
        List<X> p = Collection.createOfType(X.class, d, c);

        // verify
        assertThat(p).hasSize(2);
        assertThat(p).containsSequence(d, c);
    }

    private class D implements X {

    }

    private class C implements X {

    }

    private interface X {

    }
}

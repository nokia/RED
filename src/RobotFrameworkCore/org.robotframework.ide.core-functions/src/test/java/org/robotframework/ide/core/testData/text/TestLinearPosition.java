package org.robotframework.ide.core.testData.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;


/**
 * @author wypych
 * @see LinearPosition
 */
public class TestLinearPosition {

    @Test
    public void test_simpleCheckOfCoherence() {
        // prepare
        int line = 0;
        int column = 1;

        // execute & verify
        LinearPosition linearPosition = new LinearPosition(line, column);
        assertThat(linearPosition).isNotNull();
        assertThat(linearPosition.getLine()).isEqualTo(line);
        assertThat(linearPosition.getColumn()).isEqualTo(column);
    }
}

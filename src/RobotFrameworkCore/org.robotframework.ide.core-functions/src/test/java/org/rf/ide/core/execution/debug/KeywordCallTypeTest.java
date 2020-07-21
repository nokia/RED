/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import org.junit.jupiter.api.Test;

public class KeywordCallTypeTest {

    @Test
    public void exceptionIsThrown_whenTypeIsNotRecognized() {
        assertThatIllegalStateException().isThrownBy(() -> KeywordCallType.from("some unrecognized type"));
    }

    @Test
    public void allTypesAreCorrectlyTranslated() {
        assertThat(KeywordCallType.from("keyword")).isEqualTo(KeywordCallType.NORMAL_CALL);
        assertThat(KeywordCallType.from("setup")).isEqualTo(KeywordCallType.SETUP);
        assertThat(KeywordCallType.from("teardown")).isEqualTo(KeywordCallType.TEARDOWN);
        assertThat(KeywordCallType.from("for")).isEqualTo(KeywordCallType.FOR);
        assertThat(KeywordCallType.from("for item")).isEqualTo(KeywordCallType.FOR_ITERATION);
    }
}

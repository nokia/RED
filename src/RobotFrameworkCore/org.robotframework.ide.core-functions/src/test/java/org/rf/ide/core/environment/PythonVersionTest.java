/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.environment;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.rf.ide.core.environment.PythonVersion;

public class PythonVersionTest {

    @Test
    public void pythonVersionIsNotRecognized() throws Exception {
        assertThatIllegalStateException().isThrownBy(() -> PythonVersion.from("invalid"))
                .withMessage("Unable to recognize Python version number")
                .withNoCause();
    }

    @Test
    public void pythonVersionIsRecognized() throws Exception {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(PythonVersion.from("Robot Framework 3.0.1 (Python 2.7.14 on win32)"))
                    .isEqualTo(new PythonVersion(2, 7, 14));
            softly.assertThat(PythonVersion.from("Robot Framework 3.0.2 (Python 3.6.5 on win32)"))
                    .isEqualTo(new PythonVersion(3, 6, 5));
            softly.assertThat(PythonVersion.from("Robot Framework 3.0.3 (Jython 2.7.0 on java1.8.0_144)"))
                    .isEqualTo(new PythonVersion(2, 7, 0));
            softly.assertThat(PythonVersion.from("Robot Framework 3.0.4 (Python 2.6.6 on win32)"))
                    .isEqualTo(new PythonVersion(2, 6, 6));
            softly.assertThat(PythonVersion.from("Robot Framework 3.0.5 (Python 2.7.13+ on linux)"))
                    .isEqualTo(new PythonVersion(2, 7, 13));
            softly.assertThat(PythonVersion.from("Robot Framework 3.0.6 (Python 2.6.8a3 on linux)"))
                    .isEqualTo(new PythonVersion(2, 6, 8));
        });
    }

    @Test
    public void testNotDeprecatedVersions() throws Exception {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(new PythonVersion(2, 7, 0).isDeprecated()).isFalse();
            softly.assertThat(new PythonVersion(2, 7, 14).isDeprecated()).isFalse();
            softly.assertThat(new PythonVersion(3, 4, 4).isDeprecated()).isFalse();
            softly.assertThat(new PythonVersion(3, 5, 4).isDeprecated()).isFalse();
            softly.assertThat(new PythonVersion(3, 6, 4).isDeprecated()).isFalse();
            softly.assertThat(new PythonVersion(3, 7, 4).isDeprecated()).isFalse();
        });
    }

    @Test
    public void testDeprecatedVersions() throws Exception {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(new PythonVersion(2, 6, 6).isDeprecated()).isTrue();
            softly.assertThat(new PythonVersion(3, 2, 1).isDeprecated()).isTrue();
            softly.assertThat(new PythonVersion(3, 3, 1).isDeprecated()).isTrue();
        });
    }

}

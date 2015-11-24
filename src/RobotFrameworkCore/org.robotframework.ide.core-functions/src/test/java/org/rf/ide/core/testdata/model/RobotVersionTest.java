/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;


@SuppressWarnings("PMD.MethodNamingConventions")
public class RobotVersionTest {

    @Test
    public void versionsAreProperlyParsed() {
        assertThat(RobotVersion.from("1.2")).isEqualTo(new RobotVersion(1, 2));
        assertThat(RobotVersion.from("1.2")).isNotEqualTo(
                new RobotVersion(1, 2, 0));

        assertThat(RobotVersion.from("1.2.3")).isEqualTo(
                new RobotVersion(1, 2, 3));

        assertThat(RobotVersion.from(
                "Robot Framework 2.9.1 (Python 2.7.9 on win32)").isEqualTo(
                new RobotVersion(2, 9, 1)));
    }


    @Test(expected = IllegalStateException.class)
    public void parsingProblem_1() {
        RobotVersion.from("1");
    }


    @Test(expected = IllegalStateException.class)
    public void parsingProblem_2() {
        RobotVersion.from("version");
    }


    @Test
    public void lessTest() {
        assertThat(RobotVersion.from("1.2")).isLessThan(
                RobotVersion.from("2.0"));
        assertThat(RobotVersion.from("1.2")).isLessThan(
                RobotVersion.from("1.3"));
        assertThat(RobotVersion.from("1.2")).isLessThan(
                RobotVersion.from("1.2.3"));
        assertThat(RobotVersion.from("1.2")).isLessThan(
                RobotVersion.from("1.2.1"));
        assertThat(RobotVersion.from("1.2")).isLessThan(
                RobotVersion.from("1.2.0"));
    }


    @Test
    public void lessOrEqualTest() {
        assertThat(RobotVersion.from("1.2")).isLessThanOrEqualTo(
                RobotVersion.from("1.2"));
        assertThat(RobotVersion.from("1.2")).isLessThanOrEqualTo(
                RobotVersion.from("2.0"));
        assertThat(RobotVersion.from("1.2")).isLessThanOrEqualTo(
                RobotVersion.from("1.3"));
        assertThat(RobotVersion.from("1.2")).isLessThanOrEqualTo(
                RobotVersion.from("1.2.3"));
        assertThat(RobotVersion.from("1.2")).isLessThanOrEqualTo(
                RobotVersion.from("1.2.1"));
        assertThat(RobotVersion.from("1.2")).isLessThanOrEqualTo(
                RobotVersion.from("1.2.0"));
    }


    @Test
    public void greaterTest() {
        assertThat(RobotVersion.from("1.2")).isGreaterThan(
                RobotVersion.from("1.0"));
        assertThat(RobotVersion.from("1.2")).isGreaterThan(
                RobotVersion.from("0.1.1"));
        assertThat(RobotVersion.from("1.2")).isGreaterThan(
                RobotVersion.from("0.1"));
        assertThat(RobotVersion.from("1.2")).isGreaterThan(
                RobotVersion.from("1.1"));
        assertThat(RobotVersion.from("1.2")).isGreaterThan(
                RobotVersion.from("1.1.9"));
    }


    @Test
    public void greaterOrEqualTest() {
        assertThat(RobotVersion.from("1.2")).isGreaterThanOrEqualTo(
                RobotVersion.from("1.0"));
        assertThat(RobotVersion.from("1.2")).isGreaterThanOrEqualTo(
                RobotVersion.from("1.2"));
        assertThat(RobotVersion.from("1.2")).isGreaterThanOrEqualTo(
                RobotVersion.from("0.1.1"));
        assertThat(RobotVersion.from("1.2")).isGreaterThanOrEqualTo(
                RobotVersion.from("0.1"));
        assertThat(RobotVersion.from("1.2")).isGreaterThanOrEqualTo(
                RobotVersion.from("1.1"));
        assertThat(RobotVersion.from("1.2")).isGreaterThanOrEqualTo(
                RobotVersion.from("1.1.9"));
    }


    @Test
    public void olderThanTest_whenVersionHasNoPatch() {
        assertThat(
                RobotVersion.from("1.2").isOlderThan(RobotVersion.from("2.0")))
                .isTrue();
        assertThat(
                RobotVersion.from("1.2").isOlderThan(RobotVersion.from("1.3")))
                .isTrue();
        assertThat(
                RobotVersion.from("1.2")
                        .isOlderThan(RobotVersion.from("1.2.3"))).isTrue();
        assertThat(
                RobotVersion.from("1.2")
                        .isOlderThan(RobotVersion.from("1.2.1"))).isTrue();
        assertThat(
                RobotVersion.from("1.2")
                        .isOlderThan(RobotVersion.from("1.2.0"))).isTrue();

        assertThat(
                RobotVersion.from("1.2").isOlderThan(RobotVersion.from("1.2")))
                .isFalse();
        assertThat(
                RobotVersion.from("1.2").isOlderThan(RobotVersion.from("1.1")))
                .isFalse();
        assertThat(
                RobotVersion.from("1.2")
                        .isOlderThan(RobotVersion.from("1.1.9"))).isFalse();
        assertThat(
                RobotVersion.from("1.2").isOlderThan(RobotVersion.from("0.7")))
                .isFalse();
        assertThat(
                RobotVersion.from("1.2")
                        .isOlderThan(RobotVersion.from("0.7.9"))).isFalse();
    }


    @Test
    public void olderThanTest_whenVersionHasPatch() {
        assertThat(
                RobotVersion.from("1.2.3")
                        .isOlderThan(RobotVersion.from("2.0"))).isTrue();
        assertThat(
                RobotVersion.from("1.2.3").isOlderThan(
                        RobotVersion.from("1.2.4"))).isTrue();
        assertThat(
                RobotVersion.from("1.2.3")
                        .isOlderThan(RobotVersion.from("1.3"))).isTrue();
        assertThat(
                RobotVersion.from("1.2.3").isOlderThan(
                        RobotVersion.from("1.3.1"))).isTrue();
        assertThat(
                RobotVersion.from("1.2.3").isOlderThan(
                        RobotVersion.from("2.1.0"))).isTrue();

        assertThat(
                RobotVersion.from("1.2.3").isOlderThan(
                        RobotVersion.from("1.2.3"))).isFalse();
        assertThat(
                RobotVersion.from("1.2.3")
                        .isOlderThan(RobotVersion.from("1.1"))).isFalse();
        assertThat(
                RobotVersion.from("1.2.3").isOlderThan(
                        RobotVersion.from("1.1.9"))).isFalse();
        assertThat(
                RobotVersion.from("1.2.3")
                        .isOlderThan(RobotVersion.from("0.7"))).isFalse();
        assertThat(
                RobotVersion.from("1.2.3").isOlderThan(
                        RobotVersion.from("0.7.9"))).isFalse();
    }


    @Test
    public void olderThanOrEqualTest_whenVersionHasNoPatch() {
        assertThat(
                RobotVersion.from("1.2").isOlderThanOrEqualTo(
                        RobotVersion.from("2.0"))).isTrue();
        assertThat(
                RobotVersion.from("1.2").isOlderThanOrEqualTo(
                        RobotVersion.from("1.3"))).isTrue();
        assertThat(
                RobotVersion.from("1.2").isOlderThanOrEqualTo(
                        RobotVersion.from("1.2.3"))).isTrue();
        assertThat(
                RobotVersion.from("1.2").isOlderThanOrEqualTo(
                        RobotVersion.from("1.2.1"))).isTrue();
        assertThat(
                RobotVersion.from("1.2").isOlderThanOrEqualTo(
                        RobotVersion.from("1.2.0"))).isTrue();
        assertThat(
                RobotVersion.from("1.2").isOlderThanOrEqualTo(
                        RobotVersion.from("1.2"))).isTrue();

        assertThat(
                RobotVersion.from("1.2").isOlderThanOrEqualTo(
                        RobotVersion.from("1.1"))).isFalse();
        assertThat(
                RobotVersion.from("1.2").isOlderThanOrEqualTo(
                        RobotVersion.from("1.1.9"))).isFalse();
        assertThat(
                RobotVersion.from("1.2").isOlderThanOrEqualTo(
                        RobotVersion.from("0.7"))).isFalse();
        assertThat(
                RobotVersion.from("1.2").isOlderThanOrEqualTo(
                        RobotVersion.from("0.7.9"))).isFalse();
    }


    @Test
    public void olderThanOrEqualTest_whenVersionHasPatch() {
        assertThat(
                RobotVersion.from("1.2.3").isOlderThanOrEqualTo(
                        RobotVersion.from("2.0"))).isTrue();
        assertThat(
                RobotVersion.from("1.2.3").isOlderThanOrEqualTo(
                        RobotVersion.from("1.2.4"))).isTrue();
        assertThat(
                RobotVersion.from("1.2.3").isOlderThanOrEqualTo(
                        RobotVersion.from("1.3"))).isTrue();
        assertThat(
                RobotVersion.from("1.2.3").isOlderThanOrEqualTo(
                        RobotVersion.from("1.3.1"))).isTrue();
        assertThat(
                RobotVersion.from("1.2.3").isOlderThanOrEqualTo(
                        RobotVersion.from("2.1.0"))).isTrue();
        assertThat(
                RobotVersion.from("1.2.3").isOlderThanOrEqualTo(
                        RobotVersion.from("1.2.3"))).isTrue();

        assertThat(
                RobotVersion.from("1.2.3").isOlderThanOrEqualTo(
                        RobotVersion.from("1.1"))).isFalse();
        assertThat(
                RobotVersion.from("1.2.3").isOlderThanOrEqualTo(
                        RobotVersion.from("1.1.9"))).isFalse();
        assertThat(
                RobotVersion.from("1.2.3").isOlderThanOrEqualTo(
                        RobotVersion.from("0.7"))).isFalse();
        assertThat(
                RobotVersion.from("1.2.3").isOlderThanOrEqualTo(
                        RobotVersion.from("0.7.9"))).isFalse();
    }


    @Test
    public void newerThanTest_whenVersionHasNoPatch() {
        assertThat(
                RobotVersion.from("1.2").isNewerThan(RobotVersion.from("1.0")))
                .isTrue();
        assertThat(
                RobotVersion.from("1.2")
                        .isNewerThan(RobotVersion.from("0.1.1"))).isTrue();
        assertThat(
                RobotVersion.from("1.2").isNewerThan(RobotVersion.from("0.1")))
                .isTrue();
        assertThat(
                RobotVersion.from("1.2").isNewerThan(RobotVersion.from("1.1")))
                .isTrue();
        assertThat(
                RobotVersion.from("1.2")
                        .isNewerThan(RobotVersion.from("1.1.9"))).isTrue();

        assertThat(
                RobotVersion.from("1.2").isNewerThan(RobotVersion.from("1.2")))
                .isFalse();
        assertThat(
                RobotVersion.from("1.2")
                        .isNewerThan(RobotVersion.from("1.2.0"))).isFalse();
        assertThat(
                RobotVersion.from("1.2")
                        .isNewerThan(RobotVersion.from("1.2.7"))).isFalse();
        assertThat(
                RobotVersion.from("1.2").isNewerThan(RobotVersion.from("1.3")))
                .isFalse();
        assertThat(
                RobotVersion.from("1.2")
                        .isNewerThan(RobotVersion.from("2.0.1"))).isFalse();
    }


    @Test
    public void newerThanTest_whenVersionHasPatch() {
        assertThat(
                RobotVersion.from("1.2.3")
                        .isNewerThan(RobotVersion.from("1.0"))).isTrue();
        assertThat(
                RobotVersion.from("1.2.3").isNewerThan(
                        RobotVersion.from("1.2.1"))).isTrue();
        assertThat(
                RobotVersion.from("1.2.3")
                        .isNewerThan(RobotVersion.from("0.1"))).isTrue();
        assertThat(
                RobotVersion.from("1.2.3")
                        .isNewerThan(RobotVersion.from("1.1"))).isTrue();
        assertThat(
                RobotVersion.from("1.2.3").isNewerThan(
                        RobotVersion.from("1.1.9"))).isTrue();

        assertThat(
                RobotVersion.from("1.2.3").isNewerThan(
                        RobotVersion.from("1.2.3"))).isFalse();
        assertThat(
                RobotVersion.from("1.2.3").isNewerThan(
                        RobotVersion.from("1.2.6"))).isFalse();
        assertThat(
                RobotVersion.from("1.2.3").isNewerThan(
                        RobotVersion.from("1.3.7"))).isFalse();
        assertThat(
                RobotVersion.from("1.2.3")
                        .isNewerThan(RobotVersion.from("1.3"))).isFalse();
        assertThat(
                RobotVersion.from("1.2.3").isNewerThan(
                        RobotVersion.from("2.0.1"))).isFalse();
    }


    @Test
    public void newerThanOrEqualTest_whenVersionHasNoPatch() {
        assertThat(
                RobotVersion.from("1.2").isNewerOrEqualTo(
                        RobotVersion.from("1.0"))).isTrue();
        assertThat(
                RobotVersion.from("1.2").isNewerOrEqualTo(
                        RobotVersion.from("0.1.1"))).isTrue();
        assertThat(
                RobotVersion.from("1.2").isNewerOrEqualTo(
                        RobotVersion.from("0.1"))).isTrue();
        assertThat(
                RobotVersion.from("1.2").isNewerOrEqualTo(
                        RobotVersion.from("1.1"))).isTrue();
        assertThat(
                RobotVersion.from("1.2").isNewerOrEqualTo(
                        RobotVersion.from("1.1.9"))).isTrue();
        assertThat(
                RobotVersion.from("1.2").isNewerOrEqualTo(
                        RobotVersion.from("1.2"))).isTrue();

        assertThat(
                RobotVersion.from("1.2").isNewerOrEqualTo(
                        RobotVersion.from("1.2.0"))).isFalse();
        assertThat(
                RobotVersion.from("1.2").isNewerOrEqualTo(
                        RobotVersion.from("1.2.7"))).isFalse();
        assertThat(
                RobotVersion.from("1.2").isNewerOrEqualTo(
                        RobotVersion.from("1.3"))).isFalse();
        assertThat(
                RobotVersion.from("1.2").isNewerOrEqualTo(
                        RobotVersion.from("2.0.1"))).isFalse();
    }


    @Test
    public void newerThanOrEqualTest_whenVersionHasPatch() {
        assertThat(
                RobotVersion.from("1.2.3").isNewerOrEqualTo(
                        RobotVersion.from("1.0"))).isTrue();
        assertThat(
                RobotVersion.from("1.2.3").isNewerOrEqualTo(
                        RobotVersion.from("1.2.1"))).isTrue();
        assertThat(
                RobotVersion.from("1.2.3").isNewerOrEqualTo(
                        RobotVersion.from("0.1"))).isTrue();
        assertThat(
                RobotVersion.from("1.2.3").isNewerOrEqualTo(
                        RobotVersion.from("1.1"))).isTrue();
        assertThat(
                RobotVersion.from("1.2.3").isNewerOrEqualTo(
                        RobotVersion.from("1.1.9"))).isTrue();
        assertThat(
                RobotVersion.from("1.2.3").isNewerOrEqualTo(
                        RobotVersion.from("1.2.3"))).isTrue();

        assertThat(
                RobotVersion.from("1.2.3").isNewerOrEqualTo(
                        RobotVersion.from("1.2.6"))).isFalse();
        assertThat(
                RobotVersion.from("1.2.3").isNewerOrEqualTo(
                        RobotVersion.from("1.3.7"))).isFalse();
        assertThat(
                RobotVersion.from("1.2.3").isNewerOrEqualTo(
                        RobotVersion.from("1.3"))).isFalse();
        assertThat(
                RobotVersion.from("1.2.3").isNewerOrEqualTo(
                        RobotVersion.from("2.0.1"))).isFalse();
    }


    @Test
    public void isEqualToTest() {
        assertThat(RobotVersion.from("1.2").isEqualTo(RobotVersion.from("1.2")))
                .isTrue();
        assertThat(
                RobotVersion.from("1.2.3")
                        .isEqualTo(RobotVersion.from("1.2.3"))).isTrue();

        assertThat(
                RobotVersion.from("1.2").isEqualTo(RobotVersion.from("1.2.3")))
                .isFalse();
        assertThat(
                RobotVersion.from("1.2.3").isEqualTo(RobotVersion.from("1.2")))
                .isFalse();
    }


    @Test
    public void isNotEqualToTest() {
        assertThat(
                RobotVersion.from("1.2").isNotEqualTo(RobotVersion.from("1.2")))
                .isFalse();
        assertThat(
                RobotVersion.from("1.2.3").isNotEqualTo(
                        RobotVersion.from("1.2.3"))).isFalse();

        assertThat(
                RobotVersion.from("1.2").isNotEqualTo(
                        RobotVersion.from("1.2.3"))).isTrue();
        assertThat(
                RobotVersion.from("1.2.3").isNotEqualTo(
                        RobotVersion.from("1.2"))).isTrue();
    }


    @Test
    public void equalityTest() {
        assertThat(RobotVersion.from("1.2").equals(new Object())).isFalse();
        assertThat(RobotVersion.from("1.2").equals(RobotVersion.from("1.2.0")))
                .isFalse();
        assertThat(RobotVersion.from("1.2").equals(RobotVersion.from("1.3")))
                .isFalse();
        assertThat(RobotVersion.from("1.2.3").equals(RobotVersion.from("1.2")))
                .isFalse();

        assertThat(RobotVersion.from("1.2").equals(RobotVersion.from("1.2")))
                .isTrue();
        assertThat(
                RobotVersion.from("1.2.3").equals(RobotVersion.from("1.2.3")))
                .isTrue();
    }


    @Test
    public void hashCodeTest() {
        assertThat(RobotVersion.from("1.2").hashCode()).isEqualTo(
                RobotVersion.from("1.2").hashCode());
        assertThat(RobotVersion.from("1.2.3").hashCode()).isEqualTo(
                RobotVersion.from("1.2.3").hashCode());
    }
}

/*
* Copyright 2018 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.libraries;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;

public class SitePackagesLibrariesTest {

    @Test
    public void sitePackagesLibrariesObjectIsCreatedWithEmptyRobotAndNonRobotList() throws Exception {
        final SitePackagesLibraries sitePackagesLibraries = new SitePackagesLibraries();

        assertThat(sitePackagesLibraries.getRobotLibs()).isEmpty();
        assertThat(sitePackagesLibraries.getNonRobotLibs()).isEmpty();
    }

    @Test
    public void sitePackagesLibrariesObjectIsCreatedWithRobotAndNonRobotList() throws Exception {
        final SitePackagesLibraries sitePackagesLibraries = new SitePackagesLibraries(Arrays.asList("libOne"),
                Arrays.asList("libTwo", "libThree"));

        assertThat(sitePackagesLibraries.getRobotLibs()).containsExactly("libOne");
        assertThat(sitePackagesLibraries.getNonRobotLibs()).containsExactly("libTwo", "libThree");
    }
}

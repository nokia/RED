package org.rf.ide.core.libraries;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class SitePackagesLibrariesTest {

    @Test
    public void sitePackagesLibrariesObjectIsCreatedWithEmptyRobotAndNonRobotList() throws Exception {
        final SitePackagesLibraries sitePackagesLibararies = new SitePackagesLibraries();

        assertThat(sitePackagesLibararies.getRobotLibs()).isEmpty();
        assertThat(sitePackagesLibararies.getNonRobotLibs()).isEmpty();
    }

    @Test
    public void sitePackagesLibrariesObjectIsCreatedWithRobotAndNonRobotList() throws Exception {
        final List<List<String>> libs = Arrays.asList(Arrays.asList("libOne"), Arrays.asList("libTwo", "libThree"));
        final SitePackagesLibraries sitePackagesLibararies = new SitePackagesLibraries(libs);

        assertThat(sitePackagesLibararies.getRobotLibs()).containsExactly("libOne");
        assertThat(sitePackagesLibararies.getNonRobotLibs()).containsExactly("libTwo", "libThree");
    }
}

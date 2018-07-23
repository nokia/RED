package org.rf.ide.core.libraries;

import java.util.ArrayList;
import java.util.List;

public class SitePackagesLibraries {

    private final List<String> robotLibs;

    private final List<String> nonRobotLibs;

    public SitePackagesLibraries() {
        robotLibs = new ArrayList<>();
        nonRobotLibs = new ArrayList<>();
    }

    public SitePackagesLibraries(final List<List<String>> libs) {
        robotLibs = libs.get(0);
        nonRobotLibs = libs.get(1);
    }

    public List<String> getRobotLibs() {
        return robotLibs;
    }

    public List<String> getNonRobotLibs() {
        return nonRobotLibs;
    }
}

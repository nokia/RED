/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;

import com.google.common.base.Optional;

public class ValidationContext {

    private final RobotVersion version;


    public ValidationContext(final RobotRuntimeEnvironment runtimeEnvironment) {
        this.version = RobotVersion.from(runtimeEnvironment.getVersion());
    }

    public RobotVersion getVersion() {
        return version;
    }

    public static class RobotVersion implements Comparable<RobotVersion> {

        private final int major;

        private final int minor;

        private final Optional<Integer> patch;

        public static RobotVersion from(final String version) {
            final Matcher matcher = Pattern.compile("(\\d)\\.(\\d+)(\\.(\\d+))?").matcher(version);
            if (matcher.find()) {
                if (matcher.groupCount() == 4) {
                    if (matcher.group(4) == null) {
                        return new RobotVersion(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
                    } else {
                        return new RobotVersion(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)),
                                Integer.parseInt(matcher.group(4)));
                    }
                }
            }
            throw new IllegalStateException("Unable to recognize Robot Framework version number");
        }

        public RobotVersion(final int major, final int minor) {
            this.major = major;
            this.minor = minor;
            this.patch = Optional.absent();
        }

        public RobotVersion(final int major, final int minor, final int patch) {
            this.major = major;
            this.minor = minor;
            this.patch = Optional.of(patch);
        }

        public boolean isEqualTo(final RobotVersion otherVersion) {
            return major == otherVersion.major && minor == otherVersion.minor && patch.equals(otherVersion.patch);
        }

        public boolean isNotEqualTo(final RobotVersion otherVersion) {
            return !isEqualTo(otherVersion);
        }

        public boolean isOlderThan(final RobotVersion otherVersion) {
            return major < otherVersion.major || (major == otherVersion.major && minor < otherVersion.minor)
                    || (major == otherVersion.major && minor == otherVersion.minor
                            && isLessPatch(patch, otherVersion.patch));
        }

        public boolean isOlderThanOrEqualTo(final RobotVersion otherVersion) {
            return isOlderThan(otherVersion) || isEqualTo(otherVersion);
        }

        public boolean isNewerThan(final RobotVersion otherVersion) {
            return major > otherVersion.major || (major == otherVersion.major && minor > otherVersion.minor)
                    || (major == otherVersion.major && minor == otherVersion.minor
                            && isLessPatch(otherVersion.patch, patch));
        }

        public boolean isNewerOrEqualTo(final RobotVersion otherVersion) {
            return isNewerThan(otherVersion) || isEqualTo(otherVersion);
        }

        private boolean isLessPatch(final Optional<Integer> patch1, final Optional<Integer> patch2) {
            if (!patch1.isPresent() && !patch2.isPresent()) {
                return false;
            } else if (patch1.isPresent() && !patch2.isPresent()) {
                return false;
            } else if (!patch1.isPresent() && patch2.isPresent()) {
                return true;
            } else {
                final int p1 = patch1.get();
                final int p2 = patch2.get();
                return p1 < p2;
            }
        }

        @Override
        public int compareTo(final RobotVersion otherVersion) {
            if (isEqualTo(otherVersion)) {
                return 0;
            } else if (isOlderThan(otherVersion)) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}

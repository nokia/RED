/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PythonVersion {

    private final int major;

    private final int minor;

    private final int micro;

    /**
     * Creates python version from robot version string. Works also for other interpreters like
     * ironpython/pypy/jython.
     *
     * @param version
     *            robot.version.get_full_version() result containing interpreter version number
     *            taken from sys.version
     */
    public static PythonVersion from(final String version) {
        final Matcher matcher = Pattern.compile("\\(.+(\\d)\\.(\\d+)\\.(\\d+) on .+\\)").matcher(version);
        if (matcher.find()) {
            return new PythonVersion(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3)));
        }
        throw new IllegalStateException("Unable to recognize Python version number");
    }

    public PythonVersion(final int major, final int minor, final int micro) {
        this.major = major;
        this.minor = minor;
        this.micro = micro;
    }

    public boolean isDeprecated() {
        return major == 2 && minor < 7 || major == 3 && minor < 4;
    }

    public String asString() {
        return major + "." + minor + "." + micro;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, micro);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof PythonVersion) {
            final PythonVersion that = (PythonVersion) obj;
            return this.major == that.major && this.minor == that.minor && this.micro == that.micro;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format(getClass().getName() + "[major=%s, minor=%s, micro=%s]", this.major, this.minor,
                this.micro);
    }

}

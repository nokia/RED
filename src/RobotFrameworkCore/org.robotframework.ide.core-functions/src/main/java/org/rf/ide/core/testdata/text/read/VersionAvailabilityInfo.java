/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read;

import org.rf.ide.core.testdata.model.RobotVersion;


public class VersionAvailabilityInfo {

    private final String representation;
    private final RobotVersion availableFrom;
    private final RobotVersion depracatedFrom;
    private final RobotVersion removedFrom;


    private VersionAvailabilityInfo(final String representation,
            final RobotVersion availableFrom,
            final RobotVersion depracatedFrom, final RobotVersion removedFrom) {
        this.representation = representation;
        this.availableFrom = availableFrom;
        this.depracatedFrom = depracatedFrom;
        this.removedFrom = removedFrom;
    }


    public String getRepresentation() {
        return representation;
    }


    public RobotVersion getAvailableFrom() {
        return availableFrom;
    }


    public RobotVersion getDepracatedFrom() {
        return depracatedFrom;
    }


    public RobotVersion getRemovedFrom() {
        return removedFrom;
    }

    public static class VersionAvailabilityInfoBuilder {

        private String representation;
        private RobotVersion availableFrom;
        private RobotVersion deprecatedFrom;
        private RobotVersion removedFrom;


        private VersionAvailabilityInfoBuilder() {
        }


        public static VersionAvailabilityInfoBuilder create() {
            return new VersionAvailabilityInfoBuilder();
        }


        public VersionAvailabilityInfoBuilder addRepresentation(
                final String representation) {
            this.representation = representation;
            return this;
        }


        public VersionAvailabilityInfoBuilder availableFrom(
                final RobotVersion availableFrom) {
            this.availableFrom = availableFrom;
            return this;
        }


        public VersionAvailabilityInfoBuilder availableFrom(
                final String availableFrom) {
            this.availableFrom = RobotVersion.from(availableFrom);
            return this;
        }


        public VersionAvailabilityInfoBuilder deprecatedFrom(
                final RobotVersion deprecatedFrom) {
            this.deprecatedFrom = deprecatedFrom;
            return this;
        }


        public VersionAvailabilityInfoBuilder deprecatedFrom(
                final String deprecatedFrom) {
            this.deprecatedFrom = RobotVersion.from(deprecatedFrom);
            return this;
        }


        public VersionAvailabilityInfoBuilder removedFrom(
                final RobotVersion removedFrom) {
            this.removedFrom = removedFrom;
            return this;
        }


        public VersionAvailabilityInfoBuilder removedFrom(
                final String removedFrom) {
            this.removedFrom = RobotVersion.from(removedFrom);
            return this;
        }


        public VersionAvailabilityInfo build() {
            RobotVersion available = availableFrom;
            if (available == null) {
                available = new RobotVersion(0, 0);
            }
            RobotVersion deprecated = deprecatedFrom;
            if (deprecated == null) {
                deprecated = new RobotVersion(Integer.MAX_VALUE,
                        Integer.MAX_VALUE);
            }
            RobotVersion removed = removedFrom;
            if (removed == null) {
                removed = new RobotVersion(Integer.MAX_VALUE, Integer.MAX_VALUE);
            }

            return new VersionAvailabilityInfo(this.representation, available,
                    deprecated, removed);
        }
    }
}
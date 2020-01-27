/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl.old;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

enum ContainerElementType {
    TEXT(ContainerSettings.newInstance().setCanBeMerged(true)),
    WHITESPACE(ContainerSettings.newInstance().setCanBeMerged(true), ' ', '\t'),
    ESCAPE(ContainerSettings.newInstance().setCanBeMerged(true), '\\'),
    VARIABLE_TYPE_ID(ContainerSettings.newInstance(), '$', '@', '&', '%'),
    CURLY_BRACKET_OPEN(ContainerSettings.newInstance().setNewContainerOpener(true), '{'),
    CURLY_BRACKET_CLOSE(ContainerSettings.newInstance().setCloseForType(CURLY_BRACKET_OPEN), '}'),
    SQUARE_BRACKET_OPEN(ContainerSettings.newInstance().setNewContainerOpener(true), '['),
    SQUARE_BRACKET_CLOSE(ContainerSettings.newInstance().setCloseForType(SQUARE_BRACKET_OPEN), ']');

    private static final Map<Character, ContainerElementType> MAPPING = new HashMap<>();

    private static final Map<ContainerElementType, ContainerElementType> OPEN_TYPES_TO_CLOSE_TYPES = new HashMap<>();

    private final List<Character> representation = new ArrayList<>();

    private final ContainerSettings settings;

    private ContainerElementType(final ContainerSettings settings, final char... characters) {
        for (final char c : characters) {
            representation.add(c);
        }
        this.settings = settings;
    }

    List<Character> getRepresentation() {
        return representation;
    }

    boolean canBeMerged() {
        return settings.canBeMerged();
    }

    boolean shouldOpenNewContainer() {
        return settings.isNewContainerOpener();
    }

    ContainerElementType getCloseContainer() {
        return settings.getCloseForType();
    }

    static ContainerElementType getCloseContainerType(final ContainerElementType containerElementType) {
        synchronized (OPEN_TYPES_TO_CLOSE_TYPES) {
            if (OPEN_TYPES_TO_CLOSE_TYPES.isEmpty()) {
                for (final ContainerElementType cet : values()) {
                    final ContainerElementType openType = cet.getCloseContainer();
                    if (openType != null) {
                        OPEN_TYPES_TO_CLOSE_TYPES.put(openType, cet);
                    }
                }
            }
        }

        return OPEN_TYPES_TO_CLOSE_TYPES.get(containerElementType);
    }

    static ContainerElementType getTypeFor(final char c) {
        synchronized (MAPPING) {
            if (MAPPING.isEmpty()) {
                initMappingCharToType();
            }
        }

        ContainerElementType type = MAPPING.get(c);
        if (type == null) {
            type = ContainerElementType.TEXT;
        }

        return type;
    }

    private static void initMappingCharToType() {
        for (final ContainerElementType cet : values()) {
            for (final char representationChar : cet.getRepresentation()) {
                final ContainerElementType checkType = MAPPING.get(representationChar);
                if (checkType == null) {
                    MAPPING.put(representationChar, cet);
                } else {
                    throw new IllegalStateException(
                            "Char \'" + representationChar + "\' is already defined for " + checkType);
                }
            }
        }
    }

    static class ContainerSettings {

        private boolean isNewContainerOpener;

        private boolean canBeMerged;

        private ContainerElementType closeForType;

        private ContainerSettings(final boolean isNewContainerOpener, final boolean canBeMerged,
                final ContainerElementType closeForType) {
            this.isNewContainerOpener = isNewContainerOpener;
            this.canBeMerged = canBeMerged;
            this.closeForType = closeForType;
        }

        static ContainerSettings newInstance() {
            return new ContainerSettings(false, false, null);
        }

        boolean isNewContainerOpener() {
            return isNewContainerOpener;
        }

        ContainerSettings setNewContainerOpener(final boolean isNewContainerOpener) {
            this.isNewContainerOpener = isNewContainerOpener;

            return this;
        }

        boolean canBeMerged() {
            return canBeMerged;
        }

        ContainerSettings setCanBeMerged(final boolean canBeMerged) {
            this.canBeMerged = canBeMerged;

            return this;
        }

        ContainerElementType getCloseForType() {
            return closeForType;
        }

        ContainerSettings setCloseForType(final ContainerElementType closeForType) {
            this.closeForType = closeForType;

            return this;
        }
    }
}

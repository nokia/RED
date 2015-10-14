/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.ast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public enum ContainerElementType {
    TEXT(ContainerSettings.newInstance().setCanBeMerged(true)), WHITESPACE(
            ContainerSettings.newInstance().setCanBeMerged(true), ' ', '\t'), ESCAPE(
            ContainerSettings.newInstance().setCanBeMerged(true), '\\'), VARIABLE_TYPE_ID(
            ContainerSettings.newInstance(), '$', '@', '&', '%'), CURRLY_BRACKET_OPEN(
            ContainerSettings.newInstance().setNewContainerOpener(true), '{'), CURRLY_BRACKET_CLOSE(
            ContainerSettings.newInstance()
                    .setCloseForType(CURRLY_BRACKET_OPEN), '}'), SQUARE_BRACKET_OPEN(
            ContainerSettings.newInstance().setNewContainerOpener(true), '['), SQUARE_BRACKET_CLOSE(
            ContainerSettings.newInstance()
                    .setCloseForType(SQUARE_BRACKET_OPEN), ']');

    private static final Map<Character, ContainerElementType> mapping = new HashMap<>();
    private static final Map<ContainerElementType, ContainerElementType> openTypesToCloseTypes = new HashMap<>();

    private final List<Character> representation = new LinkedList<>();
    private final ContainerSettings settings;


    private ContainerElementType(final ContainerSettings settings,
            final char... cs) {
        for (char c : cs) {
            representation.add(c);
        }
        this.settings = settings;
    }


    public List<Character> getRepresentation() {
        return representation;
    }


    public boolean canBeMerged() {
        return settings.canBeMerged();
    }


    public boolean shouldOpenNewContainer() {
        return settings.isNewContainerOpener();
    }


    public ContainerElementType getCloseContainer() {
        return settings.getCloseForType();
    }


    public static ContainerElementType getCloseContainerType(
            final ContainerElementType containerElementType) {
        synchronized (openTypesToCloseTypes) {
            if (openTypesToCloseTypes.isEmpty()) {
                for (ContainerElementType cet : values()) {
                    ContainerElementType openType = cet.getCloseContainer();
                    if (openType != null) {
                        openTypesToCloseTypes.put(openType, cet);
                    }
                }
            }
        }

        return openTypesToCloseTypes.get(containerElementType);
    }


    public static ContainerElementType getTypeFor(char c) {
        synchronized (mapping) {
            if (mapping.isEmpty()) {
                initMappingCharToType();
            }
        }

        ContainerElementType type = mapping.get(c);
        if (type == null) {
            type = ContainerElementType.TEXT;
        }

        return type;
    }


    private static void initMappingCharToType() {
        for (ContainerElementType cet : values()) {
            for (char representationChar : cet.getRepresentation()) {
                ContainerElementType checkType = mapping
                        .get(representationChar);
                if (checkType == null) {
                    mapping.put(representationChar, cet);
                } else {
                    throw new IllegalStateException("Char \'"
                            + representationChar + "\' is already defined for "
                            + checkType);
                }
            }
        }
    }

    public static class ContainerSettings {

        private boolean isNewContainerOpener;
        private boolean canBeMerged;
        private ContainerElementType closeForType;


        private ContainerSettings(final boolean isNewContainerOpener,
                final boolean canBeMerged,
                final ContainerElementType closeForType) {
            this.isNewContainerOpener = isNewContainerOpener;
            this.canBeMerged = canBeMerged;
            this.closeForType = closeForType;
        }


        public static ContainerSettings newInstance() {
            return new ContainerSettings(false, false, null);
        }


        public boolean isNewContainerOpener() {
            return isNewContainerOpener;
        }


        public ContainerSettings setNewContainerOpener(
                boolean isNewContainerOpener) {
            this.isNewContainerOpener = isNewContainerOpener;

            return this;
        }


        public boolean canBeMerged() {
            return canBeMerged;
        }


        public ContainerSettings setCanBeMerged(boolean canBeMerged) {
            this.canBeMerged = canBeMerged;

            return this;
        }


        public ContainerElementType getCloseForType() {
            return closeForType;
        }


        public ContainerSettings setCloseForType(
                ContainerElementType closeForType) {
            this.closeForType = closeForType;

            return this;
        }
    }
}

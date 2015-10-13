/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class VariableStructureExtractor {

    public Container buildStructureTree(final String text) {
        Container mainContainer = new Container(null);

        if (text != null) {
            Container currentContainer = mainContainer;

            char[] textChars = text.toCharArray();
            int textLength = textChars.length;
            for (int charIndex = 0; charIndex < textLength; charIndex++) {
                char currentChar = textChars[charIndex];
                ContainerElementType type = ContainerElementType
                        .getTypeFor(currentChar);
                ContainerElement element = new ContainerElement(
                        new TextPosition(text, charIndex, charIndex), type);
                if (type.shouldOpenNewContainer()) {
                    Container newContainer = new Container(currentContainer);
                    currentContainer.addElement(newContainer);
                    currentContainer = newContainer;
                    currentContainer.addElement(element);
                } else if (shouldCloseContainer(type)) {
                    Container matchingContainer = findNearestContainerToClose(
                            type, currentContainer);
                    if (matchingContainer == null) {
                        currentContainer.addElement(element);
                    } else {
                        matchingContainer.addElement(element);
                        closeContainer(matchingContainer);
                        currentContainer = matchingContainer.getParent();
                    }
                } else {
                    boolean wasMerged = false;
                    List<IContainerElement> elements = currentContainer
                            .getElements();
                    if (!elements.isEmpty()) {
                        IContainerElement lastElement = elements.get(elements
                                .size() - 1);
                        ContainerElementType lastType = lastElement.getType();
                        if (lastType != null && lastType.canBeMerged()
                                && type == lastType) {
                            if (lastElement instanceof ContainerElement) {
                                ContainerElement contElement = (ContainerElement) lastElement;
                                contElement.increaseEndPosition();
                                wasMerged = true;
                            }
                        }
                    }

                    if (!wasMerged) {
                        currentContainer.addElement(element);
                    }
                }
            }
        }

        return mainContainer;
    }


    private void closeContainer(final Container container) {
        if (container != null) {
            for (IContainerElement contElem : container.getElements()) {
                if (contElem.isComplex()) {
                    Container cont = (Container) contElem;
                    if (cont.isOpenForModification()) {
                        closeContainer(cont);
                    }
                }
            }
            container.closeForModification();
        }
    }


    private Container findNearestContainerToClose(
            final ContainerElementType type, final Container currentContainer) {
        Container cont = null;

        if (currentContainer != null) {
            if (currentContainer.isOpenForModification()) {
                for (IContainerElement contElement : currentContainer
                        .getElements()) {
                    if (contElement.isComplex()) {
                        cont = findNearestContainerToClose(type,
                                (Container) contElement);
                        if (cont != null) {
                            break;
                        }
                    }
                }

                if (cont == null) {
                    if (type == ContainerElementType
                            .getCloseContainerType(currentContainer
                                    .getElements().get(0).getType())) {
                        cont = currentContainer;
                    }
                }
            }
        }

        return cont;
    }


    private boolean shouldCloseContainer(ContainerElementType type) {
        return type.getCloseContainer() != null;
    }

    public static class Container implements IContainerElement {

        private Container parent;
        private List<IContainerElement> elements = new LinkedList<>();
        private boolean isOpenForModification = true;


        public Container(final Container parent) {
            this.parent = parent;
        }


        public Container getParent() {
            return parent;
        }


        public void addElement(final IContainerElement element) {
            if (isOpenForModification()) {
                elements.add(element);
            } else {
                throw new UnsupportedOperationException(
                        "Container is closed for modification!");
            }
        }


        public void closeForModification() {
            this.isOpenForModification = false;
        }


        public boolean isOpenForModification() {
            return isOpenForModification;
        }


        public List<IContainerElement> getElements() {
            return Collections.unmodifiableList(elements);
        }


        @Override
        public boolean isComplex() {
            return true;
        }


        @Override
        public ContainerElementType getType() {
            return null;
        }


        @Override
        public String toString() {
            return String
                    .format("Container [hasParent=%s, elements=%s, isOpenForModification=%s]",
                            (parent != null), elements, isOpenForModification);
        }


        @Override
        public String prettyPrint(int deepLevel) {
            StringBuilder text = new StringBuilder();
            text.append(formatWithSpaces(deepLevel, String.format(
                    "Container [hasParent=%s, isOpenForModification=%s",
                    (parent != null), isOpenForModification)));
            int childDeepLevel = deepLevel + 1;
            for (IContainerElement elem : elements) {
                text.append("\n");
                text.append(elem.prettyPrint(childDeepLevel));
                text.append(",");
            }
            text.append("\n");
            text.append(formatWithSpaces(deepLevel, "]"));
            return text.toString();
        }


        private String formatWithSpaces(int deepLevel, String text) {
            String result;
            if (deepLevel > 0) {
                result = String.format("%" + deepLevel + "s%s", " ", text);
            } else {
                result = String.format("%s", text);
            }

            return result;
        }
    }

    public static class ContainerElement implements IContainerElement {

        private TextPosition position;
        private ContainerElementType type;


        private ContainerElement(final TextPosition position,
                final ContainerElementType type) {
            this.position = position;
            this.type = type;
        }


        public TextPosition getPosition() {
            return position;
        }


        public void increaseEndPosition() {
            position = new TextPosition(position.getText(),
                    position.getStart(), position.getEnd() + 1);
        }


        @Override
        public boolean isComplex() {
            return false;
        }


        @Override
        public ContainerElementType getType() {
            return type;
        }


        @Override
        public String toString() {
            return String.format("ContainerElement [type=%s, position=%s]",
                    type, position);
        }


        @Override
        public String prettyPrint(int deepLevel) {
            return formatWithSpaces(deepLevel, this.toString());
        }


        private String formatWithSpaces(int deepLevel, String text) {
            String result;
            if (deepLevel > 0) {
                result = String.format("%" + deepLevel + "s%s", " ", text);
            } else {
                result = String.format("%s", text);
            }

            return result;
        }
    }

    public static interface IContainerElement {

        boolean isComplex();


        ContainerElementType getType();


        String prettyPrint(int deepLevel);
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

    public enum ContainerElementType {
        TEXT(ContainerSettings.newInstance().setCanBeMerged(true)), WHITESPACE(
                ContainerSettings.newInstance().setCanBeMerged(true), ' ', '\t'), ESCAPE(
                ContainerSettings.newInstance().setCanBeMerged(true), '\\'), VARIABLE_TYPE_ID(
                ContainerSettings.newInstance(), '$', '@', '&', '%'), CURRLY_BRACKET_OPEN(
                ContainerSettings.newInstance().setNewContainerOpener(true),
                '{'), CURRLY_BRACKET_CLOSE(ContainerSettings.newInstance()
                .setCloseForType(CURRLY_BRACKET_OPEN), '}'), SQUARE_BRACKET_OPEN(
                ContainerSettings.newInstance().setNewContainerOpener(true),
                '['), SQUARE_BRACKET_CLOSE(ContainerSettings.newInstance()
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
                                + representationChar
                                + "\' is already defined for " + checkType);
                    }
                }
            }
        }
    }
}

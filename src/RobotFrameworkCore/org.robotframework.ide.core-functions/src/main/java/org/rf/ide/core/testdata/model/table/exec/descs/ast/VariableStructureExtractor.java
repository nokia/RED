/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.ast;

import java.util.List;

import org.rf.ide.core.testdata.model.table.exec.descs.TextPosition;


public class VariableStructureExtractor {

    public Container buildStructureTree(final String text) {
        final Container mainContainer = new Container(null);

        if (text != null) {
            Container currentContainer = mainContainer;

            final char[] textChars = text.toCharArray();
            final int textLength = textChars.length;
            for (int charIndex = 0; charIndex < textLength; charIndex++) {
                final char currentChar = textChars[charIndex];
                final ContainerElementType type = ContainerElementType
                        .getTypeFor(currentChar);
                final ContainerElement element = new ContainerElement(
                        new TextPosition(text, charIndex, charIndex), type);
                if (type.shouldOpenNewContainer()) {
                    final Container newContainer = new Container(currentContainer);
                    currentContainer.addElement(newContainer);
                    currentContainer = newContainer;
                    currentContainer.addElement(element);

                } else if (shouldCloseContainer(type)) {
                    final Container matchingContainer = findNearestContainerToClose(
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
                    final List<IContainerElement> elements = currentContainer.getElements();
                    if (!elements.isEmpty()) {
                        final IContainerElement lastElement = elements.get(elements.size() - 1);
                        final ContainerElementType lastType = lastElement.getType();

                        if (lastType != null && lastType.canBeMerged() && type == lastType) {
                            if (lastElement instanceof ContainerElement) {
                                final ContainerElement contElement = (ContainerElement) lastElement;
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
            for (final IContainerElement contElem : container.getElements()) {
                if (contElem.isComplex()) {
                    final Container cont = (Container) contElem;
                    if (cont.isOpenForModification()) {
                        closeContainer(cont);
                    }
                }
            }
            container.closeForModification();
        }
    }

    private Container findNearestContainerToClose(final ContainerElementType type, final Container currentContainer) {
        Container cont = null;

        if (currentContainer != null) {
            if (currentContainer.isOpenForModification()) {
                for (final IContainerElement contElement : currentContainer.getElements()) {
                    if (contElement.isComplex()) {
                        cont = findNearestContainerToClose(type, (Container) contElement);
                        if (cont != null) {
                            break;
                        }
                    }
                }

                if (cont == null) {
                    if (!currentContainer.getElements().isEmpty()) {
                        if (type == ContainerElementType
                                .getCloseContainerType(currentContainer.getElements().get(0).getType())) {
                            cont = currentContainer;
                        }
                    }
                }
            }
        }
        return cont;
    }


    private boolean shouldCloseContainer(final ContainerElementType type) {
        return type.getCloseContainer() != null;
    }
}

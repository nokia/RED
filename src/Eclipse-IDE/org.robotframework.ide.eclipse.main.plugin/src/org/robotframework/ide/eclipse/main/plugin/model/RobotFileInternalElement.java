/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import java.util.Objects;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.rf.ide.core.testdata.model.FilePosition;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 *
 */
public interface RobotFileInternalElement extends RobotElement {

    /**
     * Gets the suite file in which this element is contained or null if it is
     * not inside the suite file.
     * 
     * @return Model object representing containg file.
     */
    RobotSuiteFile getSuiteFile();

    /**
     * Gets position of the whole element inside the file
     * 
     * @return position
     */
    Position getPosition();

    /**
     * Gets position of the defining token (usually name of the element)
     * 
     * @return position
     */
    DefinitionPosition getDefinitionPosition();

    /**
     * Gets model element for given offset in file
     * 
     * @param offset
     * @return
     */
    Optional<? extends RobotElement> findElement(final int offset);

    /**
     * Gets the comment of the element
     * 
     * @return Comment of the element
     */
    String getComment();

    public static final class DefinitionPosition {

        private final int line;

        private final int offset;

        private final int length;

        public DefinitionPosition(final FilePosition positionInFile, final int length) {
            this(positionInFile.getLine(), positionInFile.getOffset(), length);
        }

        public DefinitionPosition(final int line, final int offset, final int length) {
            this.line = line;
            this.offset = offset;
            this.length = length;
        }

        public int getOffset() {
            return offset;
        }

        public int getLength() {
            return length;
        }

        public int getLine() {
            return line;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj != null && obj.getClass() == DefinitionPosition.class) {
                final DefinitionPosition that = (DefinitionPosition) obj;
                return this.line == that.line && this.offset == that.offset && this.length == that.length;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(line, offset, length);
        }

        public IRegion toRegion() {
            return new Region(offset, length);
        }
    }
}

/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.List;
import java.util.Optional;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;

import com.google.common.base.Objects;

public interface ISyntaxColouringRule {

    public static final IToken DEFAULT_TOKEN = new Token(null);

    public boolean isApplicable(IRobotLineElement nextToken);

    public Optional<PositionedTextToken> evaluate(final IRobotLineElement nextToken, int offsetInToken,
            List<IRobotLineElement> context);


    public static final class PositionedTextToken {

        private final IToken token;

        private final Position position;

        public PositionedTextToken(final IToken token, final int offset, final int length) {
            this.token = token;
            this.position = new Position(offset, length);
        }

        public IToken getToken() {
            return token;
        }

        public Position getPosition() {
            return position;
        }

        public int getOffset() {
            return position.getOffset();
        }

        public int getLength() {
            return position.getLength();
        }

        public void setOffset(final int offset) {
            position.setOffset(offset);
        }

        public void setLength(final int length) {
            position.setLength(length);
        }

        @Override
        public String toString() {
            // for debugging purposes only
            return "[" + getOffset() + ", " + getLength() + "] -> "
                    + (token.getData() == null ? "<null>" : token.getData().toString());
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof PositionedTextToken) {
                final PositionedTextToken that = (PositionedTextToken) obj;
                return Objects.equal(this.token.getData(), that.token.getData())
                        && this.getPosition().equals(that.getPosition());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return position.hashCode();
        }
    }
}

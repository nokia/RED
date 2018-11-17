/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotVersion;

public class RobotSpecialTokensTest {

    @Test
    public void upperCasedForIsRecognized_inRf31() {
        final RobotSpecialTokens specials = new RobotSpecialTokens();
        specials.initializeFor(new RobotVersion(3, 1));

        assertThat(specials.recognize(new FilePosition(1, 2), "FOR")).hasSize(1);
    }

    @Test
    public void nonUpperCasedForIsNotRecognized_inRf31() {
        final RobotSpecialTokens specials = new RobotSpecialTokens();
        specials.initializeFor(new RobotVersion(3, 1));

        assertThat(specials.recognize(new FilePosition(1, 2), "for")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "foR")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "fOr")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "fOR")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "For")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "FoR")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "FOr")).isEmpty();
    }
    
    @Test
    public void anyForIsNotRecognized_inRfUnder31() {
        final RobotSpecialTokens specials = new RobotSpecialTokens();
        specials.initializeFor(new RobotVersion(3, 0));

        assertThat(specials.recognize(new FilePosition(1, 2), "for")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "foR")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "fOr")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "fOR")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "For")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "FoR")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "FOr")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "FOR")).isEmpty();
    }
}

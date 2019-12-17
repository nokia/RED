/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.FilePosition;

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

    @Test
    public void upperCasedInTokensAreRecognized_inRf32() {
        final RobotSpecialTokens specials = new RobotSpecialTokens();
        specials.initializeFor(new RobotVersion(3, 2));

        assertThat(specials.recognize(new FilePosition(1, 2), "IN")).hasSize(1);
        assertThat(specials.recognize(new FilePosition(1, 2), "IN RANGE")).hasSize(1);
        assertThat(specials.recognize(new FilePosition(1, 2), "IN ENUMERATE")).hasSize(1);
        assertThat(specials.recognize(new FilePosition(1, 2), "IN ZIP")).hasSize(1);
    }

    @Test
    public void nonUpperCasedInTokensAreNotRecognized_inRf32() {
        final RobotSpecialTokens specials = new RobotSpecialTokens();
        specials.initializeFor(new RobotVersion(3, 2));

        assertThat(specials.recognize(new FilePosition(1, 2), "In")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "iN")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "in")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "I N")).isEmpty();

        assertThat(specials.recognize(new FilePosition(1, 2), "I N R A N G E")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "in RANGE")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "IN range")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "In RaNgE")).isEmpty();

        assertThat(specials.recognize(new FilePosition(1, 2), "I N E N U M E R A T E")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "in ENUMERATE")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "IN enumerate")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "In EnUmErAtE")).isEmpty();

        assertThat(specials.recognize(new FilePosition(1, 2), "I N Z I P")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "in ZIP")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "IN zip")).isEmpty();
        assertThat(specials.recognize(new FilePosition(1, 2), "iN zIp")).isEmpty();
    }

    @Test
    public void nonUpperCasedInTokensAreRecognized_inRfUnder32() {
        final RobotSpecialTokens specials = new RobotSpecialTokens();
        specials.initializeFor(new RobotVersion(3, 1));

        assertThat(specials.recognize(new FilePosition(1, 2), "In")).hasSize(1);
        assertThat(specials.recognize(new FilePosition(1, 2), "iN")).hasSize(1);
        assertThat(specials.recognize(new FilePosition(1, 2), "in")).hasSize(1);
        assertThat(specials.recognize(new FilePosition(1, 2), "I N")).hasSize(1);

        assertThat(specials.recognize(new FilePosition(1, 2), "I N R A N G E")).hasSize(1);
        assertThat(specials.recognize(new FilePosition(1, 2), "in RANGE")).hasSize(1);
        assertThat(specials.recognize(new FilePosition(1, 2), "IN range")).hasSize(1);
        assertThat(specials.recognize(new FilePosition(1, 2), "In RaNgE")).hasSize(1);

        assertThat(specials.recognize(new FilePosition(1, 2), "I N E N U M E R A T E")).hasSize(1);
        assertThat(specials.recognize(new FilePosition(1, 2), "in ENUMERATE")).hasSize(1);
        assertThat(specials.recognize(new FilePosition(1, 2), "IN enumerate")).hasSize(1);
        assertThat(specials.recognize(new FilePosition(1, 2), "In EnUmErAtE")).hasSize(1);

        assertThat(specials.recognize(new FilePosition(1, 2), "I N Z I P")).hasSize(1);
        assertThat(specials.recognize(new FilePosition(1, 2), "in ZIP")).hasSize(1);
        assertThat(specials.recognize(new FilePosition(1, 2), "IN zip")).hasSize(1);
        assertThat(specials.recognize(new FilePosition(1, 2), "iN zIp")).hasSize(1);
    }
}

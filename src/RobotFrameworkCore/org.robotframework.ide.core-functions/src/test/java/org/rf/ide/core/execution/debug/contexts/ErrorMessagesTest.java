/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug.contexts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import org.junit.Test;

public class ErrorMessagesTest {

    @Test
    public void messageChoosingMethodsTests() {
        assertThat(ErrorMessages.errorOfSuiteNotFoundBecauseOfMissingLocation(true))
                .isEqualTo(ErrorMessages.suiteNotFound_missingLocalDir);
        assertThat(ErrorMessages.errorOfSuiteNotFoundBecauseOfMissingLocation(false))
                .isEqualTo(ErrorMessages.suiteNotFound_missingLocalFile);

        assertThat(ErrorMessages.errorOfSuiteNotFoundBecauseOfUnknownLocation(true))
                .isEqualTo(ErrorMessages.suiteNotFound_unknownLocalDir);
        assertThat(ErrorMessages.errorOfSuiteNotFoundBecauseOfUnknownLocation(false))
                .isEqualTo(ErrorMessages.suiteNotFound_unknownLocalFile);

        assertThat(ErrorMessages.errorOfSuitePrePostKwNotFound(true)).isEqualTo(ErrorMessages.suiteSetupKwNotFound);
        assertThat(ErrorMessages.errorOfSuitePrePostKwNotFound(false)).isEqualTo(ErrorMessages.suiteTeardownKwNotFound);

        assertThat(ErrorMessages.errorOfSuitePrePostKwNotFoundBecauseOfMissingInit(true))
                .isEqualTo(ErrorMessages.suiteSetupKwNotFound_missingInit);
        assertThat(ErrorMessages.errorOfSuitePrePostKwNotFoundBecauseOfMissingInit(false))
                .isEqualTo(ErrorMessages.suiteTeardownKwNotFound_missingInit);

        assertThat(ErrorMessages.errorOfSuitePrePostKwNotFoundBecauseOfMissingSetting(true))
                .isEqualTo(ErrorMessages.suiteSetupKwNotFound_missingSetting);
        assertThat(ErrorMessages.errorOfSuitePrePostKwNotFoundBecauseOfMissingSetting(false))
                .isEqualTo(ErrorMessages.suiteTeardownKwNotFound_missingSetting);

        assertThat(ErrorMessages.errorOfSuitePrePostKwNotFoundBecauseOfDifferentCall(true))
                .isEqualTo(ErrorMessages.suiteSetupKwNotFound_diffCall);
        assertThat(ErrorMessages.errorOfSuitePrePostKwNotFoundBecauseOfDifferentCall(false))
                .isEqualTo(ErrorMessages.suiteTeardownKwNotFound_diffCall);

        assertThat(ErrorMessages.errorOfLocalPrePostKwNotFound(true))
                .isEqualTo(ErrorMessages.testSetupKwNotFound);
        assertThat(ErrorMessages.errorOfLocalPrePostKwNotFound(false))
                .isEqualTo(ErrorMessages.testTeardownKwNotFound);

        assertThat(ErrorMessages.errorOfLocalPrePostKwNotFoundBecauseOfDifferentCall(true, true))
                .isEqualTo(ErrorMessages.testSetupKwNotFound_diffCall);
        assertThatIllegalStateException()
                .isThrownBy(() -> ErrorMessages.errorOfLocalPrePostKwNotFoundBecauseOfDifferentCall(true, false));
        assertThat(ErrorMessages.errorOfLocalPrePostKwNotFoundBecauseOfDifferentCall(false, true))
                .isEqualTo(ErrorMessages.testTeardownKwNotFound_diffCall);
        assertThat(ErrorMessages.errorOfLocalPrePostKwNotFoundBecauseOfDifferentCall(false, false))
                .isEqualTo(ErrorMessages.keywordTeardownKwNotFound_diffCall);

        assertThat(ErrorMessages.errorOfLocalPrePostKwNotFoundBecauseOfMissingSetting(true, true))
                .isEqualTo(ErrorMessages.testSetupKwNotFound_missingSetting);
        assertThatIllegalStateException()
                .isThrownBy(() -> ErrorMessages.errorOfLocalPrePostKwNotFoundBecauseOfMissingSetting(true, false));
        assertThat(ErrorMessages.errorOfLocalPrePostKwNotFoundBecauseOfMissingSetting(false, true))
                .isEqualTo(ErrorMessages.testTeardownKwNotFound_missingSetting);
        assertThat(ErrorMessages.errorOfLocalPrePostKwNotFoundBecauseOfMissingSetting(false, false))
                .isEqualTo(ErrorMessages.keywordTeardownKwNotFound_missingSetting);

    }

}

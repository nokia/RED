/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug.contexts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.net.URI;
import java.util.EnumSet;
import java.util.Optional;

import org.junit.Test;
import org.rf.ide.core.execution.debug.KeywordCallType;
import org.rf.ide.core.execution.debug.RobotBreakpointSupplier;
import org.rf.ide.core.execution.debug.RunningKeyword;
import org.rf.ide.core.execution.debug.StackFrameContext;
import org.rf.ide.core.testdata.model.RobotFile;

public class SuiteContextTest {

    @Test
    public void suiteContextHasNoSourceAssociatedIfNotProvided() {
        final SuiteContext context1 = new SuiteContext("suite");
        final SuiteContext context2 = new SuiteContext("suite", true, "error");
        final SuiteContext context3 = new SuiteContext("suite", null, true, "error");
        final SuiteContext context4 = new SuiteContext("suite", null, false,
                uri -> Optional.empty());

        assertThat(context1.getAssociatedPath()).isEmpty();
        assertThat(context1.getFileRegion()).isEmpty();

        assertThat(context2.getAssociatedPath()).isEmpty();
        assertThat(context2.getFileRegion()).isEmpty();

        assertThat(context3.getAssociatedPath()).isEmpty();
        assertThat(context3.getFileRegion()).isEmpty();

        assertThat(context4.getAssociatedPath()).isEmpty();
        assertThat(context4.getFileRegion()).isEmpty();
    }

    @Test
    public void suiteContextHasSourceAssociatedWhenProvided() {
        final SuiteContext context1 = new SuiteContext("suite", URI.create("file:///suite"), true, "error");
        final SuiteContext context2 = new SuiteContext("suite", URI.create("file:///suite.robot"), false,
                uri -> Optional.empty());

        assertThat(context1.getAssociatedPath()).contains(URI.create("file:///suite"));
        assertThat(context1.getFileRegion()).isEmpty();

        assertThat(context2.getAssociatedPath()).contains(URI.create("file:///suite.robot"));
        assertThat(context2.getFileRegion()).isEmpty();
    }

    @Test
    public void suiteContextIsNotErroneousWhenNoErrorMessageIsProvided() {
        final SuiteContext context1 = new SuiteContext("suite");
        final SuiteContext context2 = new SuiteContext("suite", true, null);
        final SuiteContext context3 = new SuiteContext("suite", URI.create("file:///suite"), true, (String) null);
        final SuiteContext context4 = new SuiteContext("suite", URI.create("file:///suite.robot"), false,
                uri -> Optional.empty());

        assertThat(context1.isErroneous()).isFalse();
        assertThat(context1.getErrorMessage()).isEmpty();

        assertThat(context2.isErroneous()).isFalse();
        assertThat(context2.getErrorMessage()).isEmpty();

        assertThat(context3.isErroneous()).isFalse();
        assertThat(context3.getErrorMessage()).isEmpty();

        assertThat(context4.isErroneous()).isFalse();
        assertThat(context4.getErrorMessage()).isEmpty();
    }

    @Test
    public void suiteContextIsErroneousWhenErrorMessageIsProvided() {
        final SuiteContext context1 = new SuiteContext("suite", true, "error1");
        final SuiteContext context2 = new SuiteContext("suite", URI.create("file:///suite"), true, "error2");

        assertThat(context1.isErroneous()).isTrue();
        assertThat(context1.getErrorMessage()).contains("error1");

        assertThat(context2.isErroneous()).isTrue();
        assertThat(context2.getErrorMessage()).contains("error2");
    }

    @Test
    public void suiteIsDirectory_whenProvidedFlagSaysSo() {
        final SuiteContext context1 = new SuiteContext("suite", true, "error1");
        final SuiteContext context2 = new SuiteContext("suite", URI.create("file:///suite.robot"), false, "error2");

        assertThat(context1.isDirectory()).isTrue();
        assertThat(context2.isDirectory()).isFalse();
    }

    @Test
    public void itIsIllegalToMoveToToDifferentKeywordTypeThanSetupOrTeardown() throws Exception {
        final SuiteContext context = new SuiteContext("suite", URI.create("file:///suite.robot"), false, "error2");

        for (final KeywordCallType type : EnumSet.of(KeywordCallType.NORMAL_CALL, KeywordCallType.FOR,
                KeywordCallType.FOR_ITERATION)) {

            assertThatExceptionOfType(IllegalDebugContextStateException.class).isThrownBy(
                    () -> context.moveTo(new RunningKeyword("_", "_", type), mock(RobotBreakpointSupplier.class)))
                    .withMessage("Only suite setup or teardown keyword call is possible in current context")
                    .withNoCause();
        }
    }
    
    @Test
    public void erroneousSetupContextIsReturned_whenAlreadySuiteContextIsErroneous() {
        final RobotFile model = ModelBuilder.modelForFile("suite.robot")
            .withSettingsTable()
                .withSuiteSetup("kw", "1")
            .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        
        final SuiteContext context = new SuiteContext("suite", modelUri, false, "error");
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Suite Setup call of 'lib.kw' keyword\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }
    
    @Test
    public void erroneousTeardownContextIsReturned_whenAlreadySuiteContextIsErroneous() {
        final RobotFile model = ModelBuilder.modelForFile("suite.robot")
            .withSettingsTable()
                .withSuiteTeardown("kw", "1")
            .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();
        
        final SuiteContext context = new SuiteContext("suite", modelUri, false, "error");
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Suite Teardown call of 'lib.kw' keyword\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousSetupContextIsReturned_whenCannotProvideInitFileForTheSuite_1() {
        final URI suiteUri = URI.create("file:///suite");
        final SuiteContext context = new SuiteContext("suite", suiteUri, true, u -> Optional.empty());

        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Suite Setup call of 'lib.kw' keyword\n"
                + "The suite 'suite' is located in workspace at "
                + new File(suiteUri).getAbsolutePath()
                + " but RED couldn't find __init__ file inside this directory\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenCannotProvideInitFileForTheSuite_1() {
        final URI suiteUri = URI.create("file:///suite");
        final SuiteContext context = new SuiteContext("suite", suiteUri, true, u -> Optional.empty());

        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Suite Teardown call of 'lib.kw' keyword\n"
                + "The suite 'suite' is located in workspace at " + new File(suiteUri).getAbsolutePath()
                + " but RED couldn't find __init__ file inside this directory\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousSetupContextIsReturned_whenCannotProvideInitFileForTheSuite_2() {
        final SuiteContext context = new SuiteContext("suite");

        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Suite Setup call of 'lib.kw' keyword\n"
                + "The suite 'suite' is located in workspace at <unknown> but RED couldn't find __init__ file inside this directory\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenCannotProvideInitFileForTheSuite_2() {
        final SuiteContext context = new SuiteContext("suite");

        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Suite Teardown call of 'lib.kw' keyword\n"
                + "The suite 'suite' is located in workspace at <unknown> but RED couldn't find __init__ file inside this directory\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousSetupContextIsReturned_whenCannotProvideInitFileForTheSuite_3() {
        final SuiteContext context = new SuiteContext("suite", true, null);

        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Suite Setup call of 'lib.kw' keyword\n"
                + "The suite 'suite' is located in workspace at <unknown> but RED couldn't find __init__ file inside this directory\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenCannotProvideInitFileForTheSuite_3() {
        final SuiteContext context = new SuiteContext("suite", true, null);

        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Suite Teardown call of 'lib.kw' keyword\n"
                + "The suite 'suite' is located in workspace at <unknown> but RED couldn't find __init__ file inside this directory\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousSetupContextIsReturned_whenCannotProvideInitFileForTheSuite_4() {
        final URI suiteUri = URI.create("file:///suite");
        final SuiteContext context = new SuiteContext("suite", suiteUri, true, (String) null);

        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Suite Setup call of 'lib.kw' keyword\n"
                + "The suite 'suite' is located in workspace at " + new File(suiteUri).getAbsolutePath()
                + " but RED couldn't find __init__ file inside this directory\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenCannotProvideInitFileForTheSuite_4() {
        final URI suiteUri = URI.create("file:///suite");
        final SuiteContext context = new SuiteContext("suite", suiteUri, true, (String) null);

        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Suite Teardown call of 'lib.kw' keyword\n"
                + "The suite 'suite' is located in workspace at " + new File(suiteUri).getAbsolutePath()
                + " but RED couldn't find __init__ file inside this directory\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousSetupContextIsReturned_whenFileIsProvidedButThereIsNoSettingsTable() {
        final RobotFile model = ModelBuilder.modelForFile("suite.robot").build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();

        final SuiteContext context = new SuiteContext("suite", modelUri, false, u -> Optional.of(model));
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Suite Setup call of 'lib.kw' keyword\n"
                + "Suite Setup setting could not be found in this suite\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenFileIsProvidedButThereIsNoSettingsTable() {
        final RobotFile model = ModelBuilder.modelForFile("suite.robot").build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();

        final SuiteContext context = new SuiteContext("suite", modelUri, false, u -> Optional.of(model));
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Suite Teardown call of 'lib.kw' keyword\n"
                + "Suite Teardown setting could not be found in this suite\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousSetupContextIsReturned_whenFileIsProvidedWithSettingsSectionButNoSuiteSetupIsThere() {
        final RobotFile model = ModelBuilder.modelForFile("suite.robot")
                .withSettingsTable()
                    .withSuiteTeardown("kw", "1")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();

        final SuiteContext context = new SuiteContext("suite", modelUri, false, u -> Optional.of(model));
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Suite Setup call of 'lib.kw' keyword\n"
                + "Suite Setup setting could not be found in this suite\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenFileIsProvidedWithSettingsSectionButNoSuiteTeardownIsThere() {
        final RobotFile model = ModelBuilder.modelForFile("suite.robot")
                .withSettingsTable()
                .withSuiteSetup("kw", "1")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();

        final SuiteContext context = new SuiteContext("suite", modelUri, false, u -> Optional.of(model));
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Suite Teardown call of 'lib.kw' keyword\n"
                + "Suite Teardown setting could not be found in this suite\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousSetupContextIsReturned_whenFileIsProvidedWithSettingsSectionSuiteSetupIsEmpty_1() {
        final RobotFile model = ModelBuilder.modelForFile("suite.robot")
                .withSettingsTable()
                    .withSuiteSetup(null)
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();

        final SuiteContext context = new SuiteContext("suite", modelUri, false, u -> Optional.of(model));
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Suite Setup call of 'lib.kw' keyword\n"
                + "Suite Setup setting could not be found in this suite\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenFileIsProvidedWithSettingsSectionSuiteTeardownIsEmpty_1() {
        final RobotFile model = ModelBuilder.modelForFile("suite.robot")
                .withSettingsTable()
                    .withSuiteTeardown(null)
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();

        final SuiteContext context = new SuiteContext("suite", modelUri, false, u -> Optional.of(model));
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Suite Teardown call of 'lib.kw' keyword\n"
                + "Suite Teardown setting could not be found in this suite\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousSetupContextIsReturned_whenFileIsProvidedWithSettingsSectionSuiteSetupIsEmpty_2() {
        final RobotFile model = ModelBuilder.modelForFile("suite.robot")
                .withSettingsTable()
                    .withSuiteSetup("")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();

        final SuiteContext context = new SuiteContext("suite", modelUri, false, u -> Optional.of(model));
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Suite Setup call of 'lib.kw' keyword\n"
                + "Suite Setup setting could not be found in this suite\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenFileIsProvidedWithSettingsSectionSuiteTeardownIsEmpty_2() {
        final RobotFile model = ModelBuilder.modelForFile("suite.robot")
                .withSettingsTable()
                    .withSuiteTeardown("")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();

        final SuiteContext context = new SuiteContext("suite", modelUri, false, u -> Optional.of(model));
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Suite Teardown call of 'lib.kw' keyword\n"
                + "Suite Teardown setting could not be found in this suite\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousSetupContextIsReturned_whenFileIsProvidedWithSuiteSetupButItDoesntMatch() {
        final RobotFile model = ModelBuilder.modelForFile("suite.robot")
                .withSettingsTable()
                    .withSuiteSetup("non-matching", "1")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();

        final SuiteContext context = new SuiteContext("suite", modelUri, false, u -> Optional.of(model));
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Suite Setup call of 'lib.kw' keyword\n"
                + "Suite Setup setting was found but seem to call non-matching keyword 'non-matching'\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void erroneousTeardownContextIsReturned_whenFileIsProvidedWithSuiteTeardownButItDoesntMatch() {
        final RobotFile model = ModelBuilder.modelForFile("suite.robot")
                .withSettingsTable()
                    .withSuiteTeardown("non-matching", "1")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();

        final SuiteContext context = new SuiteContext("suite", modelUri, false, u -> Optional.of(model));
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isTrue();
        assertThat(newContext.getErrorMessage()).contains("Unable to find Suite Teardown call of 'lib.kw' keyword\n"
                + "Suite Teardown setting was found but seem to call non-matching keyword 'non-matching'\n");
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void validSetupContextIsReturned_whenFileIsProvidedWithMatchingSuiteSetup() {
        final RobotFile model = ModelBuilder.modelForFile("suite.robot")
                .withSettingsTable()
                    .withSuiteSetup("kw", "1")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();

        final SuiteContext context = new SuiteContext("suite", modelUri, false, u -> Optional.of(model));
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.SETUP),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isFalse();
        assertThat(newContext.getErrorMessage()).isEmpty();
        assertThat(newContext.previousContext()).isSameAs(context);
    }

    @Test
    public void validTeardownContextIsReturned_whenFileIsProvidedWithMatchingSuiteTeardown() {
        final RobotFile model = ModelBuilder.modelForFile("suite.robot")
                .withSettingsTable()
                    .withSuiteTeardown("kw", "1")
                .build();
        final URI modelUri = model.getParent().getProcessedFile().toURI();

        final SuiteContext context = new SuiteContext("suite", modelUri, false, u -> Optional.of(model));
        final StackFrameContext newContext = context.moveTo(new RunningKeyword("lib", "kw", KeywordCallType.TEARDOWN),
                mock(RobotBreakpointSupplier.class));

        assertThat(newContext.isErroneous()).isFalse();
        assertThat(newContext.getErrorMessage()).isEmpty();
        assertThat(newContext.previousContext()).isSameAs(context);
    }
}

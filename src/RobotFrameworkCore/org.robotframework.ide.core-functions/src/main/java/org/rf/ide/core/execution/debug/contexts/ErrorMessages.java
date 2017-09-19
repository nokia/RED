/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug.contexts;

@SuppressWarnings("PMD.VariableNamingConventions")
public class ErrorMessages {

    static final String suiteNotFound_unknownLocalDir = "Unable to find directory for suite '%s'\n"
            + "No resource could have been found for expected location inside RED workspace\n\n"
            + "    Robot-side location: %s\n"
            + "    RED-side expected location: <unknown> (no resource seem to match this suite)\n";

    static final String suiteNotFound_unknownLocalFile = "Unable to find file for suite '%s'\n"
            + "No resource could have been found for expected location inside RED workspace\n\n"
            + "    Robot-side location: %s\n"
            + "    RED-side expected location: <unknown> (no resource seem to match this suite)\n";

    static final String suiteNotFound_missingLocalDir = "Unable to find directory for suite '%s'\n"
            + "No resource could have been found for expected location inside RED workspace\n\n"
            + "    Robot-side location:%s\n"
            + "    RED-side expected location: %s\n";

    static final String suiteNotFound_missingLocalFile = "Unable to find file for suite '%s'\n"
            + "No resource could have been found for expected location inside RED workspace\n\n"
            + "    Robot-side location:%s\n"
            + "    RED-side expected location: %s\n";
    

    public static final String testNotFound_missingSuite = "Unable to find test '%s'\n"
            + "RED was unable to find suite where the test could be located\n";

    public static final String testNotFound_noMatch = "Unable to find test '%s'\n"
            + "There is no such test in current suite\n";

    public static final String testNotFound_tooManyMatches = "Unable to find test '%s'\n"
            + "There are multiple matching cases in current suite\n";

    public static final String testNotFound_templatesMismatch = "The test '%s' was found but templates in use are mismatched\n"
            + "Robot uses %s template for this test case but here %s template is defined";


    public static final String keywordNotFound = "Unable to find keyword '%s'\n"
            + "RED was unable to find suite from which keyword could be accessed\n";

    public static final String keywordNotFound_noMatch = "Unable to find keyword '%s'\n"
            + "There is no such keyword accessible from '%s' file\n";

    public static final String keywordNotFound_tooManyMatches = "Unable to find keyword '%s'\n"
            + "There are multiple matching keywords accessible from current suite\n";


    static final String suiteSetupKwNotFound = "Unable to find Suite Setup call of '%s' keyword\n";

    static final String suiteTeardownKwNotFound = "Unable to find Suite Teardown call of '%s' keyword\n";

    static final String suiteSetupKwNotFound_missingInit = "Unable to find Suite Setup call of '%s' keyword\n"
            + "The suite '%s' is located in workspace at %s but RED couldn't find __init__ file inside this directory\n";

    static final String suiteTeardownKwNotFound_missingInit = "Unable to find Suite Teardown call of '%s' keyword\n"
            + "The suite '%s' is located in workspace at %s but RED couldn't find __init__ file inside this directory\n";

    static final String suiteSetupKwNotFound_missingSetting = "Unable to find Suite Setup call of '%s' keyword\n"
            + "Suite Setup setting could not be found in this suite\n";

    static final String suiteTeardownKwNotFound_missingSetting = "Unable to find Suite Teardown call of '%s' keyword\n"
            + "Suite Teardown setting could not be found in this suite\n";

    static final String suiteSetupKwNotFound_diffCall = "Unable to find Suite Setup call of '%s' keyword\n"
            + "Suite Setup setting was found but seem to call non-matching keyword '%s'\n";

    static final String suiteTeardownKwNotFound_diffCall = "Unable to find Suite Teardown call of '%s' keyword\n"
            + "Suite Teardown setting was found but seem to call non-matching keyword '%s'\n";


    static final String testSetupKwNotFound = "Unable to find Test Setup call of '%s' keyword\n";

    static final String testTeardownKwNotFound = "Unable to find Test Teardown call of '%s' keyword\n";

    static final String testSetupKwNotFound_missingSetting = "Unable to find Test Setup call of '%s' keyword\n"
            + "Test Setup setting could not be found in this suite\n";

    static final String testTeardownKwNotFound_missingSetting = "Unable to find Test Teardown call of '%s' keyword\n"
            + "Test Teardown setting could not be found in this suite\n";

    static final String testSetupKwNotFound_diffCall = "Unable to find Test Setup call of '%s' keyword\n"
            + "Test Setup setting was found but seem to call non-matching keyword '%s'\n";

    static final String testTeardownKwNotFound_diffCall = "Unable to find Test Teardown call of '%s' keyword\n"
            + "Test Teardown setting was found but seem to call non-matching keyword '%s'\n";


    static final String keywordTeardownKwNotFound_missingSetting = "Unable to find Keyword Teardown call of '%s' keyword\n"
            + "Keyword Teardown setting could not be found in this suite\n";

    static final String keywordTeardownKwNotFound_diffCall = "Unable to find Keyword Teardown call of '%s' keyword\n"
            + "Keyword Teardown setting was found but seem to call non-matching keyword '%s'\n";


    static final String executableCallNotFound = "Unable to find executable call of '%s' keyword\n";

    static final String executableCallNotFound_diffCall = "Unable to find executable call of '%s' keyword\n"
            + "An executable was found but seem to call non-matching keyword '%s'\n";

    static final String executableCallNotFound_diffFor = "Unable to find matching :FOR loop\n"
            + "':FOR %s' was found but ':FOR %s' is being executed\n";

    static final String executableCallNotFound_foundForButCall = "Unable to find executable call of '%s' keyword\n"
            + ":FOR loop was found instead\n";

    static final String executableCallNotFound_foundCallButFor = "Unable to find :FOR loop\n"
            + "An executable was found calling '%s' keyword\n";


    static final String executableIterationNotFound = "No loop found for iteration of '%s'\n";

    static final String executableIterationMismatch = "The loop is iterating with [%s] variables but [%s] were expected\n";


    public static String errorOfSuiteNotFoundBecauseOfUnknownLocation(final boolean isDirectory) {
        return isDirectory ? suiteNotFound_unknownLocalDir : suiteNotFound_unknownLocalFile;
    }

    public static String errorOfSuiteNotFoundBecauseOfMissingLocation(final boolean isDirectory) {
        return isDirectory ? suiteNotFound_missingLocalDir : suiteNotFound_missingLocalFile;
    }

    static String errorOfSuitePrePostKwNotFound(final boolean isSetup) {
        return isSetup ? suiteSetupKwNotFound : suiteTeardownKwNotFound;
    }

    static String errorOfSuitePrePostKwNotFoundBecauseOfMissingInit(final boolean isSetup) {
        return isSetup ? suiteSetupKwNotFound_missingInit : suiteTeardownKwNotFound_missingInit;
    }

    static String errorOfSuitePrePostKwNotFoundBecauseOfMissingSetting(final boolean isSetup) {
        return isSetup ? suiteSetupKwNotFound_missingSetting : suiteTeardownKwNotFound_missingSetting;
    }

    static String errorOfSuitePrePostKwNotFoundBecauseOfDifferentCall(final boolean isSetup) {
        return isSetup ? suiteSetupKwNotFound_diffCall : suiteTeardownKwNotFound_diffCall;
    }

    static String errorOfLocalPrePostKwNotFound(final boolean isSetup) {
        return isSetup ? testSetupKwNotFound : testTeardownKwNotFound;
    }

    static String errorOfLocalPrePostKwNotFoundBecauseOfDifferentCall(final boolean isSetup,
            final boolean isTest) {
        if (isTest) {
            return isSetup ? testSetupKwNotFound_diffCall : testTeardownKwNotFound_diffCall;
        } else {
            if (isSetup) {
                throw new IllegalStateException("There is not setup for keywords!");
            }
            return keywordTeardownKwNotFound_diffCall;
        }
    }

    static String errorOfLocalPrePostKwNotFoundBecauseOfMissingSetting(final boolean isSetup,
            final boolean isTest) {
        if (isTest) {
            return isSetup ? testSetupKwNotFound_missingSetting : testTeardownKwNotFound_missingSetting;
        } else {
            if (isSetup) {
                throw new IllegalStateException("There is not setup for keywords!");
            }
            return keywordTeardownKwNotFound_missingSetting;
        }
    }
}

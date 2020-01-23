/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug.contexts;

import java.net.URI;
import java.util.List;

import org.rf.ide.core.execution.debug.RobotBreakpointSupplier;
import org.rf.ide.core.execution.debug.RunningKeyword;
import org.rf.ide.core.execution.debug.StackFrameContext;
import org.rf.ide.core.testdata.model.ExecutableSetting;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.CommonCase;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopDeclarationRowDescriptor;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

import com.google.common.base.Preconditions;

class CommonContextsTransitions {

    static StackFrameContext moveToTestSetupOrTeardown(final List<RobotFile> models, final RunningKeyword keyword,
            final StackFrameContext previousContext, final RobotBreakpointSupplier breakpointSupplier) {

        Preconditions.checkArgument(keyword.isSetup() || keyword.isTeardown());

        final boolean isSetup = keyword.isSetup();
        final StackFrameContext newContext = moveToSuiteDefinedSetupOrTeardown(models, isSetup, keyword,
                previousContext, breakpointSupplier);

        if (newContext != null) {
            return newContext;
        } else {
            final URI fileUri = models.get(0).getParent().getProcessedFile().toURI();
            final String msg = ErrorMessages.errorOfLocalPrePostKwNotFound(isSetup);
            final String errorMsg = String.format(msg, keyword.asCall());
            return new SetupTeardownContext(fileUri, 1, errorMsg, previousContext);
        }
    }

    static StackFrameContext moveToTestSetupOrTeardown(final CommonCase<?, ?> theCase, final List<RobotFile> models,
            final RunningKeyword keyword, final StackFrameContext previousContext,
            final RobotBreakpointSupplier breakpointSupplier) {

        Preconditions.checkArgument(keyword.isSetup() || keyword.isTeardown());
        
        final boolean isSetup = keyword.isSetup();
        final URI fileUri = models.get(0).getParent().getProcessedFile().toURI();

        final List<? extends ExecutableSetting> settings = isSetup ? theCase.getSetupExecutables()
                : theCase.getTeardownExecutables();
        final ExecutableSetting setting = settings.isEmpty() ? null : settings.get(0);
        
        if (setting != null) {
            return moveToLocallyDefinedSetupOrTeardown(fileUri, setting, true, keyword, previousContext,
                    breakpointSupplier);

        } else {
            final StackFrameContext newContext = moveToSuiteDefinedSetupOrTeardown(models, isSetup, keyword,
                    previousContext, breakpointSupplier);

            if (newContext != null) {
                return newContext;
            } else {
                final String msg = ErrorMessages.errorOfLocalPrePostKwNotFound(isSetup);
                final String errorMsg = String.format(msg, keyword.asCall());
                return new SetupTeardownContext(fileUri, theCase.getDeclaration().getLineNumber(), errorMsg,
                        previousContext);
            }
        }
    }

    static StackFrameContext moveToLocallyDefinedSetupOrTeardown(final URI fileUri,
            final ExecutableSetting setting, final boolean isTest, final RunningKeyword keyword,
            final StackFrameContext previousContext, final RobotBreakpointSupplier breakpointSupplier) {

        final int declarationLine = setting.getDeclaration().getLineNumber();
        final RobotToken keywordToken = setting.getKeywordName();

        final boolean isSetup = setting.isSetup();
        if (keywordToken == null || keywordToken.getText().isEmpty()) {
            final String msg = ErrorMessages.errorOfLocalPrePostKwNotFoundBecauseOfMissingSetting(isSetup, isTest);
            final String errorMessage = String.format(msg, keyword.asCall());
            return new SetupTeardownContext(fileUri, declarationLine, errorMessage, previousContext);

        } else if (CallChecker.isCallOf(keywordToken.getText(), keyword)) {
            return new SetupTeardownContext(fileUri, keywordToken.getText(), declarationLine, previousContext,
                    breakpointSupplier);

        } else {
            final String msg = ErrorMessages.errorOfLocalPrePostKwNotFoundBecauseOfDifferentCall(isSetup, isTest);
            final String errorMessage = String.format(msg, keyword.asCall(), keywordToken.getText());
            return new SetupTeardownContext(fileUri, declarationLine, errorMessage, previousContext);
        }
    }

    private static StackFrameContext moveToSuiteDefinedSetupOrTeardown(final List<RobotFile> models,
            final boolean isSetup, final RunningKeyword keyword, final StackFrameContext previousContext,
            final RobotBreakpointSupplier breakpointSupplier) {

        for (final RobotFile fileModel : models) {
            final SettingTable settingsTable = fileModel.getSettingTable();
            if (settingsTable == null) {
                continue;
            }

            final List<? extends ExecutableSetting> settings = isSetup ? settingsTable.getTestSetups()
                    : settingsTable.getTestTeardowns();

            final ExecutableSetting setting = settings.isEmpty() ? null : settings.get(0);
            if (setting != null) {
                final int declarationLine = setting.getDeclaration().getLineNumber();
                final RobotToken keywordToken = setting.getKeywordName();
                final URI fileUri = fileModel.getParent().getProcessedFile().toURI();

                if (keywordToken == null || keywordToken.getText().isEmpty()) {
                    final String msg = ErrorMessages.errorOfLocalPrePostKwNotFoundBecauseOfMissingSetting(isSetup,
                            true);
                    final String errorMsg = String.format(msg, keyword.asCall());
                    return new SetupTeardownContext(fileUri, declarationLine, errorMsg, previousContext);

                } else if (CallChecker.isCallOf(keywordToken.getText(), keyword)) {
                    return new SetupTeardownContext(fileUri, keywordToken.getText(), declarationLine, previousContext,
                            breakpointSupplier);

                } else {
                    final String msg = ErrorMessages.errorOfLocalPrePostKwNotFoundBecauseOfDifferentCall(isSetup, true);
                    final String errorMsg = String.format(msg, keyword.asCall(), keywordToken.getText());
                    return new SetupTeardownContext(fileUri, declarationLine, errorMsg, previousContext);
                }
            }
        }
        return null;
    }

    static StackFrameContext moveToExecutable(final List<RobotFile> models, final URI locationUri,
            final List<ExecutableWithDescriptor> elements, final int index, final RunningKeyword keyword,
            final RobotBreakpointSupplier breakpointSupplier) {

        if (index >= elements.size()) {
            final int line = elements.get(elements.size() - 1).getDescriptor().getAction().getLineNumber();
            final String errorMsg = String.format(ErrorMessages.executableCallNotFound, keyword.asCall());
            return new ErroneousExecutableCallContext(locationUri, line, errorMsg);

        } else {
            final ExecutableWithDescriptor elem = elements.get(index);
            if (elem.isLoopExecutable() && keyword.isForLoop()) {
                if (CallChecker.isSameForLoop((ForLoopDeclarationRowDescriptor<?>) elem.getDescriptor(), keyword)) {
                    return new ExecutableCallContext(models, elements, index, locationUri, elem.getLine(),
                            breakpointSupplier);

                } else {
                    final String errorMsg = String.format(ErrorMessages.executableCallNotFound_diffFor,
                            CallChecker.createName((ForLoopDeclarationRowDescriptor<?>) elem.getDescriptor()),
                            keyword.getName());
                    return new ExecutableCallContext(models, elements, index, locationUri, elem.getLine(), errorMsg,
                            breakpointSupplier);
                }

            } else if (elem.isLoopExecutable() && !keyword.isForLoop()) {
                final String errorMsg = String.format(ErrorMessages.executableCallNotFound_foundForButCall,
                        keyword.asCall());
                return new ExecutableCallContext(models, elements, index, locationUri, elem.getLine(), errorMsg,
                        breakpointSupplier);

            } else if (!elem.isLoopExecutable() && keyword.isForLoop()) {
                final String call = elem.getCalledKeywordName();
                final String errorMsg = String.format(ErrorMessages.executableCallNotFound_foundCallButFor, call);
                return new ExecutableCallContext(models, elements, index, locationUri, elem.getLine(), errorMsg,
                        breakpointSupplier);

            } else {
                final String call = elem.getCalledKeywordName();
                if (CallChecker.isCallOf(call, keyword)) {
                    return new ExecutableCallContext(models, elements, index, locationUri, elem.getLine(),
                            breakpointSupplier);
                } else {
                    final String errorMsg = String.format(ErrorMessages.executableCallNotFound_diffCall,
                            keyword.asCall(), call);
                    return new ExecutableCallContext(models, elements, index, locationUri, elem.getLine(), errorMsg,
                            breakpointSupplier);
                }
            }
        }
    }
}

/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.assist.RedSettingProposals.SettingTarget;
import org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposal.VariableOrigin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.io.Files;

public class AssistProposals {

    static RedSectionProposal createSectionProposal(final String sectionName, final ProposalMatch match) {
        return new RedSectionProposal("*** " + sectionName + " ***", match);
    }

    static RedFileLocationProposal createFileLocationProposal(final IFile fromFile, final IFile toFile,
            final ProposalMatch match) {
        final String label = toFile.getFullPath().makeRelative().toString();
        final String content = createCurrentFileRelativePath(fromFile, toFile);
        return new RedFileLocationProposal(content, label, toFile, match);
    }

    private static String createCurrentFileRelativePath(final IFile from, final IFile to) {
        return to.getLocation().makeRelativeTo(from.getLocation()).removeFirstSegments(1).toString();
    }

    static RedLibraryProposal createLibraryProposal(final RobotSuiteFile suiteFile,
            final LibrarySpecification libSpec, final ProposalMatch match) {

        final String libraryName = libSpec.getName();
        final List<String> arguments = new ArrayList<>();
        if (libSpec.isRemote()) {
            arguments.add(libSpec.getSecondaryKey());
        }
        final boolean isImported = suiteFile.getImportedLibraries().containsKey(libSpec);
        return new RedLibraryProposal(libraryName, arguments, isImported, libSpec.getDocumentation(), match);
    }

    static RedKeywordProposal createLibraryKeywordProposal(final LibrarySpecification spec,
            final KeywordSpecification keyword, final String bddPrefix, final KeywordScope scope,
            final String sourcePrefix, final IPath exposingFilepath,
            final Predicate<RedKeywordProposal> shouldUseQualified, final ProposalMatch match) {

        final ArgumentsDescriptor argsDescriptor = keyword.createArgumentsDescriptor();
        return new RedKeywordProposal(spec.getName(), sourcePrefix, scope, bddPrefix, keyword.getName(),
                argsDescriptor, keyword.getDocumentation(), keyword.isDeprecated(), exposingFilepath,
                shouldUseQualified, match);
    }

    static RedKeywordProposal createUserKeywordProposal(final RobotKeywordDefinition userKeyword,
            final String bddPrefix, final KeywordScope scope, final String sourcePrefix,
            final Predicate<RedKeywordProposal> shouldUseQualified, final ProposalMatch match) {

        final RobotSuiteFile file = userKeyword.getSuiteFile();
        final ArgumentsDescriptor argsDescriptor = userKeyword.createArgumentsDescriptor();
        return new RedKeywordProposal(Files.getNameWithoutExtension(file.getFile().getName()), sourcePrefix, scope,
                bddPrefix, userKeyword.getName(), argsDescriptor, userKeyword.getDocumentation(),
                userKeyword.isDeprecated(), file.getFile().getFullPath(), shouldUseQualified, match);
    }

    static RedCodeReservedWordProposal createCodeReservedWordProposal(final String word, final ProposalMatch match) {
        return new RedCodeReservedWordProposal(word, match);
    }

    static RedImportProposal createResourceImportInCodeProposal(final String nameToUse, final String bddPrefix,
            final Optional<ProposalMatch> match) {
        return new RedImportProposal(nameToUse, bddPrefix, ModelType.RESOURCE_IMPORT_SETTING, match.get());
    }

    static RedImportProposal createLibraryImportInCodeProposal(final String nameToUse, final String bddPrefix,
            final Optional<ProposalMatch> match) {
        return new RedImportProposal(nameToUse, bddPrefix, ModelType.LIBRARY_IMPORT_SETTING, match.get());
    }

    static RedSettingProposal createSettingProposal(final String settingName, final SettingTarget target,
            final ProposalMatch match) {
        return new RedSettingProposal(settingName, target, match);
    }

    static AssistProposal createNewVariableProposal(final VariableType type) {
        switch (type) {
            case SCALAR:
                return new RedNewVariableProposal("${newScalar}", type, new ArrayList<String>(),
                        RedImages.getRobotScalarVariableImage(), "Fresh scalar", "Creates fresh scalar variable");
            case LIST:
                return new RedNewVariableProposal("@{newList}", type, newArrayList("item"),
                        RedImages.getRobotListVariableImage(), "Fresh list", "Creates fresh list variable");
            case DICTIONARY:
                return new RedNewVariableProposal("&{newDict}", type, newArrayList("key=value"),
                        RedImages.getRobotDictionaryVariableImage(), "Fresh dictionary",
                        "Creates fresh dictionary variable");
            default:
                throw new IllegalStateException("Variable type not supported");
        }
    }

    static RedVariableProposal createUserVariableProposal(final RobotVariable robotVariable,
            final ProposalMatch match) {
        final String varName = robotVariable.getPrefix() + robotVariable.getName() + robotVariable.getSuffix();
        final IFile file = robotVariable.getSuiteFile().getFile();
        final String sourceName = file != null ? file.getFullPath().toString() : robotVariable.getSuiteFile().getName();
        final String value = robotVariable.getValue();
        final String comment = robotVariable.getComment();
        return new RedVariableProposal(varName, sourceName, value, comment, VariableOrigin.LOCAL, match);
    }

    static RedVariableProposal createLocalVariableProposal(final String name, final String path,
            final ProposalMatch match) {
        final String actualName = name.contains("}=") ? name.substring(0, name.indexOf("}=") + 1) : name;
        return new RedVariableProposal(actualName, path, "", "", VariableOrigin.LOCAL, match);
    }

    static RedVariableProposal createVarFileVariableProposal(final String name, final String value, final String path,
            final ProposalMatch match) {
        return new RedVariableProposal(name, path, value, "", VariableOrigin.IMPORTED, match);
    }

    static RedVariableProposal createBuiltInVariableProposal(final String name, final String value,
            final ProposalMatch match) {
        return new RedVariableProposal(name, "Built-in", value, "", VariableOrigin.BUILTIN, match);
    }

    public static Comparator<AssistProposal> sortedByLabels() {
        return new Comparator<AssistProposal>() {

            @Override
            public int compare(final AssistProposal proposal1, final AssistProposal proposal2) {
                return proposal1.getLabel().compareToIgnoreCase(proposal2.getLabel());
            }
        };
    }

    public static Comparator<RedVariableProposal> sortedByOriginAndNames() {
        return new Comparator<RedVariableProposal>() {

            @Override
            public int compare(final RedVariableProposal proposal1, final RedVariableProposal proposal2) {
                if (proposal1.getOrigin() == proposal2.getOrigin()) {
                    return proposal1.getLabel().compareToIgnoreCase(proposal2.getLabel());
                } else {
                    return proposal1.getOrigin().compareTo(proposal2.getOrigin());
                }
            }
        };
    }

    public static Comparator<RedLibraryProposal> sortedByLabelsNotImportedFirst() {
        return new Comparator<RedLibraryProposal>() {

            @Override
            public int compare(final RedLibraryProposal proposal1, final RedLibraryProposal proposal2) {
                final int result = Boolean.compare(proposal1.isImported(), proposal2.isImported());
                if (result != 0) {
                    return result;
                }
                return proposal1.getLabel().compareToIgnoreCase(proposal2.getLabel());
            }
        };
    }
}

package org.robotframework.ide.core.testData.model.table.settings.library;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.common.Argument;
import org.robotframework.ide.core.testData.model.table.settings.AExternalImported;
import org.robotframework.ide.core.testData.model.table.settings.ImportElementLocation;
import org.robotframework.ide.core.testData.model.table.settings.AExternalImported.ImportTypes;


public class Library extends AExternalImported {

    private final ImportedLibrary libraryWord;
    private List<Argument> initialArguments = new LinkedList<>();
    private LibraryAliasDeclaration aliasWords;
    private LibraryAlias alias;


    public Library(final ImportedLibrary libraryWord,
            final ImportElementLocation location) {
        super(ImportTypes.LIBRARY, location);
        this.libraryWord = libraryWord;
    }
}

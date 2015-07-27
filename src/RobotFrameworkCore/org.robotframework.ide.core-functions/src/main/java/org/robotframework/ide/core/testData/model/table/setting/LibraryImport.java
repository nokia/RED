package org.robotframework.ide.core.testData.model.table.setting;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class LibraryImport extends AImported {

    private final RobotToken libraryDeclaration;
    private RobotToken libraryNameOrPathToFile;
    private final List<RobotToken> arguments = new LinkedList<>();
    private LibraryAlias alias;
    private final List<RobotToken> comment = new LinkedList<>();


    public LibraryImport(final RobotToken libraryDeclaration) {
        this.libraryDeclaration = libraryDeclaration;
    }


    public RobotToken getLibraryNameOrPathToFile() {
        return libraryNameOrPathToFile;
    }


    public void setLibraryNameOrPathToFile(RobotToken libraryNameOrPathToFile) {
        this.libraryNameOrPathToFile = libraryNameOrPathToFile;
    }


    public LibraryAlias getAlias() {
        return alias;
    }


    public void setAlias(LibraryAlias alias) {
        this.alias = alias;
    }


    public RobotToken getLibraryDeclaration() {
        return libraryDeclaration;
    }


    public List<RobotToken> getArguments() {
        return arguments;
    }


    public void addArgument(final RobotToken argument) {
        this.arguments.add(argument);
    }


    public List<RobotToken> getComment() {
        return comment;
    }


    public void addCommentPart(final RobotToken rt) {
        this.comment.add(rt);
    }


    @Override
    public boolean isPresent() {
        return true; // TODO: check if correct imported
    }
}

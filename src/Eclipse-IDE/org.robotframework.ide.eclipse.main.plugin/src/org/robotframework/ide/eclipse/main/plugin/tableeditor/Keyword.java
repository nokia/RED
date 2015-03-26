package org.robotframework.ide.eclipse.main.plugin.tableeditor;


public class Keyword {

    private String name;
    private String arg1;
    private String arg2;
    private int argsNumber;
    
    public Keyword(String name, int argsNum, String arg1, String arg2) {
        super();
        this.name = name;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.argsNumber = argsNum;
    }

    
    public Keyword(String name, int argsNum, String arg1) {
        this.name = name;
        this.arg1 = arg1;
        this.argsNumber = argsNum;
    }


    public String getName() {
        return name;
    }

    
    public void setName(String name) {
        this.name = name;
    }

    
    public String getArg1() {
        return arg1;
    }

    
    public void setArg1(String arg1) {
        this.arg1 = arg1;
    }

    
    public String getArg2() {
        return arg2;
    }

    
    public void setArg2(String arg2) {
        this.arg2 = arg2;
    }
    

    
    public int getArgsNumber() {
        return argsNumber;
    }


    
    public void setArgsNumber(int argsNumber) {
        this.argsNumber = argsNumber;
    }


}

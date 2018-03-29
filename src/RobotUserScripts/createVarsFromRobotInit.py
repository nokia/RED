#/*
#* Copyright 2018 Nokia Solutions and Networks
#* Licensed under the Apache License, Version 2.0,
# * see license.txt file for details.
# */
import os
import re
import sys

if len(sys.argv)!=3:
    print('Script to create global variable file from robot suite folder and subfolders when variables are created by "set global variable" keyword.')
    print("Usage: create_vars_from_init.py <path to folder> <path to output file>")
    sys.exit(1)
projectPath=os.path.normpath(sys.argv[1])  
globalVarsFile=os.path.normpath(sys.argv[2]) 
initFiles=['__init__.txt','__init__.robot',"__init__.tsv"]
globalVars=''


for root, directories, filenames in os.walk(projectPath):
    for filename in filenames:
        if filename in initFiles:
            found=False
            robotInit = open(root+'/'+filename,'r')
            for line in robotInit:
                if 'set global variable' in line.lower():
                    if found==False:
                        globalVars+='#filename: '+os.path.join(root,filename)+'\n'
                    found=True
                    vars=re.split('\s{2,}|\t',line)
                    vars = list(filter(None, vars))
                    if vars[1][0]=='$':
                        globalVars += vars[1][2:-2]+'=""\n'
                    if vars[1][0]=='&':
                        globalVars += vars[1][2:-2]+'={}\n'
                    if vars[1][0]=='@':
                        globalVars += vars[1][2:-2]+'=[]\n'
            robotInit.close()


output=open(globalVarsFile,'w')
for line in globalVars:
    output.write(line)
print('global variable from __init__.txt/tsv/robot saved to '+globalVarsFile)
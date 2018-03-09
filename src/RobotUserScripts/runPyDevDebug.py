#/*
#* Copyright 2017 Nokia Solutions and Networks
#* Licensed under the Apache License, Version 2.0,
# * see license.txt file for details.
# */

# Check RED Robot Editor help for detailed help
# Windows -> Help contents -> RED Robot Editor User Guide -> User guide -> Launching Tests -> Debugging Robot&Python with RED&PyDev

# PyDev needed together with RED on the same Eclipse instance,
# Set PyDev nature to RED project: right click on Project, from PyDev menu: Set as PyDev project
# check paths to pydevd - either from PyDev Eclipse subfolder or by installing with pip
# check paths to robot\run.py - it should be located in local interpreter under Lib\\site-packages\\robot\\run.py
# check ports and ip for PyDev Remote Debug Server
# steps:
# set breakpoints,
# start PyDev remote debug server from Debug perspective (Remote debug server can be running constantly - check PyDev Preferences under PyDev/Debug)
# run Robot Debug configuration with this script



import os,sys,re
import subprocess
#import pydevd

def run_process(command):
    print("Running command: " + command)
    p = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE)

    while True:
        if sys.version_info >= (3, 0):
            nextline = str(p.stdout.readline(),"utf-8")
        else:
            nextline = p.stdout.readline()
        if nextline == '' and p.poll() is not None:
            break
        sys.stdout.write(nextline)
        sys.stdout.flush()



def LocalPythonDebug(pathToPyDevD,pydevdIp,pydevdPort):


    #try to get run.py path from local Python interpreter to run Robot tests
    try: # try to guess run.py path from local robot installation
        import robot
        import inspect
        path=inspect.getfile(robot)
    except Exception as inst:
        # if not found use user defined
        runPath = 'c:\\Python36\\Lib\\site-packages\\robot\\run.py'

    path=path.replace('\\','/').split('/')
    path.pop()
    path='/'.join(path)
    runPath=path+'/run.py'

    if not os.path.isfile(runPath):
        print('run.py file not found under following path:'+runPath)

    #try to find pydevd either from installed module or from pydevdPath
    try:
        # if pydevd is installed from pip or in PythonPath, pydevdPath can be shortened to string: -m pydevd
        import pydevd
        pydevdPath = '-m pydevd'
    except Exception as inst:
        # if pydevd module not found,use user defined pydevd src
        pydevdPath = pathToPyDevD
        if not os.path.isfile(pydevdPath):
            print('pydevd.py file not found under following path:' + pydevdPath)
            exit()

    pydevdArgs='--multiprocess --print-in-debugger-startup --vm_type python --client '+pydevdIp+' --port '+pydevdPort+' --file '+runPath

    argumentList = sys.argv
    argumentList=['"' + arg_item + '"' for arg_item in argumentList] #wrap argument lists in quotes to avoid problems with spaces in paths 
    argumentList.pop(0) # remove path to current interpreter
    argumentList[1]='-u' #Force stdin, stdout and stderr to be totally unbuffered. On systems where it matters, also put stdin, stdout and stderr in binary mode.
    argumentList.insert(2,pydevdPath+' '+pydevdArgs)

    argumentList.pop(3)  # remove robot.run from command
    command = ' '.join(argumentList)
    run_process(command)



if __name__ == "__main__":


    pathToPyDevD='change this to reflect to pydevd.py file, check your local Eclipse/Red with installed PyDev <eclipse_or_RED_with_PyDev>/plugins/org.python.pydev/pysrc/pydevd.py'
    pydevdIp = '127.0.0.1'
    pydevdPort = '5678'

    LocalPythonDebug(pathToPyDevD,pydevdIp,pydevdPort)


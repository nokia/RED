#/*
#* Copyright 2017 Nokia Solutions and Networks
#* Licensed under the Apache License, Version 2.0,
# * see license.txt file for details.
# */

import argparse
import os
import paramiko
import select
import sys
import zipfile

def mkdir_p(sftp, remote_directory):
    """Change to this directory, recursively making new folders if needed.
    Returns True if any folders were created.
    https://stackoverflow.com/questions/14819681/upload-files-using-sftp-in-python-but-create-directories-if-path-doesnt-exist
    """
    if remote_directory == '/':
        # absolute path so change directory to root
        sftp.chdir('/')
        return
    if remote_directory == '':
        # top-level relative directory must exist
        return
    try:
        sftp.chdir(remote_directory) # sub-directory exists
    except IOError:
        dirname, basename = os.path.split(remote_directory.rstrip('/'))
        mkdir_p(sftp, dirname) # make parent directories
        sftp.mkdir(basename) # sub-directory missing, so created it
        sftp.chdir(basename)
        return True

def copyToRemote(ssh,localpath,remotepath):
    sftp=ssh.open_sftp()
    excludeExt=['.libspec']
    excludeFiles=['red.xml','output.xml','log.html','.project','report.html']

    #create temp zip file with remote tests in local folder where script is located
    zipArchivePath='remote-tests.zip'
    zipf = zipfile.ZipFile(zipArchivePath, 'w', zipfile.ZIP_DEFLATED)

    for root, directories, filenames in os.walk(localpath):
        for filename in filenames:
            if not filename.endswith(tuple(excludeExt))  and filename not in excludeFiles:
                root = root.replace('\\', '/')
                relativepath = root.replace(localpath, '')+'/'
                zipf.write(localpath+relativepath+filename,relativepath+filename)
    zipf.close()
    mkdir_p(sftp, remotepath)
    sys.__stdout__.write('Copying zip file to:' + remotepath)
    sys.stdout.flush()
    sftp.put(zipArchivePath,remotepath+'remote-tests.zip')
    sftp.close()

def unzipOnRemote(ssh,remotepath):
    sys.__stdout__.write('Unzip ' + remotepath+'remote-tests.zip into '+remotepath)
    sys.stdout.flush()
    runSshCommand(ssh,'unzip -o '+remotepath+'remote-tests.zip -d '+remotepath+';rm -rf '+remotepath+'remote-tests.zip')

def runRemoteRobot(ssh,command):
    runSshCommand(ssh, command)

def zipReports(ssh,archivePath):

    command='cd '+archivePath+';rm -rf reports.zip;zip reports.zip '
    files=['output.xml','log.html','report.html']
    for file in files:
        command+=file+' '
    runSshCommand(ssh, command)

def getReportsFromRemote(ssh,archivePath,localFile):
    sftp = ssh.open_sftp()
    sftp.get(archivePath, localFile)
    sftp.close()

def unzipReports(archivePath,archiveFilename):
    zip_ref = zipfile.ZipFile(archivePath+archiveFilename, 'r')
    zip_ref.extractall(archivePath)
    zip_ref.close()
    os.remove(archivePath+archiveFilename)

def printReportsPath(localpath):
    #RED converts links to output files to clickable urls in Console view
    #Let's print links to reports which were copied to localpath folder
    sys.__stdout__.write('Output:  '+localpath+'output.xml\n')
    sys.__stdout__.write('Log:     '+localpath + 'log.html\n')
    sys.__stdout__.write('Report:  '+ localpath + 'report.html\n')
    sys.stdout.flush()
def runSshCommand(ssh,command):
    channel = ssh.get_transport().open_session()
    channel.exec_command(command)


    while True:
        if channel.exit_status_ready():
            break
        rl, wl, xl = select.select([channel], [], [], 0.0)
        if len(rl) > 0:
            command_output = (channel.recv(48).decode('utf-8'))
            sys.__stdout__.write(command_output)
            sys.stdout.flush()


parser = argparse.ArgumentParser()
requiredNamed = parser.add_argument_group('required parameters')
requiredNamed.add_argument('-s','--host', help='remote host address',required=True)
requiredNamed.add_argument('-u','--username', help='username to login to remote host',required=True)
requiredNamed.add_argument('-r','--remotepath', help='path on remote host where robot suite will be copied and executed',required=True)
requiredNamed.add_argument('-c','--robotcommand', help='robot command to be executed',nargs=argparse.REMAINDER,required=True)

group = parser.add_mutually_exclusive_group()
group.add_argument('-p','--password', help='password to login to remote host')
group.add_argument('-k','--keyfile', help='path to keyfile to login to remote host')

parser.add_argument('-l','--localpath', help='Local path to robot files/folder')
parser.add_argument('-i','--remoteinterpreter', default="python",help='command to start python interpreter on remote host.Default: "python"')

args = parser.parse_args()
server=args.host
username=args.username
password=args.password

if args.keyfile:
    key=paramiko.RSAKey.from_private_key_file(args.keyfile.replace('\\','/'))
else:
    key=None
remotepath=args.remotepath.replace('\\','/')+'/'
remoteinterpreter=args.remoteinterpreter
if len(args.robotcommand)>1:
   robotcommand=args.robotcommand
else:
   robotcommand=args.robotcommand.split()

if args.localpath:
    localpath=args.localpath.replace('\\','/')+'/'
else:
    localpath=robotcommand[-1].replace('\\','/')+'/'

robotcommand[0]=remoteinterpreter
robotcommand[3]='' # remove --listener parameter, #FIXME add listener file locally and copy it together with other files
robotcommand[4]='' # remove path to listener, #FIXME change listener path to remotepath
robotcommand[-1]=remotepath
robotcommand=' '.join(robotcommand)
robotcommand='cd '+remotepath+';'+robotcommand


ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.load_host_keys(os.path.expanduser(os.path.join("~", ".ssh", "known_hosts")))
ssh.connect(server, username=username, password=password,pkey=key)

copyToRemote(ssh,localpath,remotepath)
unzipOnRemote(ssh,remotepath)
runRemoteRobot(ssh,robotcommand)
zipReports(ssh,remotepath)
getReportsFromRemote(ssh,remotepath+'reports.zip',localpath+'reports.zip')
unzipReports(localpath,'reports.zip')
printReportsPath(localpath)
ssh.close()

sys.__stdout__.write('Remote execution completed')
sys.stdout.flush()
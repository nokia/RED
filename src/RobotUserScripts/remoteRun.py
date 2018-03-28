import os
import paramiko
import select
import sys
import zipfile

def mkdir_p(sftp, remote_directory):
    """Change to this directory, recursively making new folders if needed.
    Returns True if any folders were created."""
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
    print(archivePath)
    print(localFile)
    sftp.get(archivePath, localFile)
    sftp.close()

def unzipReports(archivePath,archiveFilename):
    zip_ref = zipfile.ZipFile(archivePath+archiveFilename, 'r')
    zip_ref.extractall(archivePath)
    zip_ref.close()
    os.remove(archivePath+archiveFilename)

def runSshCommand(ssh,command):
    channel = ssh.get_transport().open_session()
    channel.exec_command(command)
    # console_buffer=''
    # as output is non buffered on server, small buffer needs to be done on client side.
    # it is to ensure properly printing recv buffers with \n in the middle of string

    while True:
        if channel.exit_status_ready():
            break
        rl, wl, xl = select.select([channel], [], [], 0.0)
        if len(rl) > 0:
            command_output = (channel.recv(48).decode('utf-8'))
            sys.__stdout__.write(command_output)
            sys.stdout.flush()
            # if '\n' not in command_output:
            #     console_buffer += command_output
            # else:
            #     index=command_output.find('\n')
            #     sys.__stdout__.write(console_buffer+command_output[0:index]+'\n')
            #     sys.stdout.flush()
            #     console_buffer=command_output[index+1:]

server=sys.argv[1]
username=sys.argv[2]
password=sys.argv[3]

# use data model path for local folder as it is path to project
# if Launching preference is set to send robot command as one string,this param needs to be used

remotepath=sys.argv[4]+'/'
remoteinterpreter=sys.argv[5]
remoteinterpreter='python -u'

if len(sys.argv)>7:
    robotcommand=sys.argv[6:]
else:
    robotcommand=sys.argv[7].split()
localpath=robotcommand[-1].replace('\\','/')+'/'
robotcommand[0]=remoteinterpreter
robotcommand[3]='' # remove --listener parameter, #FIXME
robotcommand[4]='' # remove path to listener, #FIXME
robotcommand[-1]=remotepath
robotcommand=' '.join(robotcommand)
robotcommand='cd '+remotepath+';'+robotcommand

print('server',server)
print('username',username)
print('password',password)
print('localpath',localpath)
print('remotepath',remotepath)
print('remoteinterpreter',remoteinterpreter)
print('robotcommand',robotcommand)

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())

ssh.load_host_keys(os.path.expanduser(os.path.join("~", ".ssh", "known_hosts")))
ssh.connect(server, username=username, password=password)

copyToRemote(ssh,localpath,remotepath)
unzipOnRemote(ssh,remotepath)
runRemoteRobot(ssh,robotcommand)
zipReports(ssh,remotepath)
getReportsFromRemote(ssh,remotepath+'reports.zip',localpath+'reports.zip')
unzipReports(localpath,'reports.zip')

ssh.close()
sys.__stdout__.write('Remote execution completed')
sys.stdout.flush()


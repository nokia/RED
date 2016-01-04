'''
Created on 25-05-2015

* Copyright 2015 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.

'''
import os
import re
import random
import string 
import sys
import mmap
from fileinput import close


if len(sys.argv)<2:
    print "Usage: script PathToRcpttRootTests TagFromTestToIncludeInSuite \nIf no Tag is given, testSuite will contain all tests from given directory tree.\nTag AllTestsWithoutTags will generate suite with tests which does not contains any tags\nNot enough parameters." 
    exit()
path=sys.argv[1]
if len(sys.argv)==2:
    tag=''
else:
    tag=sys.argv[2]
print 'tag:'+tag+'#'


TestCaseId=''
matches=[]

# get the files with tag in Tags line
for root, dirnames, filenames in os.walk(path):

    for file in filenames:
        TestCaseId=''

        if re.search('\.test$',file) is not None:

            with open(root+'/'+file,"r+") as testfile:
                
                for lines in testfile:
                    if re.search('Id.*',lines) is not None:
                        TestCaseId= re.split('Id: ',lines)
                        TestCaseId=TestCaseId[1].strip('\n')
        
            
            with open(root+'/'+file,"r+") as testfile:
                data = mmap.mmap(testfile.fileno(), 0)
            
            
                if tag!='':
                    if tag == 'AllTestsWithoutTags':
                        if re.search('Save-Time:.*\nTags: .*\nTestcase-Type:', data) is None:
                            matches.append([file,root+'/'+file,TestCaseId])
                          
                    else:
                        if re.search('Save-Time:.*\nTags: '+tag+ '\nTestcase-Type:',data) is not None:
                            matches.append([file,root+'/'+file,TestCaseId])
                            
                        
                else:
                    
  
                    matches.append([file,root+'/'+file,TestCaseId])
                    TestCaseId=''
                    
            
# Create suite with found tests, generate RCPTT ID (internal magic)
random_testcase_items=''
random_testcase_items=''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(8))+'-'+''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(4))+'-'+''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(4))+'-'+''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(4))+'-'+''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(12))
random_id=''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(22))

if tag=='':
    tag="AllTests"

header='''--- RCPTT testcase ---
Format-Version: 1.0
Element-Name: '''+tag+'_TestSuite'+'''
Element-Type: testsuite
Element-Version: 2.0
Id: _'''+random_id+'''
Runtime-Version: 1.5.5.201503020312
Save-Time: 5/20/15 8:47 AM

------=_testcase-items-'''+random_testcase_items+'''
Content-Type: text/testcase
Entry-Name: testcase-items\n\n'''


footer="------=_testcase-items-"+random_testcase_items+'--'
random_string=''
output_file=open(tag+'_TestSuite.suite','w')
output=''
text=''

output+=header

for i in matches:

    i[1]=i[1].replace('\\','/')
    i[1]=i[1].replace(path,'')
    if i[1][0]=='/':
        i[1]=i[1][1:]
    text=i[2]+'\t'+"// kind: 'test' name: '"+i[0]+"' path: '"+i[1]+"'"+'\r'
    output+=text
output+=footer

output_file.write(output)


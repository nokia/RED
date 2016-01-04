'''
Created on 26-05-2015

* Copyright 2015 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.

'''
import re
import os 
import sys

if len(sys.argv)<3:
    print 'Not enough parameters'
    print 'usage: script PathToRootRcpttTests PathToHtmlReport PathToOutputHtmlReport'
    exit()
    
path_to_tests=sys.argv[1]
htmlReportFile=sys.argv[2]
output_file=sys.argv[3]

htmlReport=open(htmlReportFile,'r')
output=''


for lines in htmlReport:
    file_found = 0
    if re.search('h2 class="failure',lines) is not None:
        failedTestCase= re.split('<h2 class="failure">',lines)
        FilenameToSearch=failedTestCase[1][:-6]
        for root, dirnames, filenames in os.walk(path_to_tests):
            for file in filenames:
                if file == FilenameToSearch+'.test':
                    filepath= os.path.join(root, file)
                    lines=lines+'<h4 ><font color="#FF6666">File path: '+filepath.split(path_to_tests)[1].lstrip("\\/")+'</font></h4>'
                    file_found = 1
                    break
            if file_found==1:
                break
        else:
            sys.exit(FilenameToSearch + " was not found on disc!")
    output+=lines

output_file=open(output_file,'w')
output_file.write(output)

#
# Copyright 2016 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#
# dependencies:
# py-nilsimsa - https://github.com/diffeo/py-nilsimsa/
# Robot Framework Lint - https://github.com/boakley/robotframework-lint


try:
    from nilsimsa import Nilsimsa
except ImportError:
    print('Missing dependency py-nilsimsa package. Install: pip install py-nilsimsa \nmore info https://github.com/diffeo/py-nilsimsa')
    exit(1)
try:
    from rflint import parser
except ImportError:
    print('Missing dependency py-nilsimsa package. Install: pip install --upgrade robotframework-lint \nmore info https://github.com/boakley/robotframework-lint')
    exit(1)
import argparse
import os


class rfitem(object):
    def __init__(self,string,parent,parent_filename,lines,type):
        self.string=string                      # string dump of TC/KW from parser
        self.parent=parent                      # TC/KW name
        self.parent_filename=parent_filename    # parent filename where TC/KW is located
        self.lines=lines                        # line numbers of TC/KW
        self.type=type                          # type - KW or TC
        self.digest=Nilsimsa(self.string)       # hash

def parse_rf_file(parent_filename):
    test = parser.RobotFactory(parent_filename)
    iterate_list=[test.keywords,test.testcases]
    for k in iterate_list:
        for i in k:

            dump=''
            lines=[]
            for elements in i.steps:
                for element in elements:
                    dump+=' '+element
                    lines.append(elements.startline)
            items.append(rfitem(dump,i,parent_filename,lines,i.__module__))

arguments = argparse.ArgumentParser()
arguments.add_argument('rf_files', help="path to dir with RF tests or path to RF file",type=str)
arguments.add_argument("-out", dest='output_file', help="specify where report output should be written,(default: %(default)s)",type=str, default='output_report.txt')
arguments.add_argument("-th", dest='threshold', help="threshold which limits itmes in output report. Nilsimsa metrics are witheen -128 and +128,(default: %(default)s)",type=int, default=10)
args = arguments.parse_args()

items=[]            # stores parsed KW and TC, each item contains TC/KW name,parent file path,file lines, content dump,type
results=[]          # stores pairs of items (KW and TC) with digest metric (similarity)

threshold=args.threshold
output_file=args.output_file
rf_files=args.rf_files

print("parsing RF files")
if os.path.isfile(rf_files)==True:
    parse_rf_file(rf_files)
else:
    for root, dirnames, filenames in os.walk(rf_files):
         for filename in filenames:
             if filename.endswith(('.robot','.txt','.tsv')):
                 parent_filename=root+'/'+filename
                 parse_rf_file(parent_filename)


for i in range(0,len(items)-1):
     for j in range(i+1,len(items)):
          print ('comparing digests pairs '+str(i)+' '+str(j))
          results.append([items[i].digest.compare(items[j].digest.digest), items[i], items[j]])

results=sorted(results, reverse=True)

output_report=open(output_file,'wb')
print("writing results to file:"+str(output_file))
for i in results:
    if i[0]>threshold :
        put=str(i[0])+' '+str(i[1].parent_filename)+' '+str(i[1].parent)+' '+str(i[2].parent_filename)+' '+str(i[2].parent)+'\n'
        output_report.write(put)

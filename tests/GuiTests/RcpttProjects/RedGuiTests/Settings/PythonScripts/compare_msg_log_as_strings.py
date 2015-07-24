import re
import sys
import os

if len(sys.argv)>2:
    pattern = "^\d{8} (\d\d:){2}\d\d.\d{3}"
    path_to_log = sys.argv[1]
    path_to_ref = sys.argv[2]

    ref_content =  re.sub( pattern, "", path_to_log,flags=re.MULTILINE)
    org_content =  re.sub( pattern, "", path_to_ref,flags=re.MULTILINE)


    if org_content == ref_content:
        print "OK!"
    else:
        ref_content = ref_content.split("\n")
        org_content = org_content.split("\n")


        if len(ref_content)>len(org_content):
            org_content.append("EOF")
        if len(ref_content)<len(org_content):
            ref_content.append("EOF")

        for i in range(len(ref_content)):
            if ref_content[i]!=org_content[i]:
                print "Different line: " + str(i+1)
                print "Should be:" + ref_content[i]
                print "But is:" + org_content[i]
                break


else:
    print "Not enough arguments!"
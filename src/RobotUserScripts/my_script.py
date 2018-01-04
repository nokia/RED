#/*
#* Copyright 2017 Nokia Solutions and Networks
#* Licensed under the Apache License, Version 2.0,
# * see license.txt file for details.
# */

import sys
from io import StringIO
from subprocess import Popen, PIPE

print('##########')
print('Running Robot tests via script!')
print('##########')
sys.stdout.flush()

execution = Popen(sys.argv[1:])
execution.communicate()
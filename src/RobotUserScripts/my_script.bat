::
:: Copyright 2018 Nokia Solutions and Networks
:: Licensed under the Apache License, Version 2.0,
:: see license.txt file for details.
::

echo command line from RED
echo %*
echo removing script name with SHIFT command
SHIFT
echo running Robot
%*
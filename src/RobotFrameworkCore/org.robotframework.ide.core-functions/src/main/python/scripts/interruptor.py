import sys
from ctypes import windll
from signal import CTRL_C_EVENT

def _check_result(result):
    if result == 0:
        print('Invalid call result')
        sys.exit(1)

if __name__ == '__main__':
    pid = int(sys.argv[1])
    kernel = windll.kernel32
    
    result = kernel.FreeConsole()
    _check_result(result)
    
    result = kernel.AttachConsole(pid)
    _check_result(result)

    result = kernel.SetConsoleCtrlHandler(None, True)
    _check_result(result)
    
    result = kernel.GenerateConsoleCtrlEvent(CTRL_C_EVENT, 0)
    _check_result(result)
    
    result = kernel.SetConsoleCtrlHandler(None, False)
    _check_result(result)

    sys.exit(0)
try:
    import robot.running.namespace
    print('\n'.join(robot.running.namespace.STDLIB_NAMES))
except:
    # the std libraries set was moved to other place since Robot 2.9.1
    import robot.libraries
    print ('\n'.join(robot.libraries.STDLIBS)) 
try:
    import robot.running.namespace
    tab=(sorted(robot.running.namespace.STDLIB_NAMES))
    tab.remove("Remote")
    print " ".join(tab)
except:
    # the std libraries set was moved to other place since Robot 2.9.1
    import robot.libraries
    tab = join(robot.libraries.STDLIBS)
    tab.remove("Remote")
    print " ".join(tab)


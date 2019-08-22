class dynamic(object):

    ROBOT_LIBRARY_VERSION = 1.0

    def __init__(self, a=1):
        self.a = int(a)

    def get_keyword_names(self):
        return ['Keyword ' + str(i) for i in range(self.a)]

    def run_keyword(self, name, args):
        print("Running keyword '%s' with arguments %s from library (%s)." % (name, args, self.a))
        
    def get_keyword_documentation(self, name):
        return 'documentation'

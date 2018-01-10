import os
import sys
import time
import traceback
import socket
import json

from rflint import RfLint

class RedRfLint(RfLint):
    
    def __init__(self, client):
        super(RedRfLint, self).__init__()
        self._client = client
        self._total_files = None
        
    def _process_file(self, filename):
        self.count_files(filename)
        
        self._client.send_to_server('file_processing_started', filename)
        super(RedRfLint, self)._process_file(filename)
        self._client.send_to_server('file_processing_ended', filename)

    def _process_folder(self, path):
        # overridden the bug in rflint which causes the tree to be traversed too many times
        self.count_files(path)
        
        for entry in sorted(os.listdir(path)):
            whole_path = os.path.join(path, entry)
            if os.path.isfile(whole_path) and self.__is_robot_file(entry):
                self._process_file(os.path.join(path, entry))
            elif os.path.isdir(whole_path) and self.args.recursive:
                self._process_folder(os.path.join(path, entry))
        
    def count_files(self, path):
        if self._total_files is None:
            self._total_files = self._count_files(path)
            self._client.send_to_server('files_to_process', self._total_files)
        
    def _count_files(self, path):
        if os.path.isfile(path):
            return 1
        
        total = 0
        for root, dirs, files in os.walk(path):
            total += len([f for f in files if self.__is_robot_file(f)])
            if not self.args.recursive:
                break
        return total
    
    def __is_robot_file(self, filepath):
        _, ext = os.path.splitext(filepath)
        return ext.lower() in (".robot", ".txt", ".tsv")

    def report(self, linenumber, filename, severity, message, rulename, char):
        self._client.send_to_server('violation_found', 
                {'filepath' : filename, 'line' : linenumber, 'character' : char, 
                    'rule_name' : rulename, 'severity' : severity, 'message' : message})

        try:
            super(RedRfLint, self).report(linenumber, filename, severity, message, rulename, char)
        except:
            # call to super may have problems with encoding performed when printing to stdout
            # we don't need this printing anyway, so just swallow the exception
            pass


class JsonClient(object):
    
    CONNECTION_SLEEP_BETWEEN_TRIALS = 2
    
    def __init__(self):
        self._is_connected = False
    
    def connect(self, host, port, connection_timeout):
        self._is_connected, self.sock, self.encoder = self._connect(host, port, connection_timeout)
    
    def _connect(self, host, port, connection_timeout):
        '''Establish a connection for sending data'''
        trials = 1
        start = time.time()
        
        while int(time.time() - start) < connection_timeout:
            try:
                sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                sock.connect((host, port))
                return True, sock, MessagesEncoder(sock)
            except socket.error as e:
                print('TestRunnerAgent: connection trial #%s failed' % trials)
                print('\tUnable to open socket to "%s:%s"'  % (host, port))
                print('\terror: %s' % str(e))
                time.sleep(self.CONNECTION_SLEEP_BETWEEN_TRIALS)
            trials += 1
        return False, None, None
    
    def send_to_server(self, name, *args):
        try:
            if self._is_connected:
                packet = {name: args}
                self.encoder.write(packet)
        except Exception:
            traceback.print_exc(file=sys.stdout)
            sys.stdout.flush()
            raise

    def close_connection(self):
        if self._is_connected:
            self._mode = None
            self._is_connected = False
            
            self.encoder.close()
            self.encoder = None
            
            self.sock.close()           
            self.sock = None

class MessagesEncoder(object):
    
    def __init__(self, sock):
        self._string_encoder = (lambda s : s) if sys.version_info < (3, 0, 0) else (lambda s : bytes(s, 'UTF-8'))
        self._json_encoder = json.JSONEncoder(separators=(',', ':'), sort_keys=True).encode
        # IronPython does not return right object type if not binary mode
        self._file_to_write = sock.makefile('wb')

    def write(self, obj):
        self._write(self._json_encoder(obj))
        
    def _write(self, json_encoded_obj):
        if self._file_to_write is None:
            return
        json_string = json_encoded_obj + '\n'
        self._file_to_write.write(self._string_encoder(json_string))
        self._file_to_write.flush()

    def close(self):
        if self._file_to_write is not None:
            self._file_to_write.close()

def run_analysis(host, port, args):
    client = JsonClient()
    try:
        client.connect(host, port, 30)
        
        result = RedRfLint(client).run(__decode_args_if_needed(args))
        client.send_to_server('analysis_finished')
        client.close_connection()
        return result
    except Exception as e:
        client.send_to_server('analysis_finished', traceback.format_exc())
        client.close_connection()
        return 1
    
def __decode_args_if_needed(args):
    if sys.version_info < (3, 0, 0):
        return [arg.decode('utf-8') for arg in args]
    else:
        return args
    
if __name__ == "__main__":
    host = sys.argv[1]
    port = int(sys.argv[2])
    run_analysis(host, port, sys.argv[3:])
    
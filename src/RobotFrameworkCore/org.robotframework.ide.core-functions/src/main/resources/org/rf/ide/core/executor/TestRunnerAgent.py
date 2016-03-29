# Copyright 2010 Orbitz WorldWide
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Modified by Mikko Korpela under NSN copyrights
#  Copyright 2008-2012 Nokia Siemens Networks Oyj
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

# Ammended by Timothy Alexander <dragonfyre13@gmail.com>
# (StreamHandler class added)
#   Copyright 2013 Timothy Alexander
#   Licensed under the Apache License, Version 2.0
#      http://www.apache.org/licenses/LICENSE-2.0

#
# Modified by Mateusz Marzec under NSN copyrights
# Copyright 2015 Nokia Solutions and Networks
# * Licensed under the Apache License, Version 2.0,
# * see license.txt file for details.
#


'''A Robot Framework listener that sends information to a socket

This uses a custom streamhandler module, preferring json but sending either
json or pickle to send objects to the listening server. It should probably be
refactored to call an XMLRPC server.
'''

import os
import sys
import socket
import threading
import inspect
if sys.version_info < (3,0,0):
    import SocketServer as socketserver
else:
    import socketserver

from robot.running.signalhandler import STOP_SIGNAL_MONITOR
from robot.errors import ExecutionFailed

if sys.hexversion > 0x2060000:
    import json

    _JSONAVAIL = True
else:
    try:
        import simplejson as json

        _JSONAVAIL = True
    except ImportError:
        _JSONAVAIL = False

if not _JSONAVAIL:
    try:
        import com.xhaus.jyson.JysonCodec as json

        _JSONAVAIL = True
    except ImportError:
        _JSONAVAIL = False

try:
    import cPickle as pickle
except ImportError:
    import pickle as pickle

try:
    from cStringIO import StringIO
except ImportError:
    if sys.version_info < (3,0,0):
        from StringIO import StringIO
    else:
        from io import StringIO

try:
    # RF 2.7.5
    from robot.running import EXECUTION_CONTEXTS

    def _is_logged(level):
        current = EXECUTION_CONTEXTS.current
        if current is None:
            return True
        out = current.output
        if out is None:
            return True
        return out._xmllogger._log_message_is_logged(level)
except ImportError:
    # RF 2.5.6
    # RF 2.6.3
    def _is_logged(level):
        # Needs to be imported in the function as OUTPUT is not a constant
        from robot.output import OUTPUT

        if OUTPUT is None:
            return True
        return OUTPUT._xmllogger._log_message_is_logged(level)

# Setting Output encoding to UTF-8 and ignoring the platform specs
# RIDE will expect UTF-8
import robot.utils.encoding
# Set output encoding to UTF-8 for piped output streams
robot.utils.encoding.OUTPUT_ENCODING = 'UTF-8'
# RF 2.6.3 and RF 2.5.7
robot.utils.encoding._output_encoding = robot.utils.encoding.OUTPUT_ENCODING


class TestRunnerAgent:
    """Pass all listener events to a remote listener

    If called with one argument, that argument is a port
    If called with two, the first is a hostname, the second is a port
    """
    ROBOT_LISTENER_API_VERSION = 2

    def __init__(self, *args):
        self.port = int(args[0])
        HOST = "localhost"
        if len(args) >= 3:
            HOST = args[2]
        self.host = HOST
        self.sock = None
        self.filehandler = None
        self.streamhandler = None
        self._connect()
        self._send_pid()
        self._create_debugger((len(args) >= 2) and (args[1] == 'True'))
        self._create_kill_server()
        self._is_robot_paused = False
        self._is_debug_enabled = (args[1] == 'True')

    def _create_debugger(self, pause_on_failure):
        self._debugger = RobotDebugger(pause_on_failure)

    def _create_kill_server(self):
        self._killer = RobotKillerServer(self._debugger)
        self._server_thread = threading.Thread(
            target=self._killer.serve_forever)
        self._server_thread.setDaemon(True)
        self._server_thread.start()
        self._send_server_port(self._killer.server_address[1])

    def _send_pid(self):
        self._send_socket("start agent", "")
        self._send_socket("pid", os.getpid())
        try:
            variables = {}
            try:
                from robot.variables import GLOBAL_VARIABLES
                variables = GLOBAL_VARIABLES
            except ImportError:  # for robot >2.9
                from robot.conf.settings import RobotSettings
                from robot.variables.scopes import GlobalVariables
                variables = GlobalVariables(RobotSettings()).as_dict()
            
            data = {}
            for k in variables.keys():
                if not (k.startswith('${') or k.startswith('@{')):
                    key = '${' + k + '}'
                else:
                    key = k
                data[key] = str(variables[k])
            self._send_socket('global_vars','global_vars',data)
        except Exception:
            pass

    def _send_server_port(self, port):
        self._send_socket("port", port)

    def start_test(self, name, attrs):
        self._send_socket("start_test", name, attrs)

    def end_test(self, name, attrs):
        self._send_socket("end_test", name, attrs)

    def start_suite(self, name, attrs):
        self._send_socket("start_suite", name, attrs)

    def end_suite(self, name, attrs):
        self._send_socket("end_suite", name, attrs)

    def start_keyword(self, name, attrs):
        self._send_socket("start_keyword", name, attrs)
        if self._is_debug_enabled:
            self._send_vars()
        self._is_robot_paused = False
        #if self._debugger.is_breakpoint(name, attrs):
        if self._is_debug_enabled:
            if self._check_breakpoint():
                self._is_robot_paused = True
            #self._debugger.pause()
        #self._wait_for_breakpoint_unlock()
        #paused = self._debugger.is_paused()
        if self._is_robot_paused:
            self._send_socket('paused')
            self._wait_for_resume()
        #self._debugger.start_keyword()
        #if paused:
        #    self._send_socket('continue')

    def _wait_for_resume(self):
        data = ''
        while data != 'resume' and data != 'interrupt':
            data = self.sock.recv(4096).decode('utf-8')
            if self._is_debug_enabled:
                self._check_changed_variable(data)
        if data == 'interrupt':
            sys.exit()
        self._debugger.resume()

    def _send_vars(self):
        try:
            from robot.libraries.BuiltIn import BuiltIn
            vars = BuiltIn().get_variables()
            data = {}
            for k in vars.keys():
                value = vars[k]
                if not inspect.ismodule(value) and not inspect.isfunction(value) and not inspect.isclass(value):
                    try:
                        if (type(value) is list) or (isinstance(value, dict)):
                            data[k] = self.fix_unicode(value)
                        else:
                            data[k] = str(self.fix_unicode(value))
                    except:
                        data[k] = 'None'
            self._send_socket('vars','vars',data)
        except AttributeError:
            self._send_socket('error')

    def fix_unicode(self,data):
       if sys.version_info < (3,0,0) and isinstance(data, unicode):
           return data.encode('utf-8')
       elif sys.version_info >= (3,0,0) and isinstance(data, str):
           return data
       elif isinstance(data, basestring):
           return data.encode('unicode_escape')
       elif isinstance(data, dict):
           data = dict((self.fix_unicode(k), self.fix_unicode(data[k])) for k in data)
       elif isinstance(data, list):
           range_fun = xrange if sys.version_info < (3,0,0) else range
           for i in range_fun(0, len(data)):
               data[i] = self.fix_unicode(data[i])
       return data

    def _check_breakpoint(self):
        data = ''
        self._send_socket('check_condition')
        while data != 'stop' and data != 'continue' and data != 'interrupt':
            data = self.sock.recv(4096).decode('utf-8')
            if data != 'stop' and data != 'continue' and data != 'interrupt':
                self._run_keyword(data)
        if data == 'stop':
            return True
        if data == 'continue':
            return False
        if data == 'interrupt':
            sys.exit()

    def _run_keyword(self, data):
        if _JSONAVAIL:
            json_decoder = json.JSONDecoder(strict=False).decode
            try:
                condition = json_decoder(data)
                list = condition['keywordCondition']
                keywordName = list[0]
                if len(list) == 1:
                    argList = []
                else:
                    argList = list[1]
                from robot.libraries.BuiltIn import BuiltIn
                result = BuiltIn().run_keyword_and_return_status(keywordName, *argList)
                self._send_socket('condition_result', result)
            except Exception as e:
                self._send_socket('condition_error', str(e))
                pass
        self._send_socket('condition_checked')

    def _check_changed_variable(self, data):
        if _JSONAVAIL:
            json_decoder = json.JSONDecoder(strict=False).decode
            try:
                js = json_decoder(data)
                from robot.libraries.BuiltIn import BuiltIn
                vars = BuiltIn().get_variables()
                for key in js.keys():
                    if key in vars:
                        if len(js[key]) > 1:
                            from robot.libraries.Collections import Collections
                            if len(js[key]) == 2:
                                if isinstance(vars[key], dict):
                                    Collections().set_to_dictionary(vars[key],js[key][0],js[key][1])
                                else:
                                    Collections().set_list_value(vars[key],js[key][0],js[key][1])
                            else:
                                nestedList = vars[key]
                                newValue = ''
                                newValueIndex = 0
                                indexList = 1
                                for value in js[key]:
                                    if indexList < (len(js[key])-1):
                                        nestedList = Collections().get_from_list(nestedList, int(value))
                                        indexList = indexList + 1
                                    elif indexList == (len(js[key])-1):
                                        newValueIndex = int(value)
                                        indexList = indexList + 1
                                    elif indexList == len(js[key]):
                                        newValue = value
                                Collections().set_list_value(nestedList,newValueIndex,newValue)
                        else:
                            BuiltIn().set_test_variable(key, js[key][0])
            except:
                pass

    def end_keyword(self, name, attrs):
        self._send_socket("end_keyword", name, attrs)
        self._debugger.end_keyword(attrs['status'] == 'PASS')
        
    def resource_import(self, name, attributes):
        self._send_socket("resource_import", name, attributes)
        
    def library_import(self, name, attributes):
        self._send_socket("library_import", name, attributes)

    def message(self, message):
        if message['level'] in ('ERROR', 'FAIL'):
            self._send_socket("message", message)

    def log_message(self, message):
        if _is_logged(message['level']):
            self._send_socket("log_message", message)

    def log_file(self, path):
        self._send_socket("log_file", path)

    def output_file(self, path):
        self._send_socket("output_file", path)

    def report_file(self, path):
        self._send_socket("report_file", path)

    def summary_file(self, path):
        pass

    def debug_file(self, path):
        pass

    def close(self):
        self._send_socket("close")
        if self.sock:
            self.filehandler.close()
            self.sock.close()

    def _connect(self):
        '''Establish a connection for sending data'''
        try:
            self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.sock.connect((self.host, self.port))
            # Iron python does not return right object type if not binary mode
            self.filehandler = self.sock.makefile('wb')
            self.streamhandler = StreamHandler(self.filehandler)
        except socket.error as e:
            print('unable to open socket to "%s:%s" error: %s'
                  % (self.host, self.port, str(e)))
            self.sock = None
            self.filehandler = None

    def _send_socket(self, name, *args):
        try:
            if self.filehandler:
                packet = {name: args}
                self.streamhandler.dump(packet)
                self.filehandler.flush()
        except Exception:
            import traceback

            traceback.print_exc(file=sys.stdout)
            sys.stdout.flush()
            raise


class RobotDebugger(object):
    def __init__(self, pause_on_failure=False):
        self._state = 'running'
        self._keyword_level = 0
        self._pause_when_on_level = -1
        self._pause_on_failure = pause_on_failure
        self._resume = threading.Event()

    @staticmethod
    def is_breakpoint(name, attrs):
        return name == 'BuiltIn.Comment' and attrs['args'] == ['PAUSE']

    def pause(self):
        self._resume.clear()
        self._state = 'pause'

    def pause_on_failure(self, pause):
        self._pause_on_failure = pause

    def resume(self):
        self._state = 'running'
        self._pause_when_on_level = -1
        self._resume.set()

    def step_next(self):
        self._state = 'step_next'
        self._resume.set()

    def step_over(self):
        self._state = 'step_over'
        self._resume.set()

    def start_keyword(self):
        while self._state == 'pause':
            self._resume.wait()
            self._resume.clear()
        if self._state == 'step_next':
            self._state = 'pause'
        elif self._state == 'step_over':
            self._pause_when_on_level = self._keyword_level
            self._state = 'resume'
        self._keyword_level += 1

    def end_keyword(self, passed=True):
        self._keyword_level -= 1
        if self._keyword_level == self._pause_when_on_level \
                or (self._pause_on_failure and not passed):
            self._state = 'pause'

    def is_paused(self):
        return self._state == 'pause'


class RobotKillerServer(socketserver.TCPServer):
    allow_reuse_address = True

    def __init__(self, debugger):
        socketserver.TCPServer.__init__(self, ("", 0), RobotKillerHandler)
        self.debugger = debugger


class RobotKillerHandler(socketserver.StreamRequestHandler):
    def handle(self):
        data = self.request.makefile('r').read().strip()
        if data == 'kill':
            self._signal_kill()
        elif data == 'pause':
            self.server.debugger.pause()
        elif data == 'resume':
            self.server.debugger.resume()
        elif data == 'step_next':
            self.server.debugger.step_next()
        elif data == 'step_over':
            self.server.debugger.step_over()
        elif data == 'pause_on_failure':
            self.server.debugger.pause_on_failure(True)
        elif data == 'do_not_pause_on_failure':
            self.server.debugger.pause_on_failure(False)

    @staticmethod
    def _signal_kill():
        try:
            STOP_SIGNAL_MONITOR(1, '')
        except ExecutionFailed:
            pass


# NOTE: Moved to bottom of TestRunnerAgent per feedback in pull request,
#       so jybot doesn't encounter issues. Special imports at top of file.
class StreamError(Exception):
    """
    Base class for EncodeError and DecodeError
    """
    pass


class EncodeError(StreamError):
    """
    This exception is raised when an unencodable object is passed to the
    dump() method or function.
    """
    wrapped_exceptions = (pickle.PicklingError, )


class DecodeError(StreamError):
    """
    This exception is raised when there is a problem decoding an object,
    such as a security violation.

    Note that other exceptions may also be raised during decoding, including
    AttributeError, EOFError, ImportError, and IndexError.
    """
    # NOTE: No JSONDecodeError in json in stdlib for python >= 2.6
    wrapped_exceptions = (pickle.UnpicklingError,)
    if _JSONAVAIL:
        if hasattr(json, 'JSONDecodeError'):
            wrapped_exceptions = (pickle.UnpicklingError, json.JSONDecodeError)


def dump(obj, fp):
    StreamHandler(fp).dump(obj)


def load(fp):
    return StreamHandler(fp).load()


def dumps(obj):
    """
    Similar method to json dumps, prepending data with message length
    header. Replaces pickle.dumps, so can be used in place without
    the memory leaks on receiving side in pickle.loads (related to
    memoization of data)
    
    NOTE: Protocol is ignored when json representation is used
    """
    fp = StringIO()
    StreamHandler(fp).dump(obj)
    return fp.getvalue()


def loads(s):
    """
    Reads in json message or pickle message prepended with message length
    header from a string. Message is expected to be encoded by this class as
    well, to have same message length header type.
    
    Specifically replaces pickle.loads as that function/method has serious
    memory leak issues with long term use of same Unpickler object for
    encoding data to send, specifically related to memoization of data to
    encode.
    """
    fp = StringIO(s)
    return StreamHandler(fp).load()


class StreamHandler(object):
    '''
    This class provides a common streaming approach for the purpose
    of reliably sending data over a socket interface. Replaces usage of
    Unpickler.load where possible with JSON format prepended by message length
    header. Uses json in python stdlib (in python >= 2.6) or simplejson (in
    python < 2.6). If neither are available, falls back to pickle.Pickler and
    pickle.Unpickler, attempting to eliminate memory leakage where possible at
    the expense of CPU usage (by not re-using Pickler or Unpickler objects).
    
    NOTE: StreamHandler currently assumes that same python version is installed
    on both sides of reading/writing (or simplejson is loaded in case of one
    side or other using python < 2.6). This could be resolved by requiring an
    initial header with json vs pickle determination from the writing side, but
    would considerably complicate the protocol(s) further (handshake would need
    to occur at least, and assumes encoding is used over a socket, etc.)
    
    json.raw_decode could be used rather than prepending with a message header
    in theory (assuming json is available), but performance of repeatedly
    failing to parse written data would make this an unworkable solution in
    many cases.
    '''
    loads = staticmethod(loads)
    dumps = staticmethod(dumps)

    def __init__(self, fp):
        """
        Stream handler that encodes objects as either JSON (if available) with
        message length header prepended for sending over a socket, or as a
        pickled object if using python < 2.6 and simplejson is not installed.
        
        Since pickle.load has memory leak issues with memoization (remembers
        absolutely everything decoded since instantiation), json is a preferred
        method to encode/decode for long running processes which pass large
        amounts of data back and forth.
        """
        if _JSONAVAIL:
            self._json_encoder = json.JSONEncoder(separators=(',', ':'),
                                                  sort_keys=True).encode
            self._json_decoder = json.JSONDecoder(strict=False).decode
        else:
            def json_not_impl(dummy):
                raise NotImplementedError(
                    'Python version < 2.6 and simplejson not installed. Please'
                    ' install simplejson.')

            self._json_decoder = staticmethod(json_not_impl)
            self._json_encoder = staticmethod(json_not_impl)
        self.fp = fp

    def dump(self, obj):
        """
        Similar method to json dump, prepending data with message length
        header. Replaces pickle.dump, so can be used in place without
        the memory leaks on receiving side in pickle.load (related to
        memoization of data)
        
        NOTE: Protocol is ignored when json representation is used
        """
        # NOTE: Slightly less efficient than doing iterencode directly into the
        #       fp, however difference is negligable and reduces complexity of
        #       of the StreamHandler class (treating pickle and json the same)
        write_list = []
        if _JSONAVAIL:
            s = self._json_encoder(obj)
            write_list.append(s)
            write_list.append('\n')
        else:
            write_list.append('P')
            s = pickle.dumps(obj, pickle.HIGHEST_PROTOCOL)
            write_list.extend([str(len(s)), '|', s])
        if sys.version_info < (3,0,0):
            self.fp.write(''.join(write_list))
        else:
            self.fp.write(bytes(''.join(write_list), 'UTF-8'))
        #self.fp.flush()


def load(self):
    """
        Reads in json message prepended with message length header from a file
        (or socket, or other .read() enabled object). Message is expected to be
        encoded by this class as well, to have same message length header type.
        
        Specifically replaces pickle.load as that function/method has serious
        memory leak issues with long term use of same Unpickler object for
        encoding data to send, specifically related to memoization of data to
        encode.
        """
    header = self._load_header()
    msgtype = header[0]
    msglen = header[1:]
    if not msglen.isdigit():
        raise DecodeError('Message header not valid: %r' % header)
    msglen = int(msglen)
    buff = StringIO()
    # Don't use StringIO.len for sizing, reports string len not bytes
    buff.write(self.fp.read(msglen))
    try:
        if msgtype == 'J':
            return self._json_decoder(buff.getvalue())
        elif msgtype == 'P':
            return pickle.loads(buff.getvalue())
        else:
            raise DecodeError("Message type %r not supported" % msgtype)
    except DecodeError.wrapped_exceptions as e:
        raise DecodeError(str(e))


def _load_header(self):
    """
        Load in just the header bit from a socket/file pointer
        """
    buff = StringIO()
    while len(buff.getvalue()) == 0 or buff.getvalue()[-1] != '|':
        recv_char = self.fp.read(1)
        if not recv_char:
            raise EOFError('File/Socket closed while reading load header')
        buff.write(recv_char)
    return buff.getvalue()[:-1]

from collections import deque
import struct
from socket import socket
from thread import start_new_thread
from matplotlib import animation
from matplotlib import pyplot as plt


class SFStream(object):
    TYPE_DATA = 100
    TYPE_EVENT = 101

    def __init__(self, stream):
        self._iterable = True
        self._user, self._pwd = SFStream._user_pwd(stream)
        self._header = SFStream._header(stream)
        self._stream = stream

    def __iter__(self):
        if self._iterable:
            self._iterable = False
            return self

    @property
    def user(self):
        return self._user

    @property
    def header(self):
        return self._header

    def check_password(self, password):
        return self._pwd == password

    @staticmethod
    def _int(f):
        x = struct.unpack('!i', f.recv(4))[0]
        return x

    @staticmethod
    def _long(f):
        x = struct.unpack('!q', f.recv(8))[0]
        return x

    @staticmethod
    def _byte(f):
        x = ord(f.recv(1)[0])
        return x

    @staticmethod
    def _string(f):
        l = ord(f.recv(1)[0])
        x = f.recv(l)
        return x

    @staticmethod
    def _double(f):
        x = struct.unpack('!d', f.recv(8))[0]
        return x

    @staticmethod
    def _user_pwd(f):
        user = SFStream._string(f)
        pwd = SFStream._string(f)
        return user, pwd

    @staticmethod
    def _header(f):
        header = {}
        SFStream._byte(f)
        SFStream._byte(f)
        SFStream._byte(f)
        ns = SFStream._byte(f)
        header["tag"] = SFStream._string(f)
        header["sensors"] = []
        for i in xrange(ns):
            SFStream._byte(f)
            SFStream._byte(f)
            sensors = {"ids": SFStream._byte(f), "type": SFStream._string(f), "name": SFStream._string(f)}
            nl = SFStream._byte(f)
            sensors["cols"] = []
            for l in xrange(nl):
                sensors["cols"].append(SFStream._string(f))
            header["sensors"].append(sensors)
        return header

    def next(self):
        SFStream._byte(self._stream)  # alignment byte
        t = SFStream._byte(self._stream)  # type byte
        data = {"type": t}
        if t == SFStream.TYPE_DATA:
            ids = SFStream._byte(self._stream)  # sensor id
            data["ids"] = ids
            data["timestamp"] = SFStream._long(self._stream)
            data["values"] = []
            for i in xrange(len(self._header["sensors"][ids]["cols"])):
                data["values"].append(SFStream._double(self._stream))
        elif t == SFStream.TYPE_EVENT:
            data["sensor"] = SFStream._byte(self._stream)
            data["timestamp"] = SFStream._long(self._stream)
            data["code"] = SFStream._int(self._stream)
            data["message"] = SFStream._string(self._stream)

        return data


class AnalogPlot(object):
    def __init__(self, iterable, buffer_length):
        self._it = iterable

        self.ax = deque([0.0] * buffer_length)
        self.ay = deque([0.0] * buffer_length)
        self.maxLen = buffer_length

    # add to buffer
    def addToBuf(self, buf, val):
        if len(buf) < self.maxLen:
            buf.append(val)
        else:
            buf.pop()
            buf.appendleft(val)

    # add data
    def add(self, data):
        assert (len(data) == 2)
        self.addToBuf(self.ax, data[0])
        self.addToBuf(self.ay, data[1])

    # update plot
    def update(self, frame_num, a0, a1):
        try:
            line = self._it.next()
            while line['type'] != SFStream.TYPE_DATA or line['ids'] != 1:
                line = self._it.next()
            print line
            data = [line['timestamp'], line["values"][0]]
            if len(data) == 2:
                self.add(data)
                a0.set_data(range(self.maxLen), self.ax)
                a1.set_data(range(self.maxLen), self.ay)
        except KeyboardInterrupt:
            pass

        return a0,


def client_thread(conn, address):
    print("Streaming from " + str(address))
    s = SFStream(conn)
    print("User: " + s.user)
    print("The header is:")
    print s.header
    print
    fig = plt.figure()
    ax = plt.axes(xlim=(0, 100), ylim=(0, 1023))
    a0, = ax.plot([], [])
    a1, = ax.plot([], [])
    ap = AnalogPlot(s, 100)
    anim = animation.FuncAnimation(fig, ap.update,
                                   fargs=(a0, a1),
                                   interval=0)

    plt.show()

    # for m in s:
    #     if m["type"] == SFStream.TYPE_DATA and m["ids"] == 1:
    #         print m["timestamp"], m["values"]


def _main():
    host = '0.0.0.0'  # '127.0.0.1' can also be used
    port = 2000

    print("SensorsFlows streaming EndPoint")
    print("v0.0 [unchecked protocol version]")
    print
    print("Starting server on " + str(host) + ":" + str(port))
    print("Use ipconfig on Windows or ifconfig on Unix to retrieve your ip")

    sock = socket()
    sock.bind((host, port))
    sock.listen(5)
    conn = None
    while True:
        conn, address = sock.accept()
        start_new_thread(client_thread, (conn, address))

    if conn is not None:
        conn.close()
    if sock is not None:
        sock.close()


if __name__ == "__main__":
    _main()

#!/usr/bin/python

import re, fileinput, datetime

class MatchCount:
    def __init__ (self, name, regex):
        self.regex = re.compile(regex)
        self.ids = {}
        self.name = name
    
    def process (self, line, lineno):
        m = self.regex.search(line)
        if m:
            self.ids[m.group("id")] = (m, lineno)
    
    def len (self):
        return len(self.ids)

logTimePat = '''\d{4}/\d{2}/\d{2} \d{2}:\d{2}:\d{2}:\d{3}'''

def report (action):
    return "(?P<time>%s) \w+ com.threerings.narya.bureau: %s \[oid=(?P<id>\d+)" % (logTimePat, action)

def parseTime (time):
    yr = time[0:4]
    mo = time[5:7]
    da = time[8:10]
    hr = time[11:13]
    mi = time[14:16]
    se = time[17:19]
    ml = time[20:23]
    return datetime.datetime(int(yr), int(mo), int(da), int(hr), int(mi), int(se), int(ml) * 1000)

create = MatchCount("Immediate Creation", report("Bureau ready, sending createAgent"))
pending = MatchCount("Pending", report("Bureau not ready, pending agent"))
delayCreate = MatchCount("Delayed Creation", report("Creating agent"))
confirm = MatchCount("Confirmed created", report("Agent creation confirmed"))
fail = MatchCount("Failed creation", report("Agent creation failed"))
destroy = MatchCount("Destroy", report("Destroying agent"))
dconfirm = MatchCount("Confirmed destruction", report("Agent destruction confirmed"))

transitions = [create, delayCreate, confirm, fail, pending, destroy, dconfirm]

print "Reading log"

def readLog():
    time = re.compile(logTimePat)
    lastTime = None
    for line in fileinput.input():
        for matcher in transitions:
            matcher.process(line, fileinput.lineno())
        m = time.search(line)
        if m != None: lastTime = m

    return lastTime

lastTimeInLog = readLog()
if lastTimeInLog != None:
    lastTimeInLog = parseTime(lastTimeInLog.group())


summary = False

if summary:
    createCount = create.len() + delayCreate.len()
    orphanCount = createCount - confirm.len() - fail.len()

    print "%d created, %d started, %d failed, %d orphaned, %.1f%%" % (
        createCount, confirm.len(), fail.len(), orphanCount,
        (float(orphanCount) * 100 / createCount))

class Path:
    def __init__(self, name, *transitions):
        self.transitions = transitions
        self.name = name
        self.id = None

    def describe (self, now):
        '''Describe a path, including a description of the time since the last change'''
        names = ", ".join(map(lambda t: t.name, self.transitions))
        if self.id != None and len(self.transitions) > 0:
            time = self.transitions[-1].ids[self.id][0].group('time')
            time = parseTime(time)
            names = "%s (%s ago)" % (names, describeTimeDelta(now - time))
        return "%s: %s" % (self.name, names)

    @staticmethod
    def calculate (id):
        '''Determine the sequence of transitions taken by an agent'''
        path = []
        for trans in transitions:
            if not trans.ids.has_key(id): continue
            path.append(trans)
        path.sort(lambda a, b: a.ids[id][1] - b.ids[id][1])
        path = Path("Agent " + id, *path)
        path.id = id
        return path

class PathSequence:
    def __init__(self, *paths):
        self.paths = paths

    def match (self, path):
        path = path.transitions
        for i in range(0, len(self.paths)):
            myPath = self.paths[i].transitions
            if path == myPath:
                return ("exact", self.paths[i])
            if path == myPath[0:len(path)]:
                return ("partial", self.paths[i])
        return None

validPaths = PathSequence(
    Path("Aborted", pending, destroy),
    Path("Pending-normal", pending, delayCreate, confirm, destroy, dconfirm),
    Path("Pending-stillborn", pending, delayCreate, destroy, confirm, dconfirm),
    Path("Normal", create, confirm, destroy, dconfirm),
    Path("Stillborn", create, destroy, confirm, dconfirm),
)

def describeTimeDelta (delta):
    '''Quick english description of a time interval'''
    seconds = delta.seconds
    if delta.days > 0:
        desc = "%d days"
    elif seconds > 3600:
        desc = "%d hours" % int(seconds/3600)
    elif seconds > 60:
        desc = "%s minutes" % int(seconds/60)
    else:
        desc = "%s seconds" % seconds
    return desc


def getAllIds ():
    all = {}
    for trans in transitions:
        for id in trans.ids.keys():
            all[id] = True
    all = all.keys()
    all.sort(lambda a, b: int(a) - int(b))
    return all

def getBureau (id, path):
    # Can't get this since t is on a different log line and we only match single lines
    return "??"

def checkAll (ids, now, verbose=False):
    completedPathCounts = {}

    for id in ids:
        if verbose: print "Checking %s" % id

        path = Path.calculate(id)
        if verbose: print path.describe(now)

        match = validPaths.match(path)
        if verbose: print match

        if match == None:
            print "Invalid path: %s" % path.describe(now)
        elif match[0] == "partial":
            print "Incomplete path: %s" % path.describe(now)
        elif match[0] == "exact":
            completedPathCounts[match[1]] = completedPathCounts.get(match[1], 0) + 1
    
    for path in validPaths.paths:
        count = completedPathCounts.get(path, 0)
        print "Path %s completed %d times" % (path.name, count)

print "Checking"
checkAll(getAllIds(), lastTimeInLog)


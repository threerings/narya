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


validPaths = [
    [pending, destroy],
    [pending, delayCreate, confirm, destroy, dconfirm],
    [pending, delayCreate, destroy, confirm, dconfirm],
    [create, confirm, destroy, dconfirm],
    [create, destroy, confirm, dconfirm],
]

def findPath (id):
    path = []
    for trans in transitions:
        if not trans.ids.has_key(id): continue
        path.append(trans)
    path.sort(lambda a, b: a.ids[id][1] - b.ids[id][1])
    return path

def describeTimeDelta (delta):
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

def describePath (id, now, path):
    names = ", ".join(map(lambda p: p.name, path))
    if id != None and len(path) > 0:
        time = path[-1].ids[id][0].group('time')
        time = parseTime(time)
        names = "%s (%s ago)" % (names, describeTimeDelta(now - time))
    return names

def describeId (id, now):
    path = findPath(id)
    print "ID %s: %s" % (id, describePath(id, now, path))

def getAllIds ():
    all = {}
    for trans in transitions:
        for id in trans.ids.keys():
            all[id] = True
    all = all.keys()
    all.sort(lambda a, b: int(a) - int(b))
    return all

def matchPath (path, pathSeq):
    for i in range(0, len(pathSeq)):
        if path == pathSeq[i]:
            return "exact"
        if path == pathSeq[i][0:len(path)]:
            return "partial"
    return None

def getBureau (id, path):
    return "??"

def checkAll (ids, now, verbose=False):
    for id in ids:
        if verbose: print "Checking %s" % id
        path = findPath(id)
        match = matchPath(path, validPaths)
        if match == None:
            print "Path for id %s (bureau %s) invalid: %s" % (id, getBureau(id, path), describePath(id, now, path))
        elif match == "partial":
            print "Path for id %s (bureau %s) partially matched: %s" % (id, getBureau(id, path), describePath(id, now, path))
        elif verbose:
            print describePath(id, now, path)

print "Checking"
checkAll(getAllIds(), lastTimeInLog)


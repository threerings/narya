#
# $Id: Makefile 3328 2005-02-03 01:16:12Z mdb $

#
# Executable definitions

CC=gcc
RM=rm
CP=cp
MKDIR=mkdir

#
# Directory definitions

ROOT=../../../../../../..
LIBRARIES_PATH=
OSINCDIR:=$(shell uname -s | tr 'A-Z' 'a-z')

#
# Parameter and file definitions

INCLUDES=-I.. -I${JAVA_HOME}/include -I${JAVA_HOME}/include/${OSINCDIR}
LIBRARIES=
TARGET=libsignal.so

#
# Target definitions

all: ${TARGET}

${TARGET}: com_threerings_util_signal_SignalManager.c
	@echo "Compiling SignalManager.c"
	@${CC} ${INCLUDES} -c com_threerings_util_signal_SignalManager.c \
	    -o com_threerings_util_signal_SignalManager.o
	@echo "Creating libsignal.so"
	@${CC} -o ${TARGET} com_threerings_util_signal_SignalManager.o \
	    ${LIBRARIES} ${LIBRARIES_PATH} -shared

clean:
	-${RM} -f *.o ${TARGET}

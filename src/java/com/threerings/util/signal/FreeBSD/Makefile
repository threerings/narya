#
# $Id$

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
OSINCDIR!=uname -s | tr 'A-Z' 'a-z'
INSTALL_PATH=${ROOT}/dist/lib/i386-FreeBSD

#
# Parameter and file definitions

INCLUDES=-I.. -I${JAVA_HOME}/include -I${JAVA_HOME}/include/${OSINCDIR}
LIBRARIES=
TARGET=libsignal.so

#
# Target definitions

all: ${TARGET}

install: ${TARGET}
	@${MKDIR} -p ${INSTALL_PATH}
	cp ${TARGET} ${INSTALL_PATH}

${TARGET}: com_threerings_util_signal_SignalManager.c
	@echo "Compiling SignalManager.c"
	@${CC} ${INCLUDES} -c com_threerings_util_signal_SignalManager.c \
	    -o com_threerings_util_signal_SignalManager.o
	@echo "Creating libsignal.so"
	@${CC} -o ${TARGET} com_threerings_util_signal_SignalManager.o \
	    ${LIBRARIES} ${LIBRARIES_PATH} -shared

clean:
	-${RM} -f *.o ${TARGET}

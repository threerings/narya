#
# $Id: Makefile 3332 2005-02-03 01:30:33Z mdb $

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
INSTALL_PATH=${ROOT}/dist/lib/`uname -m`-Darwin

#
# Parameter and file definitions

INCLUDES=-I.. -I/System/Library/Frameworks/JavaVM.framework/Headers
LIBRARIES=
TARGET=libsignal.dylib

#
# Target definitions

all: ${TARGET}

install: ${TARGET}
	@${MKDIR} -p ${INSTALL_PATH}
	cp ${TARGET} ${INSTALL_PATH}

${TARGET}: com_threerings_util_signal_SignalManager.c
	@echo "Compiling $<"
	@${CC} ${INCLUDES} -c com_threerings_util_signal_SignalManager.c \
	    -o com_threerings_util_signal_SignalManager.o
	@echo "Creating $@"
	@${CC} -o ${TARGET} com_threerings_util_signal_SignalManager.o \
	    ${LIBRARIES} ${LIBRARIES_PATH} -bundle

clean:
	-${RM} -f *.o ${TARGET}

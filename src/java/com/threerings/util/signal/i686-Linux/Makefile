#
# $Id: Makefile,v 1.1 2003/03/31 02:10:04 mdb Exp $

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
INSTALL_PATH=${ROOT}/dist/lib/i686-Linux

#
# Parameter and file definitions

INCLUDES=-I.. -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux
LIBRARIES=
TARGET=libsignal.so
INSTALL_TARGET=${INSTALL_PATH}/${TARGET}

#
# Target definitions

all: ${INSTALL_TARGET}

${INSTALL_TARGET}: com_threerings_util_signal_SignalManager.c
	@echo Compiling SignalManager.c
	@${CC} ${INCLUDES} -c com_threerings_util_signal_SignalManager.c \
	    -o com_threerings_util_signal_SignalManager.o
	@echo Creating libsignal.so
	@${MKDIR} -p ${INSTALL_PATH}
	@${CC} -o ${INSTALL_TARGET} com_threerings_util_signal_SignalManager.o \
	    ${LIBRARIES} ${LIBRARIES_PATH} -shared

clean:
	-${RM} *.o
	-${RM} ${INSTALL_TARGET}

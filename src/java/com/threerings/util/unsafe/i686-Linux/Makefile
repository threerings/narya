#
# $Id: Makefile,v 1.1 2003/05/08 21:25:20 mdb Exp $

#
# Executable definitions

CC=gcc
RM=rm
CP=cp
MKDIR=mkdir

#
# Directory definitions

ROOT=../../../../../../..
LIBRARIES_PATH=-L/usr/X11R6/lib
INSTALL_PATH=${ROOT}/dist/lib/i686-Linux

#
# Parameter and file definitions

INCLUDES=-I.. -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux
LIBRARIES=-lX11
TARGET=libunsafe.so
INSTALL_TARGET=${INSTALL_PATH}/${TARGET}

#
# Target definitions

all: ${INSTALL_TARGET}

${INSTALL_TARGET}: com_threerings_util_unsafe_Unsafe.c
	@echo Compiling Unsafe.c
	@${CC} ${INCLUDES} -c com_threerings_util_unsafe_Unsafe.c \
	    -o com_threerings_util_unsafe_Unsafe.o
	@echo Creating ${TARGET}
	@${MKDIR} -p ${INSTALL_PATH}
	@${CC} -o ${INSTALL_TARGET} com_threerings_util_unsafe_Unsafe.o \
	    ${LIBRARIES} ${LIBRARIES_PATH} -shared

clean:
	-${RM} *.o
	-${RM} ${INSTALL_TARGET}

#
# $Id: Makefile,v 1.6 2004/10/20 03:28:48 mdb Exp $

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
TARGET=libkeybd.so

#
# Target definitions

all: ${TARGET}

install: ${TARGET}
	@${MKDIR} -p ${INSTALL_PATH}
	cp ${TARGET} ${INSTALL_PATH}

${TARGET}: com_threerings_util_keybd_Keyboard.c
	@echo Compiling Keyboard.c
	@${CC} ${INCLUDES} -c com_threerings_util_keybd_Keyboard.c \
	    -o com_threerings_util_keybd_Keyboard.o
	@echo Creating libkeybd.so
	@${CC} -o ${TARGET} com_threerings_util_keybd_Keyboard.o \
	    ${LIBRARIES} ${LIBRARIES_PATH} -shared

clean:
	-${RM} *.o
	-${RM} ${TARGET}

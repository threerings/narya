#
# $Id: Makefile,v 1.4 2003/02/18 23:06:54 ray Exp $

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
INSTALL_TARGET=${INSTALL_PATH}/${TARGET}

#
# Target definitions

all: ${INSTALL_TARGET}

${INSTALL_TARGET}: com_threerings_util_keybd_Keyboard.c
	${CC} ${INCLUDES} -c com_threerings_util_keybd_Keyboard.c \
	    -o com_threerings_util_keybd_Keyboard.o
	${MKDIR} -p ${INSTALL_PATH}
	${CC} -o ${INSTALL_TARGET} com_threerings_util_keybd_Keyboard.o \
	    ${LIBRARIES} ${LIBRARIES_PATH} -shared

clean:
	-${RM} *.o
	-${RM} ${INSTALL_TARGET}

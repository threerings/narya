#
# $Id: Makefile,v 1.1 2003/01/12 00:26:39 shaper Exp $

CC=gcc
RM=rm

INCLUDES=-I.. -I/usr/local/jdk1.4/include -I/usr/local/jdk1.4/include/linux
LIBRARIES_PATH=-L/usr/X11R6/lib
LIBRARIES=-lX11
TARGET=libkeybd.so

all: ${TARGET}

${TARGET}: com_threerings_util_keybd_Keyboard.c
	${CC} ${INCLUDES} -c com_threerings_util_keybd_Keyboard.c \
	    -o com_threerings_util_keybd_Keyboard.o
	${CC} -o ${TARGET} com_threerings_util_keybd_Keyboard.o \
	    ${LIBRARIES} ${LIBRARIES_PATH} -shared

clean:
	-${RM} *.o
	-${RM} ${TARGET}

#
# $Id: Makefile,v 1.1 2003/01/12 00:26:39 shaper Exp $

CROSSTOOLS_PATH=/usr/local/cross-tools
MINGW_PATH=${CROSSTOOLS_PATH}/i386-mingw32msvc
WIN32API_PATH=${CROSSTOOLS_PATH}/w32api

CC=${MINGW_PATH}/bin/gcc
RM=rm

INCLUDES=-I.. -I/usr/local/jdk1.4/include -I/usr/local/jdk1.4/include/linux \
    -I${CROSSTOOLS_PATH}/include -I${WIN32API_PATH}/include
LIBRARIES_PATH=-L${CROSSTOOLS_PATH}/lib -L${WIN32API_PATH}/lib
LIBRARIES=
TARGET=keybd.dll

all: ${TARGET}

${TARGET}: com_threerings_util_keybd_Keyboard.c
	${CC} ${INCLUDES} -c com_threerings_util_keybd_Keyboard.c \
	    -o com_threerings_util_keybd_Keyboard.o
	${CC} -o ${TARGET} com_threerings_util_keybd_Keyboard.o \
	    ${LIBRARIES} ${LIBRARIES_PATH} -shared

clean:
	-${RM} *.o
	-${RM} ${TARGET}

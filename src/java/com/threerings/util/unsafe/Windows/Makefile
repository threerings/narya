#
# $Id$

NAME=unsafe

#
# Executable definitions

CC=i586-mingw32msvc-gcc
RM=rm
CP=cp
MKDIR=mkdir
DLLWRAP=i586-mingw32msvc-dllwrap
#
# Directory definitions

ROOT=../../../../../../..
INSTALL_PATH=${ROOT}/dist/lib/i686-Windows

#
# Source files

SRCS = com_threerings_util_unsafe_Unsafe.c
OBJS = ${SRCS:.c=.o}

#
# Parameter and file definitions

INCLUDES=
CFLAGS=-I.. -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux

LDFLAGS=-L/usr/X11R6/lib
LIBS=
TARGET=${NAME}.dll
INSTALL_TARGET=${INSTALL_PATH}/${TARGET}

#
# Target definitions

all: ${INSTALL_TARGET}

${INSTALL_TARGET}: ${OBJS}
	@${MKDIR} -p ${INSTALL_PATH}
	${DLLWRAP} --output-def ${NAME}.def --add-stdcall-alias \
		-o ${INSTALL_TARGET} -s ${OBJS}

clean:
	-${RM} ${OBJS}
	-${RM} ${INSTALL_TARGET}

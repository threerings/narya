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
INSTALL_PATH=${ROOT}/dist/lib/i386-FreeBSD

#
# Parameter and file definitions

INCLUDES=-I.. -I${JAVA_HOME}/include -I${JAVA_HOME}/include/${OSINCDIR}
LIBRARIES=
TARGET=libunsafe.so

#
# Target definitions

all: ${TARGET}

install: ${TARGET}
	@${MKDIR} -p ${INSTALL_PATH}
	cp ${TARGET} ${INSTALL_PATH}

${TARGET}: com_threerings_util_unsafe_Unsafe.c
	@echo "Compiling Unsafe.c"
	@${CC} ${INCLUDES} -c com_threerings_util_unsafe_Unsafe.c \
	    -o com_threerings_util_unsafe_Unsafe.o
	@echo "Creating libunsafe.so"
	@${CC} -o ${TARGET} com_threerings_util_unsafe_Unsafe.o \
	    ${LIBRARIES} ${LIBRARIES_PATH} -shared

clean:
	-${RM} -f *.o ${TARGET}

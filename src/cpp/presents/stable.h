//
// C Library Headers
//
#include <math.h>
#include <time.h>
#include <float.h>
#include <wchar.h>

//
// Standard C++ Headers
//
#include <string>
#include <vector>
#include <map>
#include <stdexcept>

//
// Boost Headers
//
#pragma warning(push, 3)

#define BOOST_ALL_NO_LIB
#define BOOST_REGEX_NO_LIB
#define BOOST_DATE_TIME_NO_LIB

#include <boost/smart_ptr.hpp>
#include <boost/static_assert.hpp>
#include <boost/bind.hpp>

//
// boost::signals
#pragma warning(push)
#pragma warning(disable: 4512) // assignment operator could not be generated
#include <boost/signals.hpp>
#pragma warning(pop)

// typedefs
typedef boost::signals::connection connection_t;
typedef boost::signals::scoped_connection scoped_connection_t;

// GCC
#if defined(__GNUC__)
#  define PRESENTS_COMPILER_GCC	(__GNUC__ * 10000 + __GNUC_MINOR__ * 100 + __GNUC_PATCHLEVEL__)

// Visual Studio
#elif defined(_MSC_VER)
#  define PRESENTS_COMPILER_MSVC 	_MSC_VER

// Unknown Compiler
#else
#  error The current compiler is not supported.
#endif

#ifdef __APPLE__
#include "TargetConditionals.h"
#endif

#if defined(_WIN32)
#  define PRESENTS_HOST_LITTLE_ENDIAN
typedef signed __int8		int8;
typedef unsigned __int8		uint8;
typedef signed __int16		int16;
typedef unsigned __int16	uint16;
typedef signed __int32		int32;
typedef unsigned __int32	uint32;
typedef signed __int64		int64;
typedef unsigned __int64	uint64;
typedef unsigned int        uint;
typedef unsigned long       ulong; 

#elif defined(TARGET_OS_IPHONE)
#  define PRESENTS_IOS
#include <MacTypes.h>
typedef int8_t		int8;
typedef int16_t		int16;
typedef int32_t		int32;
typedef int64_t		int64;
typedef uint8_t		uint8;
typedef uint16_t	uint16;
typedef uint32_t	uint32;
typedef uint64_t	uint64;
#define _INT32

typedef unsigned int uint;
typedef unsigned long ulong;

#elif defined(__APPLE__)
#  define PRESENTS_MACOSX
#include <libkern/OSTypes.h>

#ifndef __i386__
typedef SInt64 int64;
typedef UInt64 uint64;
typedef SInt32 int32;
typedef SInt16 int16;
typedef SInt8 int8;
typedef UInt32 uint32;
typedef UInt16 uint16;
typedef UInt8 uint8;
#else
typedef int8_t		int8;
typedef int16_t		int16;
typedef int32_t		int32;
typedef int64_t		int64;
typedef uint8_t		uint8;
typedef uint16_t	uint16;
typedef uint32_t	uint32;
typedef uint64_t	uint64;
#define _INT32
#endif

typedef unsigned int uint;
typedef unsigned long ulong;


#elif defined(__linux__)
#  define PRESENTS_HOST_LITTLE_ENDIAN
typedef signed char	   int8;
typedef signed short	   int16;
typedef signed int 	   int32;
typedef signed long long   int64;
typedef unsigned char	   uint8;
typedef unsigned short	   uint16;
typedef unsigned int 	   uint32;
typedef unsigned long long uint64;

typedef unsigned int uint;
typedef unsigned long ulong;

// Unknown Platform
#else
#  error The current platform is not supported.
#endif

#if defined(__APPLE__)
// Apple defines __BIG_ENDIAN__ and __LITTLE_ENDIAN__
#  if defined(__BIG_ENDIAN__)
#    define PRESENTS_HOST_BIG_ENDIAN
#  else
#    define PRESENTS_HOST_LITTLE_ENDIAN
#  endif
#endif

#define PLOG	presents::internal::log

#define Shared boost::shared_ptr

#define utf8 std::string
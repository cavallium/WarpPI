cmake_minimum_required(VERSION 2.8)
project(picalculatornative)
SET(COMPILE_DEFINITIONS -Werror)

find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

include_directories(/opt/vc/include)
include_directories(/opt/vc/include/interface/vcos/pthreads)
include_directories(/opt/vc/include/interface/vmcs_host)
include_directories(/opt/vc/include/interface/vmcs_host/linux)

link_directories(/opt/vc/lib)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
set(SOURCE_FILES TestJNI.cpp)
add_library(picalculatornative SHARED ${SOURCE_FILES})
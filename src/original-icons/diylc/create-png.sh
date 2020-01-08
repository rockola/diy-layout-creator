#!/usr/bin/env bash

# requires ImageMagick

SRC=diylc.svg
TARGETDIR=../../../target/PNG
INTERMEDIATE=${TARGETDIR}/diylc.png

mkdir -p ${TARGETDIR}
# First, create full size PNG
# because ImageMagick does not know how to create different
# sized PNG files straight from SVG...
# Supposedly IM7 will though, hope so
convert ${SRC} -transparent white ${INTERMEDIATE}
for DIM in 16 32 48 64 96 128 256 512 1024
do
    TARGET=`basename $SRC .svg`-${DIM}x${DIM}.png
    echo ${TARGET}
    convert ${INTERMEDIATE} -geometry ${DIM}x${DIM}  -transparent white ${TARGETDIR}/${TARGET}
done

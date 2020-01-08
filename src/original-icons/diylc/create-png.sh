#!/usr/bin/env bash

# requires ImageMagick

SRC=diylc.svg
TARGETDIR=../../../target/PNG

mkdir -p ${TARGETDIR}
for DIM in 16 32 48 64 96 128 256 512 1024
do
    TARGET=`basename $SRC .svg`-${DIM}x${DIM}.png
    echo ${TARGET}
    convert -size ${DIM}x${DIM} ${SRC} -transparent white ${TARGETDIR}/${TARGET}
done

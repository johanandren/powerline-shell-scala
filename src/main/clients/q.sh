#!/bin/sh
#
# source this into the current shell to get the q command
# which is a history based quick-cd
#
QDIR=$(dirname $0)

function q() {
    if [ ! "$1" ]; then
        echo "Usage: ch <expr>"
    else
        DIR=$($QDIR/dir-history-search $1)
        if [ "$DIR" ]; then
            cd $DIR
        fi
    fi
}
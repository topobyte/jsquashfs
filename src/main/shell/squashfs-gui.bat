@echo off

call lib/setclasspath.bat

start javaw -Djava.library.path=lib/ -cp "%CP%" com.fernsroth.squashfs.gui.JSquashFSGUI %*

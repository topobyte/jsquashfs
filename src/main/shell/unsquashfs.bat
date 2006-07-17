@echo off

call lib/setclasspath.bat

java -cp "%CP%" com.fernsroth.squashfs.Unsquashfs %*

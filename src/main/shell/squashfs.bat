@echo off

set CP=lib/*;lib/

java -cp %CP% com.fernsroth.squashfs.Squashfs %*

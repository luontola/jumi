#!/usr/bin/ruby
require 'fileutils'

# If the Scala compile server (fsc) is killed, it will not remove the
# file (in the below mentioned directory) which says that in which port
# the server is running. This leads to a deadlock in IDEA's Scala plugin
# during Make.
#
# You should follow these steps to use fsc in IDEA:
#
# 1. Make sure that no fsc instances are running (i.e. kill all Java
#    processes which smell like fsc)
# 2. Run this script
# 3. Start a "Scala Compilation Server" run configuration in IDEA (maybe
#    with JVM options: -Xmx512m -verbose)
# 4. Build the project with Make

FileUtils.rm_rf ENV['TEMP'] + '/scala-devel'

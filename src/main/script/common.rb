#!/usr/bin/ruby

SCRIPTS = File.dirname($0)
PROJECT_HOME = File.expand_path('../../..', SCRIPTS)

if !File.file?("#{PROJECT_HOME}/pom.xml")
  $stderr.puts "Error: Project not found at #{PROJECT_HOME}"
  exit 1
end

#!/usr/bin/ruby

SCRIPTS = File.dirname($0)
PROJECT_HOME = File.expand_path('../../..', SCRIPTS)

File.open("#{PROJECT_HOME}/AUTHORS.txt", 'w') do |file|
  file.puts <<-eos
This is the official list of Dimdwarf authors for copyright purposes.

The authors are ordered by an estimation of the amount of code that they have
contributed to the project. For a detailed list of who owns the copyright of a
particular file, see the version history of that file.

  eos
  file.puts `#{SCRIPTS}/list_authors.rb`
end

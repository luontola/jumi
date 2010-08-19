#!/usr/bin/ruby
require File.dirname($0) + '/common'

# Does a clean build on the project and pushes the changes to the
# remote Git repository if there are no failures. If there are
# uncommitted changes, stashes them before building and restores
# them after the build.

BRANCH = 'master'
REMOTE = 'origin'

def clean_build
  stashed_something = stash
  ok = system("mvn clean verify")
  if stashed_something
    unstash
  end
  ok
end

def stash
  message = `git stash`
  puts message
  message.include? 'Saved working directory and index state'
end

def unstash
  system("git stash pop")
end

def push_changes
  system("git push #{REMOTE} #{BRANCH}")
end

def success(message)
  puts
  puts message
  exit 0
end

def failure(message)
  puts
  puts "Error: #{message}"
  exit 1
end

Dir.chdir(PROJECT_HOME) do
  clean_build   or failure 'Failed to build'
  push_changes  or failure 'Failed to push changes - you should do it manually'
  success 'All OK'
end

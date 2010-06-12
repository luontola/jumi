#!/usr/bin/ruby
require File.dirname($0) + '/common'

BRANCH = 'master'
REMOTE = 'origin'

def clean_build()
  return (system("git checkout -f #{BRANCH}") and
          system("mvn clean verify"))
end

def push_changes()
  return system("git push #{REMOTE} #{BRANCH}")
end

def rollback_changes()
  return system("git reset --hard #{REMOTE}/#{BRANCH}")
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
  if clean_build()
    if push_changes()
      success 'All OK'
    else
      failure 'Failed to push changes - you should do it manually'
    end
  else
    rollback_changes()
    failure 'Failed to build - changes rolled back (undo with `git reset --hard HEAD@{1}`)'
  end
end

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

def print_results(msg)
  puts
  puts msg
end

Dir.chdir(PROJECT_HOME) do
  if clean_build()
    if push_changes()
      print_results 'All OK'
    else
      print_results 'Error: Failed to push changes - you should do it manually'
    end
  else
    rollback_changes()
    print_results 'Error: Failed to build - changes rolled back (undo with `git reset --hard HEAD@{1}`)'
  end
end

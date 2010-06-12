#!/usr/bin/ruby
require File.dirname($0) + '/common'

# Does a clean build on the project and pushes the changes to the
# remote Git repository if there are no failures. Otherwise reverts the
# local repository to the version at the remote repository.
#
# The workflow supported by this script is that the office has one
# workstation dedicated for manual CI builds. The team members have
# only read access to the central repository, but the CI machine has
# also write access. When a team member wants to push his changes to
# the server, he will first push them to the CI machine. Then he will
# walk to the CI machine and run this script (you could even automate
# this step with a 'post-receive' hook).
#
# If the script succeeds, the change is pushed to the central
# repository and from there others will get the changes (a sound signal
# at this point could be useful). If the change fails, the CI machine
# automatically reverts all changes (so that someone else can come use
# the CI machine) and the team member will walk back to his own
# workstation to fix the bug (after inspecting the build logs on the CI
# machine). This way the code on the central repository is guaranteed
# to always pass the build.

BRANCH = 'master'
REMOTE = 'origin'

def clean_build
  return (system("git checkout -f #{BRANCH}") and
          system("mvn clean verify"))
end

def push_changes
  return system("git push #{REMOTE} #{BRANCH}")
end

def rollback_changes
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
  if clean_build
    if push_changes
      success 'All OK'
    else
      failure 'Failed to push changes - you should do it manually'
    end
  else
    rollback_changes
    failure 'Failed to build - changes rolled back (undo with `git reset --hard HEAD@{1}`)'
  end
end

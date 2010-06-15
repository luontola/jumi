#!/usr/bin/ruby

authors = []
`git shortlog -s -n`.each_line do |line|
  parts = line.chomp.split(' ', 2)
  count = parts[0].to_i
  name = parts[1]
  authors << {:count => count, :name => name}
end

$total_count = authors.inject(0) { |sum, author| sum + author[:count] }

def author_to_s(author)
  percent = sprintf("%6.2f\%", author[:count].to_f / $total_count * 100)
  return "(#{percent}) #{author[:name]}\n"
end

authors.each do |author|
  print author_to_s(author)
end

#!/usr/bin/ruby1.9

paths = ARGV.map {|x| File.expand_path(x) }
system("cd bin; java -classpath '.:../sikuli-script.jar' textmatch.Main #{paths.join(' ')}" + ' | grep -v "^\[info\]"')

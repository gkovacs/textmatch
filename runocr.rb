#!/usr/bin/ruby1.9

paths = ARGV.map {|x| File.expand_path(x) }
puts paths
system("export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:#{Dir.pwd}; cd bin; java -classpath '.:../sikuli-script.jar' textmatch.RunOCR #{paths.join(' ')}" + ' | grep -v "^\[info\]"')

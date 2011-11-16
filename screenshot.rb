#!/usr/bin/ruby1.9

paths = ARGV.map {|x| File.expand_path(x) }
system("export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:.; cd bin; java -classpath '.:../sikuli-script.jar' textmatch.ScreenshotTaker #{paths.join(' ')}" + ' | grep -v "^\[info\]"')

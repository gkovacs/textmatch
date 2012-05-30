# About

This is the codebase for ScreenMatch, a system which matches messages to screenshots, so that they can be shown to translators to provide context.

The details of this system are published in http://groups.csail.mit.edu/uid/other-pubs/chi2012-screenshots-for-translation-context.pdf

# Prereqs

These instructions were tested on Ubuntu 11.04, 32-bit. It should also work with 11.10 (though I haven't tested it myself), though if you're using 12.04 then you will need to install tesseract2 manually (only version 3 is available in the repos). For 64-bit installs, you'll need to replace the included sikuli-script.jar with a 64-bit version (from http://sikuli.org/ )

Once Ubuntu 11.04 is installed, install the following packages:

    sudo apt-get install ant openjdk-6-jdk tesseract-ocr tesseract-ocr-eng libcv2.1 libcvaux2.1 ruby1.9.1

Run the following command so that it'll find ruby 1.9 (needed for the convenience scripts)

    sudo ln -s /usr/bin/ruby1.9.1 /usr/bin/ruby1.9

# Downloading and Compiling

    git clone git://github.com/gkovacs/textmatch.git
    cd textmatch
    ant

# Running

The following is an example of how one would use this tool for pcmanfm. First we'll need a file pcmanfm-es.po which is a gettext-format message file, and the directory pcmanfm-screenshots contains screenshots of pcmanfm in png format.

    ./textmatch.rb pcmanfm-es.po pcmanfm-screenshots/*.png > pcmanfm-annotated.po

The output of this command is a po file where each message is annotated with a comment starting with #& that indicates the region in the screenshot that matches it. The format of the comment is:

    #& /home/geza/workspace/textmatch/pcmanfm-screenshots/6.png(229,160,117,10)~~~~~~Detailed Llst View

In this case, we have the matched screenshot file, followed by (x,y,width,height) of the rectangle in the screenshot containing the matched text. (The remainder is debug output and can be ignored). To visualize the output, you can use the command:

    ./htmlgen.rg pcmanfm-annotated.po > pcmanfm-annotated.html

If you open the file pcmanfm-annotated.html, it will have each of the screenshots displayed alongside the messages.

# License

This is Free/open-source software, licensed under the MIT License, which you can find in LICENSE.txt

# Contact

Geza Kovacs http://gkovacs.github.com/

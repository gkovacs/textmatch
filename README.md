# About

This is the codebase for ScreenMatch, a system which matches messages to screenshots, so that they can be shown to translators to provide context.

The details of this system are published in http://groups.csail.mit.edu/uid/other-pubs/chi2012-screenshots-for-translation-context.pdf

# Prereqs

A VMware image is available at http://gkovacs.xvm.mit.edu/screenmatch-ubuntu-natty-i386.7z in case you want to skip these steps (username and password are transifex; compiled code is in ~/textmatch).

These instructions were tested on Ubuntu 11.04 32-bit. It should also work with 11.10 (though I haven't tested it myself), though if you're using 12.04 then you will need to install tesseract2 manually (only version 3 is available in the repos). For 64-bit installs, you'll need to replace the included sikuli-script.jar with a 64-bit version (from http://sikuli.org/ ).

Once Ubuntu 11.04 32-bit is installed, install the following packages:

    sudo apt-get install ant openjdk-6-jdk tesseract-ocr tesseract-ocr-eng libcv2.1 libcvaux2.1 ruby1.9.1

Then make ruby1.9 point to ruby1.9.1 (needed for the convenience scripts)

    sudo ln -s /usr/bin/ruby1.9.1 /usr/bin/ruby1.9

# Downloading and Compiling

    git clone git://github.com/gkovacs/textmatch.git
    cd textmatch
    ant

# Running

The following is an example of how one would use this tool for pcmanfm. First we'll need a file pcmanfm-es.po which is a gettext-format message file, and a directory pcmanfm-screenshots which contains screenshots of pcmanfm in png format.

    ./textmatch.rb pcmanfm-es.po pcmanfm-screenshots/*.png > pcmanfm-annotated.po

The output of this command is a po file where each message is annotated with a comment starting with #& that indicates the region in the screenshot that matches it. The format of the comment is:

    #& /home/geza/workspace/textmatch/pcmanfm-screenshots/6.png(229,160,117,10)~~~~~~Detailed Llst View
    msgid "Detailed List View"

In this case, we have the matched screenshot file, followed by (x,y,width,height) of the rectangle in the screenshot containing the matched text. (The remainder is debug output and can be ignored). To visualize the output, you can use the command:

    ./htmlgen.rg pcmanfm-annotated.po > pcmanfm-annotated.html

If you open the file pcmanfm-annotated.html, it will have each of the screenshots displayed alongside the messages.

# Testing

This software was tested by comparing the automatic screenshot-message matches to manual matches. The manual matches for pcmanfm are stored in pcmanfm-reference.po; the possible screenshots which would be valid matches are indicated by a comment starting with #% (or #% nomatches, if there were no matches among the screenshots for that message)

    #% /home/geza/workspace/textmatch/pcmanfm-screenshots/11.png
    #% /home/geza/workspace/textmatch/pcmanfm-screenshots/5.png
    #% /home/geza/workspace/textmatch/pcmanfm-screenshots/6.png
    #% /home/geza/workspace/textmatch/pcmanfm-screenshots/10.png
    msgid "Detailed List View"

We can then assess the accuracy of the software by seeing whether the match made by textmatch.rb is among these. This is done by the script accuracy.rb, which you supply a message file containing both #& (automatic match) and #% (manual match) annotations, and it reports statistics:

    ./textmatch.rb pcmanfm-reference.po pcmanfm-screenshots/*.png > pcmanfm-reference-tested-tess2.po
    ./accuracy.rb pcmanfm-reference-tested-tess2.po > pcmanfm-reference-tested-tess2.txt

# License

This is Free/open-source software, licensed under the MIT License, which you can find in LICENSE.txt

# Contact

Geza Kovacs http://gkovacs.github.com/

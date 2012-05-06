# About

This is the codebase for ScreenMatch, a system which matches messages to screenshots, so that they can be shown to translators to provide context.

The details of this system are published in http://groups.csail.mit.edu/uid/other-pubs/chi2012-screenshots-for-translation-context.pdf

# Prereqs

Place sikuli-script.jar from http://sikuli.org/ into this directory

# Running

    ./textmatch.rb message-file.po imgfile1.png imgfile2.png > annotated-message-file.po

The output of this command is a po file where each message is annotated with a comment indicating the region in the screenshot that matches it.

    ./htmlgen.rg annotated-message-file.po

The output of this command is a html file where each of the screenshots are displayed alongside the messages.

# License

This is Free/Open-source software, licensed under the MIT License, which you can find in LICENSE.TXT

# Contact

Geza Kovacs http://gkovacs.github.com/

#!/usr/bin/python

import os

#os.system("cp -r ../publicstuff-android ../publicstuff-android-node2");

#os.system("../publicstuff-android-node2/appbuilder.py"); #this has to load with arguments to avoid infinite loop lol

os.system("ant wassabi-reset");

'''
Ant needs to be in the system $PATH to run from command line

WARNING, Ant doesn't seem to reset variables
therefore, do not try to combine these targets

They have to be separate commandline statements
'''


os.system("ant wassabi");os.system("ant wassabi-reset");
os.system("ant pensacola-fl");os.system("ant wassabi-reset");
os.system("ant bradenton");os.system("ant wassabi-reset");
os.system("ant riverton-ut");os.system("ant wassabi-reset");
os.system("ant dormont-pa");os.system("ant wassabi-reset");
os.system("ant philadelphia-pa");os.system("ant wassabi-reset");
os.system("ant durango-co");os.system("ant wassabi-reset");
os.system("ant dayton-oh");os.system("ant wassabi-reset");
os.system("ant fontana-ca");os.system("ant wassabi-reset");
os.system("ant north-miami-beach");os.system("ant wassabi-reset");
os.system("ant elk-grove");os.system("ant wassabi-reset");
os.system("ant chandler-az");os.system("ant wassabi-reset");
os.system("ant plano-tx");os.system("ant wassabi-reset");

#node two
os.system("ant asheville");os.system("ant wassabi-reset");
os.system("ant woodstock-ga");os.system("ant wassabi-reset");
os.system("ant pataskala");os.system("ant wassabi-reset");
os.system("ant daly-city");os.system("ant wassabi-reset");
os.system("ant oceanside-ca");os.system("ant wassabi-reset");
os.system("ant newport-beach");os.system("ant wassabi-reset");
os.system("ant tallahassee");os.system("ant wassabi-reset");
os.system("ant aha");os.system("ant wassabi-reset");

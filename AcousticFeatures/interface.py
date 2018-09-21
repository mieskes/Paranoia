#!/usr/bin/env python
# -*- coding: utf-8 -*-
import argparse
import time
# from AcousticFeatures.ftex_utils import *
from ftex_utils import *
from acousticftextractor import AcousticFeatureExtractor
#TODO: Change pyLinter snake_case naming convention for variables

#=========================Commandline Parser===================================
#Initialize a arguments parser for commandline based execution of the Script
parser = argparse.ArgumentParser(prog='AcousticFeatureExtraction',
                                 description='Extracts acoustic features from speech\n',
                                 formatter_class=argparse.RawTextHelpFormatter)
parser.add_argument('-s','--segFiles',type=str, metavar = '', nargs='*',
                    help="a single .txt file or a list seperated by \" \" \n"
                    "containing the segmetns of the speech\n"
                    "Content of the File should look like the example below:\n"
                    "-----------------------exampleTxtFile.txt---------------------\n"
                    "[NameAndPathToTheAudioFiles] Optional or give it in via a -a in matching order\n"
                    "Therapeut Therapeut 00:00:00 000 00:00:00 000 00:00:00\n"
                    "Name of the Segment | Name of the Segment | TimeStamp Begin | TimeStamp End | Timestamp Duration")
parser.add_argument('-x','--executeMode',type=int, metavar = '', nargs='?', default=1,
                    help="Determines the execution mode (default = 1)\n"
                    "\t0: Don't execute the script\n"
                    "\t1: Extract features only on the new audio file (containing only 1 speaker)\n"
                    "\t2: Like option 1 but in addition generate the slices\n"
                    "\t3: Extract features based on the slices (important: add -o True)")
parser.add_argument('-n','--naming',type=int, metavar = '', nargs='?', default=1,
                    help="determines the naming of the slices (default = 1)\n"
                    "\t1: ascending digits sequence (0000-9999)\n"
                    "\t2: containing the start of the segment in ms\n"
                    "\t3: containing start and end of the segment ins ms")
parser.add_argument('-k','--keep',type=str2bool, metavar = '', nargs='?', default=True,
                    help="The Default value True will keep all temporary files \n"
                    "which were created in the process (converted .wav files)\n"
                    "and the cutted .wav files from the segmentation")
parser.add_argument('-t','--therapist',type=str2bool, metavar = '', nargs='?', default=True,
                    help="Determines the target speaker, from whom the features \n"
                    "shall be extracted. The default value is true, resulting in ft extraction\n"
                    "for the Therapist. False or 0 is for extracion of patients acoustic ft.")
parser.add_argument('-b','--backchannels',type=float, metavar = '', nargs='?', default=500,
                    help="determines the maximum duration for backchannels (def. below). Everything below\n"
                    "that duration is removed from the segments. Input is in ms and format is float.\n"
                    "The default value for backchannel is 500.0 ms. If it set to 0\n"
                    "no backchannels will be removed, and the segments contain overlap for the speakers.\n"
                    "\tBackhcannels: are utterances of the other speaker. For example,\n"
                    "\twhile the therapist speaks the patients say mhm, aha, ...")
parser.add_argument('-o','--overwrite',type=str2bool, metavar = '', nargs='?', default=False,
                    help="The scripts checks if a file was already converted and sliced.\n"
                    "This option determines if the old files shall be used\n"
                    "In default (False) nothing will be overwritten\n"
                    "True is recommended for execution mode 3, features based on slices!")
parser.add_argument('--version', action='version', version='%(prog)s 2.1\n'
                                                    'written by M.Sc. Bjoern Buedenbender (FRA UAS)')
#TODO: Consider the Creation of a logfile for the script, or look for a library that does it
#TODO: Add Parser Argument -l for getting a Log File
#TODO: Add Parser Argument for a Txt File Containing Links to the segmentTxt and the regarding audio

def main(segFiles=None,execute=1,keep=True,naming=1,therapist=True,backchannels=500.0,overwrite=False):
    print "\n======================================================================="
    print "\t\tConfiguration of Feature Extractor Class"
    print "======================================================================="
    AcFtEx = AcousticFeatureExtractor(segFiles,therapist)
    AcFtEx.namingOfSlices = naming  #determines the variant of naming
    AcFtEx.exeMode = execute        #determines the execution mode (default 1)
    AcFtEx.KEEP = keep
    print "Set constant KEEP to %s" %(str(keep))
    AcFtEx.OVERWRITE = overwrite
    print "Set constant OVERWRITE to %s" %(str(overwrite))

    if backchannels == 0 or backchannels == None:
        AcFtEx.RMBACKCHANNELS = False
        AcFtEx.MAXBCDURATION = 0.0
        print "Set constant RMBACKCHANNELS to False (No backchannels will be removed from segments)"
    elif not backchannels == 500.0:
        AcFtEx.MAXBCDURATION = backchannels
        print "Set the maximum duration to %s ms. Every interference below will be removed from segments" % str(AcFtEx.MAXBCDURATION)
    print "=======================================================================\n" #End of the configuration of the script

    if not (execute == 0):
        AcFtEx.executeFtExtraction()
    else:
        print "Initialized class without execution of the script."
    if not AcFtEx.KEEP:
        AcFtEx.cleanUp()
    AcFtEx.showPerformance()
if __name__ == "__main__":
    start = time.time()                                         #Performance Measure (Whole Script)
    args = parser.parse_args() #get the Commandline Arguments
    main(args.segFiles,args.executeMode, args.keep, args.naming, args.therapist,args.backchannels,args.overwrite)
    end = time.time()                                           #Performance Measure (Whole Script)
    if args.segFiles != None:
        print "Runtime of the whole script (for n=%s segmentfiles): %s seconds" %(str(len(args.segFiles)),str(end -start))     #Performance Measure (Whole Script)
    else:
        print "Runtime of the whole script (for n=%s segmentfiles): %s seconds" %("0",str(end -start))     #Performance Measure (Whole Script)

#!/usr/bin/env python
# -*- coding: utf-8 -*-
import argparse
import time
# from AcousticFeatures.ftex_utils import *
from ftex_utils import *
from acousticftextractor import AcousticFeatureExtractor
import logging
import os
#TODO: Change pyLinter snake_case naming convention for variables

'''Configuration of the Logger
        Levels:
            logging.INFO            All Messages
            logging.DEBUG           Debug Messages + Warnings
            logging.WARNING         Only Warnings'''
logFileFormatter = logging.Formatter("%(asctime)s [%(threadName)-12.12s] [%(levelname)-5.5s]  %(message)s")
logConsoleFormatter =  logging.Formatter("")
rootLogger = logging.getLogger(__name__)
rootLogger.setLevel(logging.INFO)
rootDir = os.path.dirname(os.path.abspath(__file__))
fileHandler = logging.FileHandler("{0}/{1}.log".format(rootDir, "acft"))
fileHandler.setFormatter(logFileFormatter)
rootLogger.addHandler(fileHandler)

consoleHandler = logging.StreamHandler()
consoleHandler.setFormatter(logConsoleFormatter)

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
parser.add_argument('-v','--verbose',type=str2bool, metavar = '', nargs='?', default=True,
                    help="If True prints Log Messages (Error, Warning, Info, Performance)\n"
                    "in the terminal window (Default: True)\n")
parser.add_argument('--version', action='version', version='%(prog)s 2.1\n'
                                                    'written by M.Sc. Bjoern Buedenbender (FRA UAS)')
#TODO: Consider the Creation of a logfile for the script, or look for a library that does it
#TODO: Add Parser Argument -l for getting a Log File
#TODO: Add Parser Argument for a Txt File Containing Links to the segmentTxt and the regarding audio

def main(segFiles=None,execute=1,keep=True,naming=1,therapist=True,backchannels=500.0,overwrite=False):
    #START NACHRICHT
    rootLogger.info("=======================================================================")
    rootLogger.info(bcolors.HEADER +"\t\tConfiguration of Feature Extractor Class" + bcolors.ENDC)
    rootLogger.info("=======================================================================")
    AcFtEx = AcousticFeatureExtractor(segFiles,therapist)
    AcFtEx.namingOfSlices = naming  #determines the variant of naming
    AcFtEx.exeMode = execute        #determines the execution mode (default 1)
    AcFtEx.KEEP = keep
    rootLogger.info("Set constant KEEP to %s" %(str(keep)))
    AcFtEx.OVERWRITE = overwrite
    rootLogger.info("Set constant OVERWRITE to %s" %(str(overwrite)))

    if backchannels == 0 or backchannels == None:
        AcFtEx.RMBACKCHANNELS = False
        AcFtEx.MAXBCDURATION = 0.0
        rootLogger.info("Set constant RMBACKCHANNELS to False (No backchannels will be")
        rootLogger.info("\t\tremoved from segments)")
    elif not backchannels == 500.0:
        AcFtEx.MAXBCDURATION = backchannels
        rootLogger.info("Set the Backchannel maximum duration to %s ms."  % str(AcFtEx.MAXBCDURATION))
        rootLogger.info("\t\tEvery interference below will be removed from segments")
    rootLogger.info("=======================================================================") #End of the configuration of the script

    if not (execute == 0):
        AcFtEx.executeFtExtraction()
    else:
        print "Initialized class without execution of the script."
    if not AcFtEx.KEEP:
        AcFtEx.cleanUp()
    AcFtEx.showPerformance()
    AcFtEx.testLogger()

if __name__ == "__main__":
    start = time.time()                                         #Performance Measure (Whole Script)
    args = parser.parse_args() #get the Commandline Arguments
    #determines if log mesages shall be printed in console (as well as the log file)
    if args.verbose:
        rootLogger.addHandler(consoleHandler)
    main(args.segFiles,args.executeMode, args.keep, args.naming, args.therapist,args.backchannels,args.overwrite)
    end = time.time()                                           #Performance Measure (Whole Script)
    if args.segFiles != None:
        rootLogger.info("Runtime of the whole script (for n=%s segmentfiles): %s seconds" %(str(len(args.segFiles)),str(end -start)))     #Performance Measure (Whole Script)
    else:
        rootLogger.info("Runtime of the whole script (for n=%s segmentfiles): %s seconds" %("0",str(end -start)))     #Performance Measure (Whole Script)
    rootLogger.info("END OF LOG\n\n")

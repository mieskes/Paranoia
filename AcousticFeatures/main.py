#!/usr/bin/env python
import sys
import os
# sys.pasth.append(os.path.abspath("pyAudio"))
import argparse
from pyAudioAnalysis import audioBasicIO
from pyAudioAnalysis import audioFeatureExtraction as ftExt


"""Written by Bjoern Buedenbender, 2018
first steps into getting Acoustic Features from Therapists Recorded Audio sessions"""
#TODO: Write Java Function in ClassfiyTabReader that gives a Txt File with the Audio name and TimeStamp of the Segments
#TODO: Check the Docs for pympi (getSegment or similar Function Avaiable?)
#TODO: Extract Annotations (Segments) from Elan Files
#TODO: Write a Function which retrives the Timestamps of the Therapists
#TODO: Get a Loop Running which Calculates the FeatureSets (pyAudioAnalysis) for the Therapists Segments
#TODO: Write Output into a .arff file
#TODO: Details about the .arff file Format

#Initialize a arguments parser for commandline based execution of the Script
parser = argparse.ArgumentParser(prog='AcousticFeatureExtraction',
                                 description='Extracts acoustic features from speech\n',
                                 formatter_class=argparse.RawTextHelpFormatter)
parser.add_argument('-s','--segFiles',type=str, metavar = '', nargs='*',
                    help="a single .txt file or a list seperated by \" \" \n"
                    "containing the segmetns of the speech\n"
                    "Content of the File should look like the example below:\n"
                    "-----------------------exampleTxtFile.txt---------------------\n"
                    "[NameAndPathToTheAudioFiles] Optional or give it in via a Secondlist -a\n"
                    "Therapeut Therapeut 00:00:00 000 00:00:00 000 00:00:00\n"
                    "Name of the Segment | Name of the Segment | TimeStamp Begin | TimeStamp End | Timestamp Duration")
parser.add_argument('--version', action='version', version='%(prog)s 1.0\n'
                                                    'written by M.Sc. Bjoern Buedenbender (FRA UAS)')

class AcousticFeatureExtractor:

    audioFiles = []     #List in .txt Format with the Audio files
    segmentFile = []    #List of .txt File for the Timestamp of the Segments

    def __init__(self,txtFiles=None,audioFilesList=None):
        if txtFiles != None:
            for singleTxtFile in txtFiles:
                if checkForTxtFile(singleTxtFile):
                    self.segmentFile.append(singleTxtFile)
                else:
                    print "\"%s\" is not a .txt file or doesnt exist" % singleTxtFile
        # for fileName in audioFilesList:
        #     if os.path.isfile(fileName):
        #         self.audioFiles.append(fileName)

    def parseSegmentFiles(self):
        pass
    #TODO: Write a Procedure that parses the .txt File
    #TODO: Write a Function that extracts Features for the Segments
    #TODO: Write a Function that creates an ARFF File


def main(segFiles=None):
    AcFtEx = AcousticFeatureExtractor(segFiles)

def checkForTxtFile(fileName,throwError=False):
    if os.path.isfile(fileName) and fileName.lower().endswith(".txt"):
        return True
    else:
        if throwError == True:
            raise IOError("Couldnt Find File or wasnt a txt File: \"%s\"" % fileName)
        else:
            return False

if __name__ == "__main__":
    args = parser.parse_args() #get the Commandline Arguments
    main(args.segFiles)

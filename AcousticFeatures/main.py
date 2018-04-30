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
#TODO: Write a Function which retrives the Timestamps of the Therapists
#TODO: Get a Loop Running which Calculates the FeatureSets (pyAudioAnalysis) for the Therapists Segments
#TODO: Write Output into a .arff file
#TODO: Details about the .arff file Format


class AcousticFeatureExtractor:

    audioFiles = []     #List in .txt Format with the Audio files
    segmentFiles = []    #List of .txt File for the Timestamp of the Segments

    #CONSTANTS for Fileformats
    TXT = ".txt"
    AUDIO = (".wav",".mp3")
    #TODO: Find which AudioFormats are Usable with pyAudioAnalysis

    def __init__(self,txtFiles=None,audioFiles=None):
        if txtFiles != None:
            for singleTxtFile in txtFiles:
                if isFileOfFormat(singleTxtFile,self.TXT):
                    self.segmentFiles.append(singleTxtFile)

        if audioFiles != None:
            for singleAudioFile in audioFiles:
                if isFileOfFormat(singleAudioFile,self.AUDIO):
                    self.audioFiles.append(singleAudioFile)

    def parseSegmentFiles(self):
        pass
#============================================================

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
parser.add_argument('-a','--audFiles',type=str, metavar = '', nargs='*',
                    help="a single audio file or a list seperated by \" \" \n"
                    "containing the audio files of the speech\n"
                    "----------------------Allowed file formats---------------------\n"
                    ".wav\n"
                    ".mp3 ?\n")
parser.add_argument('--version', action='version', version='%(prog)s 1.1\n'
                                                    'written by M.Sc. Bjoern Buedenbender (FRA UAS)')
#TODO: Consider the Creation of a logfile for the script, or look for a library that does it
#TODO: Add Parser Argument for a Txt File Containing Links to the segmentTxt and the regarding audio
#TODO: Write a Reader Function for the LinkTxtFile (Paths to segment and audio files)


def main(segFiles=None,audFiles=None):
    AcFtEx = AcousticFeatureExtractor(segFiles,audFiles)


def isFileOfFormat(filePath,fileFormat,throwError=False):

    #Checks if the Parameter fileFormat is Category and contains Multiple Fileformats
    #Iterates threw the Category (list)
    if isinstance(fileFormat,list) or isinstance(fileFormat,tuple):
        # print fileFormat
        for singleFileFormat in fileFormat:
            if os.path.isfile(filePath) and filePath.lower().endswith(str(singleFileFormat)):
                return True
            else:
                if throwError == True:
                    raise IOError("Couldnt Find File or wasnt a %s File: \"%s\"" % (singleFileFormat,filePath))
                else:
                    print "Couldnt Find File or wasnt a %s File: \"%s\"" % (singleFileFormat,filePath)
        return False
    else:
        if os.path.isfile(filePath) and filePath.lower().endswith(str(fileFormat)):
            return True
        else:
            if throwError == True:
                raise IOError("Couldnt Find File or wasnt a %s File: \"%s\"" % (fileFormat,filePath))
            else:
                "Couldnt Find File or wasnt a %s File: \"%s\"" % (fileFormat,filePath)
                return False

if __name__ == "__main__":
    args = parser.parse_args() #get the Commandline Arguments
    main(args.segFiles,args.audFiles)
    #cd /media/durzo/DATA1/Uni/Promotion/Code/AcousticFeatures
    #./main.py -s "/media/durzo/DATA1/Uni/Promotion/Transkription/Backup/SurfaceFeatures/Target/Sessions/rating1-3/02003_25_211216_transkription_171017.txt"

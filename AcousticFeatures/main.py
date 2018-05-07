#!/usr/bin/env python
# -*- coding: utf-8 -*-
import os
# sys.pasth.append(os.path.abspath("pyAudio"))
import argparse
from pyAudioAnalysis import audioBasicIO
from pyAudioAnalysis import audioFeatureExtraction as ftExt


"""Written by Bjoern Buedenbender, 2018
Dependencies:
    -pyAudioAnalysis"""
#TODO: Write Java Function in ClassfiyTabReader that gives a Txt File with the Audio name and TimeStamp of the Segments
#TODO: Get a Loop Running which Calculates the FeatureSets (pyAudioAnalysis) for the Therapists Segments
#TODO: Write Output into a .arff file
#TODO: segment wav Files and save the pieces (if KEEP = True, keep the files)
#TODO: Implement check if cutted wav files already exist
#TODO: Conversion mp3 to wav via pydub
#TODO: check if file is already converted



class AcousticFeatureExtractor:

    audioFiles = []     #List in .txt Format with the Audio files
    segmentFiles = []    #List of .txt File for the Timestamp of the Segments

    #CONSTANTS for Fileformats
    TXT = ".txt"
    AUDIO = (".wav",".mp3")
    KEEP = False # Keep the cutted wav files for future use
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

    def parseSegmentFile(self,singleSegmentFile,parseSequence="Therapeut\tTherapeut"):
        """Takes a .txt file and parseSquence which denotes the starting point
        of a Segment and returns a List of the Segments in the format of [Beginn] [End] [Duration]
        the function additionally returns the path to the matching audio file if specified in the first
        line of the txt file.

        Parameters
        ----------
        singleSegmentFile : String
            Path to the segment txt file.
        parseSequence : String
            Substring that indicates the start of a segment for example \"Therapeut\tTherapeut\".

        Returns
        -------
        List
            List containing all Segments in three timestamps.
        String
            Optional: Link to a referenced audio file

        """
        """GENERAL ASSUMPTIONS for this parser to work
                -timestamps are seperated by the tabulator
                -txt file contains the parseSequence"""
        listOfSegments = [] #Format: Timestamps of [Beginn] [End] [Duration]
        audioFileInSegmentTxt = ""
        lengthOfParseSequence = len(parseSequence)

        with open(singleSegmentFile,"r") as f:
            #Checks if the Location to the AudioFiles is Present saves it in local var
            firstLine = f.readline()
            if (firstLine[:1] == "[") and (firstLine[-2:-1]=="]"):
                linkToAudioFile = firstLine[1:-2] #Removing the Parser Seq [ and ] from string
                if isFileOfFormat(linkToAudioFile,self.AUDIO):
                    audioFileInSegmentTxt = linkToAudioFile
            for line in f:
                if line.find(parseSequence) != -1: #Checks if the Name segment is present in the current line
                    wordsOfLine = line[lengthOfParseSequence:].split("\t") #Seperates the timestamps by tabulator
                    listOfSegments.append((wordsOfLine[1],wordsOfLine[3],wordsOfLine[5]))
        return listOfSegments,audioFileInSegmentTxt


    def calculateAccousticFeatures(self):
        for counter,singleSegmentFile in enumerate(self.segmentFiles):
            segmentTimeStamps, audioFile = self.parseSegmentFile(singleSegmentFile)
            #TODO Add conversion from mp3 to wav
            if audioFile == "" and counter <= len(self.audioFiles): #When in the segment txt no audiofile is linked check if a list of audiofiles is given by commandline
                audioFile = self.audioFiles[counter]
            if audioFile != "":
                [Fs, x] = audioBasicIO.readAudioFile(audioFile)
                #Fs = FrameRate, x = Signal
                features = ftExt.stFeatureExtraction(x, Fs, 0.050*Fs, 0.025*Fs)
                print features

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
#TODO: Add Parser Argument -l for getting a Log File
#TODO: Add Parser Argument for -k Keep the segmented Wav Files
#TODO: Add Parser Argument for a Txt File Containing Links to the segmentTxt and the regarding audio
#TODO: Write a Reader Function for the LinkTxtFile (Paths to segment and audio files)
#TODO: Add an Argument for command line Based Execution of the Script like -x (execution)


def main(segFiles=None,audFiles=None):
    AcFtEx = AcousticFeatureExtractor(segFiles,audFiles)
    AcFtEx.calculateAccousticFeatures() #DEBUGGIN Purposes TODO DELETE


def isFileOfFormat(filePath,fileFormat,throwError=False):
    #Checks if the Parameter fileFormat is Category and contains Multiple Fileformats
    #Iterates threw the Category (list)
    if isinstance(fileFormat, (tuple, list)):
        # print fileFormat
        for singleFileFormat in fileFormat:
            if os.path.isfile(filePath) and filePath.lower().endswith(str(singleFileFormat)):
                return True
            else:
                if throwError is True:
                    raise IOError("Couldnt Find File or wasnt a %s File: \"%s\"" % (singleFileFormat, filePath))
                else:
                    print "Couldnt Find File or wasnt a %s File: \"%s\"" % (singleFileFormat, filePath)
        return False
    else:
        if os.path.isfile(filePath) and filePath.lower().endswith(str(fileFormat)):
            return True
        else:
            if throwError is True:
                raise IOError("Couldnt Find File or wasnt a %s File: \"%s\"" % (fileFormat,filePath))
            else:
                print "Couldnt Find File or wasnt a %s File: \"%s\"" % (fileFormat,filePath)
                return False
                
if __name__ == "__main__":
    args = parser.parse_args() #get the Commandline Arguments
    main(args.segFiles,args.audFiles)

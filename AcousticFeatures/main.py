#!/usr/bin/env python
# -*- coding: utf-8 -*-
import os
import argparse
from pydub import AudioSegment
from shutil import rmtree

"""Written by Bjoern Buedenbender, 2018
Dependencies:
    -pyDub"""
#TODO: Write Java Function in ClassfiyTabReader that gives a Txt File with the Audio name and TimeStamp of the Segments
#TODO: Write Output into a .arff file
#TODO: Implement check if cutted wav files already exist
#TODO: Change pyLinter snake_case naming convention for variables

class AcousticFeatureExtractor(object):
    """Class for handling the Extraction of accoustic features from speech.
    Given a segmented audio file.

    Parameters
    ----------
    txtFiles : list or string
        Content = .txt files with the segments.
    audioFiles : list or string
        if the audio source file is not defined in the txtFile with the
        segments you can additionally input the audiofiles.

    Attributes
    ----------
    TXT : string constant
        Allowed file format for text files in this class.
    segmentFiles : type
        Description of attribute `segmentFiles`.
    segmentFiles : type
        Description of attribute `segmentFiles`.
    AUDIO : string constant
        Allowed file format for audio files in this class.
    KEEP : boolean constant
        True keeps all temporary generated files (converted .wav files and
        cutted slices of the .wav files), Hint: improves performance in
        future execution of the script.
    """

    audioFiles = []     #List in .txt Format with the Audio files
    segmentFiles = []    #List of .txt File for the Timestamp of the Segments
    convertedAudioFiles = [] #List of Files that got converted

    #CONSTANTS for Fileformats
    TXT = ".txt"
    AUDIO = (".wav", ".mp3")
    KEEP = True # Keep the cutted wav files for future use
    slicesDir = ""
    namingOfSlices = 1
    parseSequence="Therapeut\tTherapeut"

    def __init__(self, txtFiles=None, audioFiles=None):
        if txtFiles != None:
            for singleTxtFile in txtFiles:
                if isFileOfFormat(singleTxtFile,self.TXT):
                    self.segmentFiles.append(singleTxtFile)

        if audioFiles != None:
            for singleAudioFile in audioFiles:
                if isFileOfFormat(singleAudioFile, self.AUDIO):
                    self.audioFiles.append(singleAudioFile)

    def parseSegmentFile(self,singleSegmentFile):
        """Takes a .txt file and parseSquence which denotes the starting point
        of a Segment and returns a List of the Segments in the format of
        [Beginn] [End] [Duration]
        the function additionally returns the path to the matching audio file
        if specified in the first line of the txt file.
        GENERAL ASSUMPTIONS for this parser to work
                -timestamps are seperated by the tabulator
                -txt file contains the parseSequence"""

        listOfSegments = [] #Format: Timestamps of [Beginn] [End] [Duration]
        audioFileInSegmentTxt = ""
        lengthOfParseSequence = len(self.parseSequence)

        with open(singleSegmentFile, "r") as f:
            #Checks if the Location to the AudioFiles is Present saves it in local var
            firstLine = f.readline()
            if (firstLine[:1] == "[") and (firstLine[-2:-1] == "]"):
                linkToAudioFile = firstLine[1:-2] #Removing the Parser Seq [ and ] from string
                if isFileOfFormat(linkToAudioFile,self.AUDIO):
                    audioFileInSegmentTxt = linkToAudioFile
            for line in f:
                if line.find(self.parseSequence) != -1: #Checks if the Name segment is present in the current line
                    wordsOfLine = line[lengthOfParseSequence:].split("\t") #Seperates the timestamps by tabulator
                    listOfSegments.append((wordsOfLine[1],wordsOfLine[3],wordsOfLine[5]))
        return listOfSegments,audioFileInSegmentTxt


    def convertToWav(self,audioFile):
        wavAudioFile = ""
        #Checks if converted wav file already exist
        wavAudioFile = os.path.splitext(audioFile)[0]+".wav"
        if not isFileOfFormat(wavAudioFile,".wav"):
            soundConverter = AudioSegment.from_file(audioFile,format=audioFile[-3:])
            # soundConverter.set_channels(1)
            fileHandle = soundConverter.export(wavAudioFile, format="wav")
            fileHandle.close()
            print "Converted .mp3 to .wav for further processing"
        else:
            print "Found already converted .wav form of .mp3, for further processing"
        return wavAudioFile

    def cleanUp(self):
        if self.convertedAudioFiles: #checks if the List is empty
            for audioFile in self.convertedAudioFiles:
                os.remove(audioFile)
                print "Deleted temporary converted .wav file"
        if os.path.exists(self.slicesDir):
            rmtree(self.slicesDir)
            print "Deleted all slices (of the original audio file)"

    def extractFeatures(self):
        pass
        #
        # [Fs, x] = audioBasicIO.readAudioFile(tmpPathSoundSlice)
        # # #Fs = FrameRate, x = Signal
        # # print Fs
        # # print x
        # #TODO: https://github.com/tyiannak/pyAudioAnalysis/issues/18 Possible Solution (Call Stereo to Mono)
        # features = ftExt.stFeatureExtraction(x, Fs, 0.050*Fs, 0.025*Fs)
        # print features

    def getSpeechOfOneSpeaker(self):
        for counter,singleSegmentFile in enumerate(self.segmentFiles):
            segmentTimeStamps, audioFile = self.parseSegmentFile(singleSegmentFile)
            #TODO: Rework If Expression 1 line down
            if audioFile == "" and counter <= len(self.audioFiles) and len(self.audioFiles) != 0:
                audioFile = self.audioFiles[counter]
                #When in the segment txt no audiofile is linked check if a list of audiofiles is given by commandline

            if audioFile != "":
                if audioFile[-3:].lower() == "mp3":  #Converts .mp3 to .wav for further processing
                    audioFile = self.convertToWav(audioFile)
                    self.convertedAudioFiles.append(audioFile)

                self.slicesDir = os.path.splitext(audioFile)[0] #Directory for slices of the wav file
                soundBuffer = AudioSegment.from_file(audioFile,format=audioFile[-3:])

                for i,singleSegment in enumerate(segmentTimeStamps):
                    tmpSoundSlice = soundBuffer[str2ms(singleSegment[0]):str2ms(singleSegment[1])]

                    if not os.path.exists(self.slicesDir):
                        #Create a Directory for all cutted segments of the wav file
                        os.makedirs(self.slicesDir)
                    if self.namingOfSlices == 1:
                        tmpPathSoundSlice = os.path.splitext(audioFile)[0]+"/segment%s.wav" %str(i).zfill(4)
                    elif self.namingOfSlices == 2:
                        tmpPathSoundSlice = os.path.splitext(audioFile)[0]+"/segment%s.wav" %str(str2ms(singleSegment[0]))
                    elif self.namingOfSlices == 3:
                        tmpPathSoundSlice = os.path.splitext(audioFile)[0]+"/segment%s-%s.wav" %(str(str2ms(singleSegment[0])),str2ms(singleSegment[1]))
                    if not isFileOfFormat(tmpPathSoundSlice,".wav"): #TODO: Consider ADD an Overwrite Parameter
                        fileHandle = tmpSoundSlice.export(tmpPathSoundSlice, format="wav")

    def executeFtExtraction(self):
        self.getSpeechOfOneSpeaker()

#============================================================

def str2ms(s):
    hr, mm, sec = map(float, s.split(':'))
    inMs = ((hr * 60 + mm) * 60 + sec) * 1000
    return int(inMs)


def str2bool(v):
    if v.lower() in ('yes', 'true', 't', 'y', '1'):
        return True
    elif v.lower() in ('no', 'false', 'f', 'n', '0'):
        return False
    else:
        raise argparse.ArgumentTypeError('Boolean value expected.')
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
                    ".wav is prefered\n"
                    ".mp3 will be converted to .wav\n")
parser.add_argument('-x','--execute',type=str2bool, metavar = '', nargs='?', default=True,
                    help="use boolean expressions like yes, true, 1 or no, false 0 to determine\n"
                    "wethere the script should directly be executed\n"
                    "default = True")
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
parser.add_argument('--version', action='version', version='%(prog)s 1.2\n'
                                                    'written by M.Sc. Bjoern Buedenbender (FRA UAS)')
#TODO: Consider the Creation of a logfile for the script, or look for a library that does it
#TODO: Add Parser Argument -l for getting a Log File
#TODO: Add Parser Argument for a Txt File Containing Links to the segmentTxt and the regarding audio
#TODO: Write a Reader Function for the LinkTxtFile (Paths to segment and audio files)
#TODO: Add an Argument for command line Based Execution of the Script like -x (execution)

def main(segFiles=None,audFiles=None,execute=True,keep=True,naming=1,therapist=True):
    AcFtEx = AcousticFeatureExtractor(segFiles,audFiles)
    AcFtEx.namingOfSlices = naming
    if keep:
        AcFtEx.KEEP = True
    else:
        AcFtEx.KEEP = False
        print "Set constant KEEP to False (Deletion of all temporary files after completion)"
    if therapist:
        AcFtEx.parseSequence = "Therapeut\tTherapeut"
    else:
        AcFtEx.parseSequence = "Patient\tPatient"
    if execute:
        AcFtEx.executeFtExtraction()
    else:
        print "Initialized class without execution of the script."

    if not AcFtEx.KEEP:
        AcFtEx.cleanUp()


def isFileOfFormat(filePath,fileFormat,throwError=False):
    #Checks if the Parameter fileFormat is Category and contains multiple fileformats
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
    else: #executed when the argument fileFormat only contains a single fileformat
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
    main(args.segFiles,args.audFiles,args.execute, args.keep, args.naming, args.therapist)

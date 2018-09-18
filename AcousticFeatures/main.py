#!/usr/bin/env python
# -*- coding: utf-8 -*-
import os
import argparse
from pydub import AudioSegment
from shutil import rmtree
import itertools
import time

"""Written by Bjoern Buedenbender, 2018
Dependencies:
    -pyDub
    -openSMILE (/usr/local/bin/SMILExtract)"""
#TODO: Add an overwrite parameter (if the already existing slices shouldnt be used)
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
    #LISTS containing paths to files
    audioFiles = []
    segmentFiles = []    #List of .txt File for the Timestamp of the Segments
    convertedAudioFiles = [] #List of Files that got converted

    #CONSTANTS for Fileformats
    TXT = ".txt"
    AUDIO = (".wav", ".mp3")
    KEEP = True # Keep the cutted wav files for future use
    THERAPIST = True #True extracts Features for Therapist, False for Patient
    SPEAKER = "T"
    RMBACKCHANNELS = True
    MAXBCDURATION = 500 #Maximum tolerable duration of backchannel in ms
    T_PARSESEQ = "Therapeut\tTherapeut"
    P_PARSESEQ = "Patient\tPatient"

    slicesDir = ""
    featuresDir = ""
    namingOfSlices = 1
    exeMode = 1
    parseSequence="Therapeut\tTherapeut"
    #[0] = Config for opensmile [1] = Inputfile [2] = Outputfile
    openSMILEsettings = ["-C OSconfig/IS12_speaker_trait.conf","",""]

    def __init__(self, txtFiles=None,therapist=True):
        if txtFiles != None:
            for singleTxtFile in txtFiles:
                if isFileOfFormat(singleTxtFile,self.TXT):
                    self.segmentFiles.append(singleTxtFile)

        self.THERAPIST = therapist #Setting selection of speaker to extract ft from
        if self.THERAPIST:
            self.parseSequence = self.T_PARSESEQ
            self.SPEAKER = "T"
        else:
            self.parseSequence = self.P_PARSESEQ
            self.SPEAKER = "P"

    def getSegments(self,singleSegmentFile):
        audioFile = ""
        listOfSegments = []
        if not self.RMBACKCHANNELS:
            listOfSegments, audioFile = self.parseSegmentFile(singleSegmentFile,self.parseSequence)
            listOfSegments = convListstr2ms(listOfSegments) #converts time-like strings 1:23:23 in time in ms
        else:
            #When Backchannels shall be removed, this gets the segments for the front channel (either the Therapist or Patient)
            #And the Backchannel (ther Otherway Arround)
            if self.THERAPIST:
                SegSpeakerTarget, audioFile = self.parseSegmentFile(singleSegmentFile,self.T_PARSESEQ)
                SegSpeakerBackchannel, audioFile = self.parseSegmentFile(singleSegmentFile,self.P_PARSESEQ)
            else:
                SegSpeakerTarget, audioFile = self.parseSegmentFile(singleSegmentFile,self.P_PARSESEQ)
                SegSpeakerBackchannel, audioFile = self.parseSegmentFile(singleSegmentFile,self.T_PARSESEQ)
            #Convert the segment timestamps from time-like strings 3:02:52 to ms
            SegSpeakerTarget = convListstr2ms(SegSpeakerTarget)
            SegSpeakerBackchannel = convListstr2ms(SegSpeakerBackchannel)
            listOfSegments = remove_ints(SegSpeakerTarget,SegSpeakerBackchannel, self.MAXBCDURATION)
        return listOfSegments,audioFile

    def parseSegmentFile(self,singleSegmentFile,parseSequence):
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
        lengthOfParseSequence = len(parseSequence)

        with open(singleSegmentFile, "r") as f:
            #Checks if the Location to the AudioFiles is Present saves it in local var
            firstLine = f.readline()
            if (firstLine[:1] == "[") and (firstLine[-2:-1] == "]"):
                print "Found audiofile reference"
                linkToAudioFile = firstLine[1:-2] #Removing the Parser Seq [ and ] from string
                if isFileOfFormat(linkToAudioFile,self.AUDIO):
                    audioFileInSegmentTxt = linkToAudioFile
            for line in f:
                if line.find(parseSequence) != -1: #Checks if the Name segment is present in the current line
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

    def extractFeatures(self,inputFile):
        self.openSMILEsettings[1] = "-I " + inputFile
        cmd_template = 'SMILExtract {config_path} {wav_path} {arff_path}'

        os.system(cmd_template.format(
            config_path=self.openSMILEsettings[0],
            wav_path=self.openSMILEsettings[1],
            arff_path=self.openSMILEsettings[2],
            ))

    def setNamesOfSlices(self,startInMS,endInMS,i):
        if self.namingOfSlices == 1:
            sliceName = self.slicesDir+"/%s-segment%s.wav" %(self.SPEAKER,str(i).zfill(4))
        elif self.namingOfSlices == 2:
            sliceName = self.slicesDir+"/%s-segment%s.wav" %(self.SPEAKER,str(startInMS))
        elif self.namingOfSlices == 3:
            sliceName = self.slicesDir+"/%s-segment%s-%s.wav" %(self.SPEAKER,str(startInMS),str(endInMS))
        return sliceName

    def getSpeechOfOneSpeaker(self,singleSegmentFile):
        #get the segment timestamps and the link to the audiofile
        segmentTimeStamps, audioFile = self.getSegments(singleSegmentFile)

        if audioFile != "":
            #Reads the complete audiofile and saves it to soundBuffer
            if audioFile[-3:].lower() == "mp3":  #Converts .mp3 to .wav for further processing
                audioFile = self.convertToWav(audioFile)
                self.convertedAudioFiles.append(audioFile)
            soundBuffer = AudioSegment.from_file(audioFile,format=audioFile[-3:])

            #Create a Directory for all cutted segments of the wav file
            self.slicesDir = os.path.splitext(audioFile)[0] +"_slicesDir"
            if not os.path.exists(self.slicesDir):
                os.makedirs(self.slicesDir)
            #Create a Directory for all feature files (.arff)
            self.featuresDir = os.path.dirname(audioFile) + "/ExtractedFeatures"
            if not os.path.exists(self.featuresDir):
                os.makedirs(self.featuresDir)
            #sets the outputname for .arff file containing the extracted features
            outputFile = self.featuresDir + "/AcFt_" +os.path.basename(audioFile)[0:-3] + "arff"
            self.openSMILEsettings[2] =  "-O " + outputFile

            '''
            NEXT STEPS:
                    -Argument for overwrite (slices) ???
                    -compare the two files in the folder /media/durzo/69FDAA69060E624F/Backup
            '''

            #variables for the audiofile (containing only speech of one speaker)
            concatAudioFile = AudioSegment.empty()
            pathConcatAudioFile = self.slicesDir  + "/only" + self.SPEAKER + "_" + os.path.basename(audioFile)[0:-3] + "wav"

            start = time.time()     #Performance Measuere (Concatenating AudioSegments)
            #Slice the Audiofile in segments (slices)
            for i,singleSegment in enumerate(segmentTimeStamps):
                tmpSoundSlice = soundBuffer[singleSegment[0]:singleSegment[1]]
                if (not self.exeMode == 3) and (not isFileOfFormat(pathConcatAudioFile,".wav",False)): #TODO: Consider ADD an Overwrite Parameter
                    concatAudioFile += tmpSoundSlice
                tmpPathSoundSlice = self.setNamesOfSlices(singleSegment[0],singleSegment[1],i)
                if not isFileOfFormat(tmpPathSoundSlice,".wav") and self.exeMode > 1: #TODO: Consider ADD an Overwrite Parameter
                    fileHandle = tmpSoundSlice.export(tmpPathSoundSlice, format="wav")
                if self.exeMode == 3:
                    self.extractFeatures(tmpPathSoundSlice)
            end = time.time()       #Performance Measuere (Concatenating AudioSegments)
            print (end -start)      #Performance Measuere (Concatenating AudioSegments)
            '''debugging: Testing Configurations of openSmile uncomment line below'''
            # self.extractFeatures("/media/durzo/69FDAA69060E624F/Uni/Promotion/Aufnahmen/02003_Therapiesitzung22und23_141216_slicesDir/T-segment0000.wav")

            if not isFileOfFormat(pathConcatAudioFile,".wav"): #TODO: Consider ADD an Overwrite Parameter
                print "Creating one audio file containing only: [%s]" % self.SPEAKER
                fileHandle = concatAudioFile.export(pathConcatAudioFile, format="wav")
                fileHandle.close()
            if not self.exeMode == 3:
                print "Initializing feature extraction based on the file\n\t:\'%s\'" %pathConcatAudioFile
                start = time.time()     #Performance Matters
                self.extractFeatures(pathConcatAudioFile)
                end = time.time()       #Performance Matters
                print (end -start)      #Performance Matters


    def executeFtExtraction(self):
        for singleSegmentFile in self.segmentFiles:
            self.getSpeechOfOneSpeaker(singleSegmentFile)

#=======================END OF CLASS===========================================

#=====================Utility functions========================================
def str2ms(s):
    hr, mm, sec = map(float, s.split(':'))
    inMs = ((hr * 60 + mm) * 60 + sec) * 1000
    return int(inMs)

def convListstr2ms(somelist):
    convertedlist = []
    for start,end,duration in somelist:
        convertedlist.append([str2ms(start),str2ms(end),str2ms(duration)])
    return convertedlist

def str2bool(v):
    if v.lower() in ('yes', 'true', 't', 'y', '1'):
        return True
    elif v.lower() in ('no', 'false', 'f', 'n', '0'):
        return False
    else:
        raise argparse.ArgumentTypeError('Boolean value expected.')

def isFileOfFormat(filePath,fileFormat,verbose=True,throwError=False):
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
                elif verbose is True:
                    print "Couldnt Find File or wasnt a %s File: \"%s\"" % (singleFileFormat, filePath)
        return False
    else: #executed when the argument fileFormat only contains a single fileformat
        if os.path.isfile(filePath) and filePath.lower().endswith(str(fileFormat)):
            return True
        else:
            if throwError is True:
                raise IOError("Couldnt Find File or wasnt a %s File: \"%s\"" % (fileFormat,filePath))
            elif verbose is True:
                print "Couldnt Find File or wasnt a %s File: \"%s\"" % (fileFormat,filePath)
                return False

#==========================================================================================00
#The 3 Functions below belong together:
#   remove_ints takes a List A from which all overlaps with a second list (intervalls), shall
#   be removed if the overlap is bigger than maxDuration (Default: 500 ms)
#Used in PARANOIA Project to subtract backchannels from the segments of Speaker 1
#==========================================================================================00
def remove_ints(listA,intervals,maxDuration=500):
    cleanedListA = []
    for targetSegment in listA:
        overlapsFound=[]

        for s_interval in intervals:
            tmp = range_intersect(targetSegment,s_interval)
            if tmp != None:
                overlapsFound.append(tmp)
        if len(overlapsFound)>0:
            # Remove all overlaps which are longer then maxDuration
            overlapsFound = [ elem for elem in overlapsFound if elem[2] <= maxDuration] #500 MAX Duration für Backchannel
            cleanedListA.append(cut_ints(overlapsFound, targetSegment[0], targetSegment[1]))
        else:
            cleanedListA.append([targetSegment])
    cleanedListA = list(itertools.chain(*cleanedListA))
    return cleanedListA

def cut_ints(intervals, mn, mx):
    results = []
    next_start = mn
    for x in intervals:
        if next_start < x[0]:
            results.append([next_start,x[0]])
            next_start = x[1]
        elif next_start < x[1]:
            next_start = x[1]
    if next_start < mx:
        results.append([next_start, mx])
    return results

def range_intersect(x,y):
    z = (max(x[0],y[0]),min(x[1],y[1]))
    if (z[0] < z[1]):
        return [z[0], z[1], z[1] - z[0]] # to make this an inclusive range

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
                    "\t3: Extract features based on the slices")
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

parser.add_argument('--version', action='version', version='%(prog)s 2.0\n'
                                                    'written by M.Sc. Bjoern Buedenbender (FRA UAS)')
#TODO: Consider the Creation of a logfile for the script, or look for a library that does it
#TODO: Add Parser Argument -l for getting a Log File
#TODO: Add Parser Argument for a Txt File Containing Links to the segmentTxt and the regarding audio

def main(segFiles=None,execute=1,keep=True,naming=1,therapist=True,backchannels=500.0):
    AcFtEx = AcousticFeatureExtractor(segFiles,therapist)
    AcFtEx.namingOfSlices = naming  #determines the variant of naming
    AcFtEx.exeMode = execute        #determines the execution mode (default 1)
    if keep:
        AcFtEx.KEEP = True
    else:
        AcFtEx.KEEP = False
        print "Set constant KEEP to False (Deletion of all temporary files after completion)"
    if backchannels == 0 or backchannels == None:
        AcFtEx.RMBACKCHANNELS = False
        AcFtEx.MAXBCDURATION = 0.0
        print "Set constant RMBACKCHANNELS to False (No backchannels will be removed from segments)"
    elif not backchannels == 500.0:
        AcFtEx.MAXBCDURATION = backchannels
        print "Set the maximum duration to %s ms. Every interference below will be removed from segments" % str(AcFtEx.MAXBCDURATION)
    if not (execute == 0):
        AcFtEx.executeFtExtraction()
    else:
        print "Initialized class without execution of the script."
    if not AcFtEx.KEEP:
        AcFtEx.cleanUp()
if __name__ == "__main__":
    start = time.time()                                         #Performance Measure (Whole Script)
    args = parser.parse_args() #get the Commandline Arguments
    main(args.segFiles,args.executeMode, args.keep, args.naming, args.therapist,args.backchannels)
    end = time.time()                                           #Performance Measure (Whole Script)
    print "Runtime of the whole script:" + str(end -start)      #Performance Measure (Whole Script)

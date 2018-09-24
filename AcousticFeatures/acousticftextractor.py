from ftex_utils import *
import os
from pydub import AudioSegment
import time
from shutil import rmtree, copy2
import logging

logger = logging.getLogger(__name__)

"""Written by Bjoern Buedenbender, 2018
Dependencies:
    -pyDub
    -openSMILE (/usr/local/bin/SMILExtract)"""

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
    executionTime4oneSegmentFile = [] #Performance meassure
    perfConcatenating = []  #Performance meassure
    perfFtExtraction = []  #Performance meassure

    #CONSTANTS for Fileformats
    TXT = ".txt"
    AUDIO = (".wav", ".mp3")
    OVERWRITE = False #Overwrite all old files (converted mp3, slices, etc)
    KEEP = True # Keep the cutted wav files for future use
    THERAPIST = True #True extracts Features for Therapist, False for Patient
    SPEAKER = "T"
    RMBACKCHANNELS = True
    MAXBCDURATION = 500 #Maximum tolerable duration of backchannel in ms
    T_PARSESEQ = "Therapeut\tTherapeut"
    P_PARSESEQ = "Patient\tPatient"

    slicesDir = ""
    featuresDir = ""
    dataDir = "data"
    rootDir = ""
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
        #creates a dir in the root of this script named data
        self.rootDir = os.path.dirname(os.path.abspath(__file__))
        self.dataDir = self.rootDir + "/data"
        if not os.path.exists(self.dataDir):
            os.makedirs(self.dataDir)
            logger.info("Created the directory for the workingdata: "+self.dataDir)

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
                logger.info("Found audiofile reference")
                linkToAudioFile = firstLine[1:-2] #Removing the Parser Seq [ and ] from string
                if isFileOfFormat(linkToAudioFile,self.AUDIO):
                    audioFileInSegmentTxt = linkToAudioFile
            else:
                logger.warning("No audiofile reference found in the segmentfile(\"%s\")" %singleSegmentFile)
                logger.info("Please add a reference (absolut path) in the first line of the segmentfile in brackets i.e.: [/path/to/the/audiofile.wav]")
            for line in f:
                if line.find(parseSequence) != -1: #Checks if the Name segment is present in the current line
                    wordsOfLine = line[lengthOfParseSequence:].split("\t") #Seperates the timestamps by tabulator
                    listOfSegments.append((wordsOfLine[1],wordsOfLine[3],wordsOfLine[5]))
        return listOfSegments,audioFileInSegmentTxt


    def convertToWav(self,audioFile):
        wavAudioFile = self.dataDir +"/" + os.path.splitext(os.path.basename(audioFile))[0]+".wav"
        #Checks if converted wav file already exist
        if not isFileOfFormat(wavAudioFile,".wav"):
            soundConverter = AudioSegment.from_file(audioFile,format=audioFile[-3:])
            # soundConverter.set_channels(1)
            fileHandle = soundConverter.export(wavAudioFile, format="wav")
            fileHandle.close()
            logger.info("Converted .mp3 to .wav for further processing")
        else:
            logger.info("Found already converted .wav form of .mp3, for further processing")
        return wavAudioFile

    def cleanUp(self):
        if self.convertedAudioFiles: #checks if the List is empty
            for audioFile in self.convertedAudioFiles:
                os.remove(audioFile)
                logger.info("Deleted temporary converted .wav file")
        if os.path.exists(self.slicesDir):
            rmtree(self.slicesDir)
            logger.info("Deleted all slices (of the original audio file)")

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
            else:
                copy2(audioFile,self.dataDir)
                logger.info("Moved a copy of audiofile (%s) to the data (working dir of the script)" %audioFile)
                audioFile = self.dataDir + "/" + os.path.basename(audioFile)
            soundBuffer = AudioSegment.from_file(audioFile,format=audioFile[-3:])

            #Create a Directory for all cutted segments of the wav file
            self.slicesDir = os.path.splitext(audioFile)[0] +"_slicesDir"
            if not os.path.exists(self.slicesDir):
                os.makedirs(self.slicesDir)
            #Create a Directory for all feature files (.arff)
            self.featuresDir = self.rootDir + "/ExtractedFeatures"
            if not os.path.exists(self.featuresDir):
                os.makedirs(self.featuresDir)
            #sets the outputname for .arff file containing the extracted features
            outputFile = self.featuresDir + "/AcFt_" +os.path.basename(audioFile)[0:-3] + "arff"
            self.openSMILEsettings[2] =  "-O " + outputFile
            #variables for the audiofile (containing only speech of one speaker)
            concatAudioFile = AudioSegment.empty()
            pathConcatAudioFile = self.slicesDir  + "/only" + self.SPEAKER + "_" + os.path.basename(audioFile)[0:-3] + "wav"

            start = time.time()     #Performance Measuere (Concatenating AudioSegments)
            #Slice the Audiofile in segments (slices)
            performanceBuffer = 0   #Performance Measure (For exeMode = 3 adding up all extractiontimes)
            for i,singleSegment in enumerate(segmentTimeStamps):
                tmpSoundSlice = soundBuffer[singleSegment[0]:singleSegment[1]]
                if (not isFileOfFormat(pathConcatAudioFile,".wav",False) or self.OVERWRITE) and (not self.exeMode == 3):
                    concatAudioFile += tmpSoundSlice
                tmpPathSoundSlice = self.setNamesOfSlices(singleSegment[0],singleSegment[1],i)
                if (not isFileOfFormat(tmpPathSoundSlice,".wav",False) or self.OVERWRITE) and self.exeMode > 1 :
                    fileHandle = tmpSoundSlice.export(tmpPathSoundSlice, format="wav")
                if self.exeMode == 3:
                    self.extractFeatures(tmpPathSoundSlice)
            end = time.time()       #Performance Measuere (Concatenating AudioSegments)
            self.perfConcatenating.append(end -start)
            logger.info("Performance concatenating to one audiofile (1 Speaker): %s seconds" %str((end -start)))      #Performance Measuere (Concatenating AudioSegments)

            if not isFileOfFormat(pathConcatAudioFile,".wav") or self.OVERWRITE:
                logger.info("Creating one audio file containing only: [%s]" % self.SPEAKER)
                fileHandle = concatAudioFile.export(pathConcatAudioFile, format="wav")
                fileHandle.close()
            if not self.exeMode == 3:
                logger.info("Initializing feature extraction based on the file\n\t:\'%s\'" %pathConcatAudioFile)
                start = time.time()     #Performance Matters
                self.extractFeatures(pathConcatAudioFile)
                end = time.time()       #Performance Matters
                self.perfFtExtraction.append(end -start)
                logger.info("Performance extracting features: %s seconds" %str((end -start)))      #Performance Matters


    def executeFtExtraction(self):
        for i,singleSegmentFile in enumerate(self.segmentFiles):
            print i
            logger.info("=======================================================================")
            logger.info("\t\tProcessing the [%s] segmentfile:" %str(i+1).zfill(2))
            logger.info(singleSegmentFile)
            logger.info("=======================================================================")
            start = time.time()                                                                               #Performance Measure (single segmentfile)
            self.getSpeechOfOneSpeaker(singleSegmentFile)
            end = time.time()                                                                                 #Performance Measure (single segmentfile)
            self.executionTime4oneSegmentFile.append([singleSegmentFile,str(end -start)])                     #Performance Measure (single segmentfile)
            logger.info("Execution of file (%s) in %s seconds" % (singleSegmentFile,str(end -start)))               #Performance Measure (single segmentfile)

    def showPerformance(self):
        #TODO: Add Performance Meassures for: Rendering of the "1 Speaker Audio File" and Konvertierung
        if self.exeMode != 0:
            logger.info("===================" + bcolors.HEADER + "Performance / Segmentfile" + bcolors.ENDC + "===========================")
            if self.exeMode != 3:
                for perf,perfFt,perfConc in zip(self.executionTime4oneSegmentFile,self.perfFtExtraction,self.perfConcatenating):
                    logger.info("Performance for: \t\t" + perf[0])
                    logger.info("Feature extraction (in s): \t" + str(perfFt))
                    logger.info("Concatenating (in s): \t\t" + str(perfConc))
                    logger.info("Complete Execution (in s): \t" + bcolors.OKGREEN + str(perf[1]) + bcolors.ENDC)
                    logger.info(" ")
            else: #in exeMode 3 no concatenating happens, instaed concatenating PF is replaced by feature extraction time
                for perf,perfConc in zip(self.executionTime4oneSegmentFile,self.perfConcatenating):
                    logger.info("Performance for: \t\t" + perf[0])
                    logger.info("Feature extraction (in s): \t" + str(perfConc))
                    logger.info("Complete Execution (in s): \t" + bcolors.OKGREEN + str(perf[1]) + bcolors.ENDC)
                    logger.info(" ")
            print "======================================================================="
#=======================END OF CLASS===========================================

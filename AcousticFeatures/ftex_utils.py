#!/usr/bin/env python
# -*- coding: utf-8 -*-

#Feature Extractor Utility Functions
'''Written by Bjoern Buedenbender, 2018'''
import argparse
import os
import itertools
#=====================Utility Classes========================================
class bcolors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'
#=====================Utility Functions========================================
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

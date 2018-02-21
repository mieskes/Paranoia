package de.hda.paranoia.nlp.pipeline.customComponent;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

/**
 * Created by Andreas on 20.09.2017.
 */
public class SpeechBreakFeatureExtractor extends FeatureExtractorResource_ImplBase
        implements DocumentFeatureExtractor {

    /**
     * The String is added to features as description of the value 'number of speech brake with one seconds' in Weka.
     */
    public static final String NR_SB_ONE_SEC = "nrSpeechBreakOneSec";

    /**
     * The String is added to features as description of the value 'number of speech brake with two seconds' in Weka.
     */
    public static final String NR_SB_TWO_SEC = "nrSpeechBreakTwoSec";

    /**
     * The String is added to features as description of the value 'number of speech brake with three seconds' in Weka.
     */
    public static final String NR_SB_THREE_SEC = "nrSpeechBreakThreeSec";

    /**
     * The String is added to features as description of the value 'number of speech brake with four seconds' in Weka.
     */
    public static final String NR_SB_FOUR_SEC = "nrSpeechBreakFourSec";

    /**
     * The String is added to features as description of the value 'number of speech brake with five seconds' in Weka.
     */
    public static final String NR_SB_FIVE_SEC = "nrSpeechBreakFiveSec";

    /**
     * The String is added to features as description of the value 'number of speech brake with grater then five seconds'
     * in Weka.
     */
    public static final String NR_SB_GREATER_FIVE_SEC = "nrSpeechBreakGreaterFiveSec";

    /**
     * The String is added to features as description of the value 'total numbers of speech breaks' in Weka.
     */
    public static final String NR_TOTAL_BREAKS = "nrTotalBreaks";

    /**
     * The String is added to features as description of the value 'relative frequency of speech brake with one second'
     * in Weka.
     */
    public static final String REL_FREQ_SB_ONE_SEC = "relFreqSpeechBreakOneSec";

    /**
     * The String is added to features as description of the value 'relative frequency of speech brake with two seconds'
     * in Weka.
     */
    public static final String REL_FREQ_SB_TWO_SEC = "relFreqSpeechBreakTwoSec";

    /**
     * The String is added to features as description of the value 'relative frequency of speech brake with three seconds'
     * in Weka.
     */
    public static final String REL_FREQ_SB_THREE_SEC = "relFreqSpeechBreakThreeSec";

    /**
     * The String is added to features as description of the value 'relative frequency of speech brake with four seconds'
     * in Weka.
     */
    public static final String REL_FREQ_SB_FOUR_SEC = "relFreqSpeechBreakFourSec";

    /**
     * The String is added to features as description of the value 'relative frequency of speech brake with five seconds'
     * in Weka.
     */
    public static final String REL_FREQ_SB_FIVE_SEC = "relFreqSpeechBreakFiveSec";

    /**
     * The String is added to features as description of the value 'relative frequency of speech brake with
     * grater then five seconds' in Weka.
     */
    public static final String REL_FREQ_SB_GREATER_FIVE_SEC = "relFreqSpeechBreakGreaterFiveSec";

    /**
     * The String is added to features as description of the value 'relative frequency of total Number of speech brakes'
     * in Weka.
     */
    public static final String REL_FREQ_BREAKS = "relFreqNrOfBreaks";

    /**
     * Contains the  number of speech brake with one seconds.
     */
    private int nrSpeechBreakOneSec;

    /**
     * Contains the  number of speech brake with two seconds.
     */
    private int nrSpeechBreakTwoSec;

    /**
     * Contains the number of speech brake with three seconds.
     */
    private int nrSpeechBreakThreeSec;

    /**
     * Contains the number of speech brake with four seconds.
     */
    private int nrSpeechBreakFourSec;

    /**
     * Contains the number of speech brake with five seconds.
     */
    private int nrSpeechBreakFiveSec;

    /**
     * Contains the number of speech brake with grater then five seconds.
     */
    private int nrSpeechBreakGreaterFiveSec;

    /**
     * Contains the total numbers of speech breaks.
     */
    private int totalNrOfBreaks;

    /**
     * Contains the relative frequency of speech brake with one second.
     */
    private int relFreqSpeechBreakOneSec;

    /**
     * Contains the relative frequency of speech brake with two seconds.
     */
    private int relFreqSpeechBreakTwoSec;

    /**
     * Contains the relative frequency of speech brake with three seconds.
     */
    private int relFreqSpeechBreakThreeSec;

    /**
     * Contains the relative frequency of speech brake with four seconds.
     */
    private int relFreqSpeechBreakFourSec;

    /**
     * Contains the relative frequency of speech brake with five seconds
     */
    private int relFreqSpeechBreakFiveSec;

    /**
     * Contains the relative frequency of speech brake with greater then five seconds.
     */
    private int relFreqSpeechBreakGreaterFiveSec;

    /**
     * Contains the relative frequency of the total number of speech breaks.
     */
    private int relFreqNrOfBreaks;

    /**
     * Contains the time as Date.
     */
    private Date time;

    /**
     * Contains the date as String.
     */
    private String date;

    /**
     * Contains the sum of time.
     */
    private long sum = 0;

    /**
     * Contains the average time.
     */
    private long avgTime = 0;



    /**
     *
     * @param aJCas Contains the classified texts from the .txt files
     * @return a list of Features
     * @throws TextClassificationException
     */
    public List<Feature> extract(JCas aJCas)
            throws TextClassificationException {



        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("CET"));


        List<Feature> featList = new ArrayList<Feature>();
        String [] text = aJCas.getDocumentText().split(" ");
        String regexTime = Pattern.quote("(p:") + "(\\d\\d:\\d\\d:\\d\\d)" + Pattern.quote(")") + ".{0,1}";
        Pattern breakWithTime2 = Pattern.compile(regexTime);

        try {

            String date3;
            for (String word : text) {

                if (word.matches(Pattern.quote("(.)") + ".{0,1}")) {
                    time = sdf.parse("00:00:01");
                    sum += time.getTime();
                    nrSpeechBreakOneSec++;
                }

                if (word.matches(Pattern.quote("(..)") + ".{0,1}")) {
                    nrSpeechBreakTwoSec++;
                    time = sdf.parse("00:00:02");
                    sum += time.getTime();
                }

                if (word.matches(Pattern.quote("(...)") + ".{0,1}")) {
                    nrSpeechBreakThreeSec++;
                    time = sdf.parse("00:00:03");
                    sum += time.getTime();

                }

                if (word.matches(Pattern.quote("(....)") + ".{0,1}")) {
                    nrSpeechBreakFourSec++;
                    time = sdf.parse("00:00:04");
                    sum += time.getTime();
                }

                if (word.matches(Pattern.quote("(.....)"))) {
                    nrSpeechBreakFiveSec++;
                    time = sdf.parse("00:00:05");
                    sum += time.getTime();

                }


                if (breakWithTime2.matcher(word).matches()) {
                    Matcher m = breakWithTime2.matcher(word);
                    while (m.find()) {
                        time = sdf.parse(m.group(1));
                        sum += time.getTime();
                    }
                    nrSpeechBreakGreaterFiveSec++;

                }


            }
        }catch (Exception e){
            e.getMessage();
        }

        totalNrOfBreaks = nrSpeechBreakOneSec + nrSpeechBreakTwoSec + nrSpeechBreakThreeSec + nrSpeechBreakFourSec
                            + nrSpeechBreakFiveSec + nrSpeechBreakGreaterFiveSec;


        sdf.applyPattern("ss.SSS");
        if(sum > 0 && totalNrOfBreaks > 0) {
            avgTime = sum / totalNrOfBreaks;
             date = sdf.format(new Date(avgTime));
        }
        Collection<Token> tokenCol = JCasUtil.select(aJCas, Token.class);




        relFreqSpeechBreakOneSec = nrSpeechBreakOneSec / tokenCol.size();
        relFreqSpeechBreakTwoSec = nrSpeechBreakTwoSec / tokenCol.size();
        relFreqSpeechBreakThreeSec = nrSpeechBreakThreeSec / tokenCol.size();
        relFreqSpeechBreakFourSec = nrSpeechBreakFourSec / tokenCol.size();
        relFreqSpeechBreakFiveSec = nrSpeechBreakFiveSec / tokenCol.size();
        relFreqSpeechBreakGreaterFiveSec = nrSpeechBreakGreaterFiveSec / tokenCol.size();
        relFreqNrOfBreaks = totalNrOfBreaks / tokenCol.size();


        featList.add(new Feature(NR_SB_ONE_SEC, nrSpeechBreakOneSec));
        featList.add(new Feature(NR_SB_TWO_SEC, nrSpeechBreakTwoSec));
        featList.add(new Feature(NR_SB_THREE_SEC, nrSpeechBreakThreeSec));
        featList.add(new Feature(NR_SB_FOUR_SEC, nrSpeechBreakFourSec));
        featList.add(new Feature(NR_SB_FIVE_SEC, nrSpeechBreakFiveSec));
        featList.add(new Feature(NR_SB_GREATER_FIVE_SEC, nrSpeechBreakGreaterFiveSec));
        featList.add(new Feature(NR_TOTAL_BREAKS, totalNrOfBreaks));



        featList.add(new Feature(REL_FREQ_SB_ONE_SEC, relFreqSpeechBreakOneSec));
        featList.add(new Feature(REL_FREQ_SB_TWO_SEC, relFreqSpeechBreakFourSec));
        featList.add(new Feature(REL_FREQ_SB_THREE_SEC, relFreqSpeechBreakThreeSec));
        featList.add(new Feature(REL_FREQ_SB_FOUR_SEC, relFreqSpeechBreakFourSec));
        featList.add(new Feature(REL_FREQ_SB_FIVE_SEC, relFreqSpeechBreakFiveSec));
        featList.add(new Feature(REL_FREQ_SB_GREATER_FIVE_SEC, relFreqSpeechBreakGreaterFiveSec));
        featList.add(new Feature(REL_FREQ_BREAKS, relFreqNrOfBreaks));


        return featList;
    }


    public int getNrSpeechBreakOneSec() {
        return nrSpeechBreakOneSec;
    }

    public int getNrSpeechBreakTwoSec() {
        return nrSpeechBreakTwoSec;
    }

    public int getNrSpeechBreakThreeSec() {
        return nrSpeechBreakThreeSec;
    }

    public int getNrSpeechBreakFourSec() {
        return nrSpeechBreakFourSec;
    }

    public int getNrSpeechBreakFiveSec() {
        return nrSpeechBreakFiveSec;
    }

    public int getNrSpeechBreakGreaterFiveSec() {
        return nrSpeechBreakGreaterFiveSec;
    }

    public int getTotalNrOfBreaks() {
        return totalNrOfBreaks;
    }

    public Date getTime() {
        return time;
    }


}

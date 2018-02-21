package de.hda.paranoia.nlp.pipeline.customComponent;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Andreas on 20.09.2017.
 */
public class StutterFeatureExtractor extends FeatureExtractorResource_ImplBase
        implements DocumentFeatureExtractor {

    /**
     * The String is added to features as description of the value 'number of incomplete stuttered words' in Weka.
     */
    public static final String NR_ST_WORD_INCOMPLETE = "nrOfStutterWordsIncomplete";

    /**
     * The String is added to features as description of the value 'number of incomplete stuttered sentence' in Weka.
     */
    public static final String NR_ST_SENTENCE_INCOMPLETE = "nrOfStutterSentenceIncomplete";

    /**
     * The String is added to features as description of the value 'relative frequency of incomplete stuttered words'
     * in Weka.
     */
    public static final String REL_FREQ_ST_WORD_INCOMPLETE = "relFreqOfStutterWordsIncomplete";

    /**
     * The String is added to features as description of the value 'relative frequency of incomplete stuttered sentence'
     * in Weka.
     */
    public static final String REL_FREQ_ST_SENTENCE_INCOMPLETE = "relFreqOfStutterSentenceIncomplete";

    /**
     * The String is added to features as description of the value 'total number of stutters' in Weka.
     */
    public static final String NR_ST_TOTAL = "nrOfStutterTotal";

    /**
     * Contains the number of incomplete stuttered words.
     */
    private int nrOfStutterWordsIncomplete;

    /**
     * Contains the the number of incomplete stuttered sentence.
     */
    private int nrOfStutterSentenceIncomplete;

    /**
     * Contains relative frequency of incomplete stuttered words.
     */
    private int relFreqOfStutterWordsIncomplete;

    /**
     * Contains the relative frequency of incomplete stuttered sentence.
     */
    private int relFreqOfStutterSentenceIncomplete;

    /**
     * Contains the total number of stutters
     */
    private int totalNumber;


    /**
     *
     * @param aJCas Contains the classified texts from the .txt files
     * @return a list of Features
     * @throws TextClassificationException
     */
    public List<Feature> extract(JCas aJCas)
            throws TextClassificationException {

        List<Feature> featList = new ArrayList<Feature>();

        Pattern IncompleteStutterPattern = Pattern.compile("[A-Za-z]\\-");
        Pattern SentenceBreakPattern = Pattern.compile(".{1,20}([A-Za-z])\\-");
        String [] text = aJCas.getDocumentText().split(" ");

        for(String word : text){
            if(IncompleteStutterPattern.matcher(word).matches()){
                nrOfStutterWordsIncomplete++;
            }

            if(SentenceBreakPattern.matcher(word).matches()){
                nrOfStutterSentenceIncomplete++;
            }
        }

        Collection<Token> tokenCol = JCasUtil.select(aJCas, Token.class);
        Collection<Sentence> sentCol = JCasUtil.select(aJCas, Sentence.class);
        totalNumber = nrOfStutterWordsIncomplete + nrOfStutterSentenceIncomplete;

        relFreqOfStutterWordsIncomplete =   nrOfStutterWordsIncomplete / tokenCol.size();
        relFreqOfStutterSentenceIncomplete = nrOfStutterSentenceIncomplete / sentCol.size();

        featList.add(new Feature(NR_ST_TOTAL, totalNumber));
        featList.add(new Feature(NR_ST_WORD_INCOMPLETE, nrOfStutterWordsIncomplete));
        featList.add(new Feature(NR_ST_SENTENCE_INCOMPLETE, nrOfStutterSentenceIncomplete));
        featList.add(new Feature(REL_FREQ_ST_WORD_INCOMPLETE, relFreqOfStutterWordsIncomplete));
        featList.add(new Feature(REL_FREQ_ST_SENTENCE_INCOMPLETE, relFreqOfStutterSentenceIncomplete));

        return featList;
    }

    public int getNrOfStutterWordsIncomplete() {
        return nrOfStutterWordsIncomplete;
    }

    public int getNrOfStutterSentenceIncomplete() {
        return nrOfStutterSentenceIncomplete;
    }

    public int getTotalNumber() {
        return totalNumber;
    }


}

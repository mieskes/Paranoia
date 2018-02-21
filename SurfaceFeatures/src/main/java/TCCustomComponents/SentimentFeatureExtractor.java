package TCCustomComponents;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Andreas on 02.10.2017.
 * @// TODO: 11.10.2017 Under Construction
 */
public class SentimentFeatureExtractor extends FeatureExtractorResource_ImplBase
        implements DocumentFeatureExtractor {

    /**
     * The String is added to features as description of the value 'number of negative segments' in Weka.
     */
    public static final String NR_OF_NEGATIVE_SENTIMENTS = "nrOfNegativeSentiment";

    /**
     * The String is added to features as description of the value 'number of positive segments' in Weka.
     */
    public static final String NR_OF_POSITIVE_SENTIMENTS = "nrOfPositiveSentiment";

    /**
     * The String is added to features as description of the value 'relative frequency of negative segments' in Weka.
     */
    public static final String REL_FREQ_OF_NEGATIVE_SENTIMENTS = "relFreqOfNegativeSentiment";

    /**
     * The String is added to features as description of the value 'relative frequency of positive segments' in Weka.
     */
    public static final String REL_FREQ_POSITIVE_SENTIMENTS = "relFreqPositiveSentiment";

    /**
     * Contains the number of negative segments.
     */
    private int nrOfNegativeSentiment = 0;

    /**
     * Contains the number of positive segments.
     */
    private int nrOfPositiveSentimen = 0;

    /**
     * Contains the relative frequency of negative segments.
     */
    private double relFreqOfNegativeSentiment;

    /**
     * Contains the relative frequency of positive segments.
     */
    private double relFreqPositiveSentiment;

    /**
     * Loads the resource path
     */
    private static ClassLoader classloader = Thread.currentThread().getContextClassLoader();

    /**
     *
     * @param aJCas Contains the classified texts from the .txt files
     * @return a list of Features
     * @throws TextClassificationException
     */
    public List<Feature> extract(JCas aJCas)
            throws TextClassificationException {

        List<Feature> featList = new ArrayList<Feature>();
        List<String> posSentimentList = new ArrayList<String>();
        List<String> negSentimentList = new ArrayList<String>();


        try{

            //Loading a the list which contains negative Sentiments
            InputStream is = classloader.getResourceAsStream("data/sentiment/german_sentiment_list_negative.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while((line = reader.readLine()) != null){
                negSentimentList.add(line);

            }

            reader.close();

            //Loading the list of which contains positive Sentiments
            line = null;
            is = classloader.getResourceAsStream("data/sentiment/german_sentiment_list_positive.txt");
            reader = new BufferedReader(new InputStreamReader(is));
            while((line = reader.readLine()) != null){
                posSentimentList.add(line);

            }
            reader.close();

            String text = aJCas.getDocumentText();
            for(String negSentiment :negSentimentList) {
                Matcher m = Pattern.compile("\\b"+Pattern.quote(negSentiment) + "\\b").matcher(text);
                while (m.find()){
                    nrOfNegativeSentiment++;
                }

            }



            for(String posSentiment :posSentimentList){
                Matcher m = Pattern.compile("\\b"+Pattern.quote(posSentiment) + "\\b").matcher(text);
                while(m.find()){
                    nrOfPositiveSentimen++;
                }

            }


        }catch (Exception e){
            e.getMessage();
        }

        Collection<Token> tokenCol = JCasUtil.select(aJCas, Token.class);

        relFreqOfNegativeSentiment = nrOfNegativeSentiment / tokenCol.size();
        relFreqPositiveSentiment = nrOfPositiveSentimen / tokenCol.size();

        featList.add(new Feature(NR_OF_NEGATIVE_SENTIMENTS,nrOfNegativeSentiment));
        featList.add(new Feature(NR_OF_POSITIVE_SENTIMENTS,nrOfPositiveSentimen));
        featList.add(new Feature(REL_FREQ_OF_NEGATIVE_SENTIMENTS,relFreqOfNegativeSentiment));
        featList.add(new Feature(REL_FREQ_POSITIVE_SENTIMENTS,relFreqPositiveSentiment));

        negSentimentList.clear();
        posSentimentList.clear();

        return featList;
    }

    /**
     *
     * @return
     */
    public int getNrOfNegativeSentiment() {
        return nrOfNegativeSentiment;
    }

    /**
     *
     * @return
     */
    public int getNrOfPositiveSentimen() {
        return nrOfPositiveSentimen;
    }



    }

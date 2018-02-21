package de.hda.paranoia.nlp.pipeline.customComponent;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Andreas on 02.10.2017.
 */
public class NoiseFeatureExtractor extends FeatureExtractorResource_ImplBase
        implements DocumentFeatureExtractor {

    /**
     * The String is added to features as description of the value 'number of incomprehensible Paragraphs' in Weka.
     */
    public static final String NR_INCOMPREHENSIBLE_PARAGRAPH_SPEECH = "nrIncomprehensibleParagraphCount";

    /**
     * The String is added to features as description of the value 'number of incomprehensible speech' in Weka.
     */
    public static final String NR_INCOMPREHENSIBLE_SPEECH = "nrIncomprehensibleCount";

    /**
     * The String is added to features as description of the value 'number of incomprehensible with interpretation'
     * in Weka.
     */
    public static final String NR_INCOMPREHENSIBLE_SPEECH_INTER = "nrIncomprehensibleInterCount";

    /**
     * The String is added to features as description of the value 'relative frequency of incomprehensible speech'
     * in Weka.
     */
    public static final String REL_FREQ_INCOMPREHENSIBLE_SPEECH = "relFreqIncomprehensibleCount";

    /**
     * The String is added to features as description of the value 'relative frequency of incomprehensible speech
     * with interpretation' in Weka.
     */
    public static final String REL_FREQ_INCOMPREHENSIBLE_SPEECH_INTER = "relFreqIncomprehensibleInterCount";

    /**
     * Contains Number of incomprehensible Paragraphs
     */
    private int incomprehensibleParagraphCount = 0;

    /**
     * Contains number of incomprehensible speech
     */
    private int incomprehensibleCount = 0;

    /**
     * Contains number of incomprehensible with interpretation
     */
    private int incomprehensibleInterCount = 0;

    /**
     * Contains the total number of all noise events
     */
    private int sum;

    /**
     * Contains relative frequency of incomprehensible speech
     */
    private double relFreqIncomprehensibleCount;

    /**
     * Contains relative frequency of incomprehensible speech
     */
    private double relFreqIncomprehensibleInterCount;

    /**
     * Contains Java matcher
     */
    private  Matcher m;

    /**
     * Contains pattern for incomprehensible speech with interpretation
     */
    private Pattern incompInter = Pattern.compile(Pattern.quote("(?:") + "(.*?)" + Pattern.quote(")") + ".{0,1}");

    /**
     * Contains pattern for incomprehensible
     */
    private Pattern incomp = Pattern.compile(Pattern.quote("/")+ ".{0,1}");

    /**
     * Contains pattern for incomprehensible paragraph
     */
    private Pattern incompPara = Pattern.compile(Pattern.quote("(unverständlich)") + ".{0,1}");

    /**
     *
     * @param aJCas Contains the classified texts from the .txt files
     * @return a list of Features
     * @throws TextClassificationException
     */
    public List<Feature> extract(JCas aJCas)
            throws TextClassificationException {


        List<Feature> featList = new ArrayList<Feature>();
        String text = aJCas.getDocumentText();

        m = incompPara.matcher(text);
        while (m.find()){
            incomprehensibleParagraphCount++;
        }


        m = incomp.matcher(text);
        while (m.find()){
            incomprehensibleCount++;
        }



        m = incompInter.matcher(text);
        while (m.find()){
            incomprehensibleInterCount++;
        }
        Collection<Token> tokenCol = JCasUtil.select(aJCas, Token.class);
        relFreqIncomprehensibleCount = incomprehensibleCount / tokenCol.size();
        relFreqIncomprehensibleInterCount = incomprehensibleInterCount / tokenCol.size();

        sum = incomprehensibleParagraphCount + incomprehensibleCount + incomprehensibleInterCount;
        featList.add(new Feature(NR_INCOMPREHENSIBLE_PARAGRAPH_SPEECH,incomprehensibleParagraphCount));
        featList.add(new Feature(NR_INCOMPREHENSIBLE_SPEECH,incomprehensibleCount));
        featList.add(new Feature(NR_INCOMPREHENSIBLE_SPEECH_INTER,incomprehensibleInterCount));
        featList.add(new Feature(REL_FREQ_INCOMPREHENSIBLE_SPEECH,relFreqIncomprehensibleCount));
        featList.add(new Feature(REL_FREQ_INCOMPREHENSIBLE_SPEECH_INTER,relFreqIncomprehensibleInterCount));
        return featList;
    }



    public Integer getSumOfNoise(){

        return sum;
    }

    public Integer getIncomprehensibleParagraphCount() {
        return incomprehensibleParagraphCount;
    }

    public Integer getIncomprehensibleCount() {
        return incomprehensibleCount;
    }

    public Integer getIncomprehensibleInterCount() {
        return incomprehensibleInterCount;
    }


}

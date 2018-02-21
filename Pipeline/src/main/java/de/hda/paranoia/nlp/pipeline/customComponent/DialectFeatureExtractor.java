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
 * Created by Andreas on 21.09.2017.
 */
public class DialectFeatureExtractor  extends FeatureExtractorResource_ImplBase
        implements DocumentFeatureExtractor {


    /**
     * The String is added to features as description of the value 'number of words with dialect' in Weka.
     */
    public static final String NR_DIALECT_INST = "nrDialectWords";

    /**
     * The String is added to features as description of the value 'relative frequency of words with dialect' in Weka.
     */
    public static final String REL_FREQ_DIALECT_INST = "relFreqDialectWords";

    /**
     * The number of words with dialect
     */
    private int numberOfDialects;

    /**
     * The relative frequency of words with dialect
     */
    private double relFreqDialectWords;

    /**
     *
     * @param aJCas Contains the classified texts from the .txt files.
     * @return a list of Features.
     * @throws TextClassificationException
     */
    public List<Feature> extract(JCas aJCas)
            throws TextClassificationException {
        List<Feature> featList = new ArrayList<Feature>();

        // Creates a pattern that finds translated words form dialect into 'Hochdeutsch'.

        String regexTime = Pattern.quote("(hd: ") + "(.*?)" + Pattern.quote(")");
        Pattern dialectPattern = Pattern.compile(regexTime);
        String text = aJCas.getDocumentText();




                Matcher m = dialectPattern.matcher(text);
                while(m.find()) {
                    numberOfDialects++;

                }


        Collection<Token> tokenCol = JCasUtil.select(aJCas, Token.class);
        relFreqDialectWords =   numberOfDialects /  tokenCol.size();

        featList.add(new Feature(NR_DIALECT_INST, numberOfDialects));
        featList.add(new Feature(REL_FREQ_DIALECT_INST, relFreqDialectWords));
        return featList;
    }



}

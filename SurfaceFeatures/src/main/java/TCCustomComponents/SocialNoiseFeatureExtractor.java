package TCCustomComponents;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Andreas on 20.09.2017.
 */
public class SocialNoiseFeatureExtractor extends FeatureExtractorResource_ImplBase
        implements DocumentFeatureExtractor {

    /**
     * The String is added to features as description of the value 'number of approval' in Weka.
     */
    public static final String NR_SN_APROVAL = "ApprovalNr";

    /**
     * The String is added to features as description of the value 'number of disapproval' in Weka.
     */
    public static final String NR_SN_DISAPROVAL = "DisapprovalNr";

    /**
     * The String is added to features as description of the value 'number of hesitation' in Weka.
     */
    public static final String NR_SN_HESITANTLY = "HesitantlyNr";

    /**
     * The String is added to features as description of the value 'number of Exclamation' in Weka.
     */
    public static final String NR_SN_EXCLAMATION = "ExclamationWordNr";

    /**
     * The String is added to features as description of the value 'number of Questions' in Weka.
     */
    public static final String NR_SN_QUESTIONING = "QuestioningNr";

    /**
     * The String is added to features as description of the value 'relative frequency of approval' in Weka.
     */
    public static final String REL_FREQ_SN_APROVAL = "relFreqApproval";

    /**
     * The String is added to features as description of the value 'relative frequency of disapproval' in Weka.
     */
    public static final String REL_FREQ_DISAPROVAL = "relFreqDisapproval";

    /**
     * The String is added to features as description of the value 'relative frequency of hesitation' in Weka.
     */
    public static final String REL_FREQ_SN_HESITANTLY = "relFreqHesitantly";

    /**
     * The String is added to features as description of the value 'relative frequency of exclamation' in Weka.
     */
    public static final String REL_FREQ_SN_EXCLAMATION = "relFreqExclamationWord";

    /**
     * The String is added to features as description of the value 'relative frequency of questions' in Weka.
     */
    public static final String REL_FREQ_SN_QUESTIONING = "relFrequestioning";

    /**
     * The String is added to features as description of the value 'relative frequency of social noise' in Weka.
     */
    public static final String REL_FREQ_SN = "relSn";

    /**
     * The String is added to features as description of the value 'total number of social noise' in Weka.
     */
    public static final String NR_SN_TOTAL = "relSnTotal";


    private int snCount;

    /**
     * Contains the number of social noise.
     */
    private double relSn;

    /**
     * Contains number of exclamation.
     */
    private int exclamation;

    /**
     * Contains the number of approval.
     */
    private int aproval;

    /**
     * Contains the number of disapproval.
     */
    private int disaproval;

    /**
     * Contains the number of hesitation
     */
    private int hesitantly;

    /**
     *  Contains the number of questions
     */
    private int questioning;

    /**
     *
     * @param aJCas Contains the classified texts from the .txt files
     * @return a list of Features
     * @throws TextClassificationException
     */
    public List<Feature> extract(JCas aJCas)
            throws TextClassificationException {

        List<Feature> featList = new ArrayList<Feature>();

        for (Token token : JCasUtil.select(aJCas, Token.class)){

            if(token.getCoveredText().matches("hm-hm") || token.getCoveredText().matches("Hm")
                    ||  token.getCoveredText().matches("uh-huh") || token.getCoveredText().matches("ja")){

                    aproval++;
            }

            if(token.getCoveredText().matches("nah") || token.getCoveredText().matches("hm-mm")
                    ||  token.getCoveredText().matches("uh-uh")){
                disaproval++;
            }

            if(token.getCoveredText().matches("eh") || token.getCoveredText().matches("em")
                    ||  token.getCoveredText().matches("ähm") || token.getCoveredText().matches("er")
                    || token.getCoveredText().matches("oh") || token.getCoveredText().matches("uh")){
                hesitantly++;
            }

            if(token.getCoveredText().matches("ach") || token.getCoveredText().matches("aha")
                    ||  token.getCoveredText().matches("ahh") || token.getCoveredText().matches("oh")
                    || token.getCoveredText().matches("oooh") || token.getCoveredText().matches("oops")
                    || token.getCoveredText().matches("puh") || token.getCoveredText().matches("ups")
                    || token.getCoveredText().matches("wow")){
                exclamation++;
            }


            if(token.getCoveredText().matches("hmm") || token.getCoveredText().matches("oh")
                    ||  token.getCoveredText().matches("huh")){
                questioning++;
            }

            if(token.getCoveredText().matches("\\[sn")){

                snCount++;
            }



        }


        Collection<Token> tokenCol = JCasUtil.select(aJCas, Token.class);


        int relFreqSnApproval = aproval / tokenCol.size();
        int relFreqSnDisapproval = disaproval / tokenCol.size();
        int relFreqSnHesitantly = hesitantly / tokenCol.size();
        int relFreqExclamation = exclamation / tokenCol.size();
        int relFreqQuestioning = questioning / tokenCol.size();
        int relFreqTotal = aproval + disaproval + hesitantly + exclamation + questioning;
            relFreqTotal /= tokenCol.size();

        relSn = (double)snCount / (double)tokenCol.size();

        DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
        DecimalFormat df = new DecimalFormat("####.##");
        sym.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(sym);

        relSn = Double.parseDouble(df.format(relSn));

        System.out.println(relSn+" "+snCount + " " + tokenCol.size());

        featList.add(new Feature(NR_SN_APROVAL, aproval));
        featList.add(new Feature(NR_SN_DISAPROVAL, disaproval));
        featList.add(new Feature(NR_SN_HESITANTLY, hesitantly));
        featList.add(new Feature(NR_SN_EXCLAMATION, exclamation));
        featList.add(new Feature(NR_SN_QUESTIONING, questioning));


        featList.add(new Feature(REL_FREQ_SN_APROVAL, relFreqSnApproval));
        featList.add(new Feature(REL_FREQ_DISAPROVAL, relFreqSnDisapproval));
        featList.add(new Feature(REL_FREQ_SN_HESITANTLY, relFreqSnHesitantly));
        featList.add(new Feature(REL_FREQ_SN_EXCLAMATION, relFreqExclamation));
        featList.add(new Feature(REL_FREQ_SN_QUESTIONING, relFreqQuestioning));
        featList.add(new Feature(NR_SN_TOTAL, relFreqTotal));
        featList.add(new Feature(REL_FREQ_SN, relSn));

        return featList;
    }

    /**
     *
     * @return
     */
    public Integer getExclamation() {
        return exclamation;
    }

    /**
     *
     * @return
     */
    public Integer getAproval() {
        return aproval;
    }

    /**
     *
     * @return
     */
    public Integer getDisaproval() {
        return disaproval;
    }

    /**
     *
     * @return
     */
    public Integer getHesitantly() {
        return hesitantly;
    }

    /**
     *
     * @return
     */
    public Integer getQuestioning() {
        return questioning;
    }



}

package TCCustomComponents;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
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
 * "src/main/resources/data/liwc/"
 + "LIWC_de_classes.txt"
 * Created by Andreas on 19.10.2017.
 */
public class EmotionFeatureExtractor extends FeatureExtractorResource_ImplBase
        implements DocumentFeatureExtractor {

    /**
     * Parameter description for liwc class directory.
     */
    public static final String PARAM_LIWC_CLASS_DIR = "liwcClassFileDir";

    /**
     *
     */
    @ConfigurationParameter(name = PARAM_LIWC_CLASS_DIR, mandatory = false,
            description = "Description", defaultValue = "true")
    private String liwcClassFileDir;


    /**
     * Contains the filepath for LIWC_de_classes.txt
     */
    private static ClassLoader classloader = Thread.currentThread().getContextClassLoader();

    /**
     * Contains LIWC classes
     */
    private List<LiwcClasses> liwcClassList = new ArrayList<LiwcClasses>();

    /**
     * Contains LIWC Words
     */
    private List<LiwcWords> liwcWordList = new ArrayList<LiwcWords>();

    /**
     * loads LIWC classes
     * @throws Exception
     */
    public void loadLiwcClasses() throws Exception{
        String [] tabArray = null;
        InputStream is = classloader.getResourceAsStream("data/liwc/LIWC_de_classes.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while((line = reader.readLine()) != null){
            tabArray = line.split("\t");
            liwcClassList.add(new LiwcClasses(tabArray[0], tabArray[1]));
        }
    }

    /**
     * loads LIWC words
     * @throws Exception
     */
    public void loadLiwcWords() throws Exception{
        String [] tabArray = null;

        InputStream is = classloader.getResourceAsStream("data/liwc/LIWC_de.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while((line = reader.readLine()) != null){
            List<String> ids = new ArrayList<String>();
            tabArray = line.split("\t");

            for(int i = 1; i< tabArray.length; i++){
                ids.add(tabArray[i]);
            }

            liwcWordList.add(new LiwcWords(tabArray[0], ids));
        }
    }

    /**
     *
     * @param aJCas Contains the classified texts from the .txt files
     * @return a list of Features
     * @throws TextClassificationException
     */
    public List<Feature> extract(JCas aJCas)
            throws TextClassificationException{

        List<Feature> featList = new ArrayList<Feature>();
        List<Feature> tempFeatList = new ArrayList<Feature>();
        try {

                loadLiwcWords();
                loadLiwcClasses();

        }catch (Exception e){
            e.getMessage();
        }

        for(LiwcClasses liwcClasses: liwcClassList){
            featList.add(new Feature("nr" + liwcClasses.getName(), new Integer(0)));
        }


        String text = aJCas.getDocumentText();

        //searches for all LIWC words in a text and adds texts class name and count of the word to features
        for(LiwcWords liwcWords : liwcWordList) {

            Pattern p = Pattern.compile( "\\b" +liwcWords.getWord() + "\\b" );
            Matcher m = p.matcher(text);
            while (m.find()) {


                    for (LiwcClasses liwcClasses : liwcClassList) {
                        for (int id : liwcWords.getIds()) {
                            if (liwcClasses.getId() == id) {

                                for (Feature feature : featList) {

                                    if (feature.getName().equals("nr" + liwcClasses.getName())) {

                                        feature.setValue((Integer) feature.getValue() + 1);
                                    }
                                }
                            }
                        }
                    }
                }
            }


        Collection<Token> tokenCol = JCasUtil.select(aJCas, Token.class);
            for(Feature feature :featList){
                tempFeatList.add(new Feature("relFreq" + feature.getName(), ((Integer) feature.getValue()).doubleValue() /  ((Integer) tokenCol.size()).intValue()));
            }

        featList.addAll(tempFeatList);
        tempFeatList.clear();
        liwcClassList.clear();
        liwcWordList.clear();
        return featList;
    }

    /**
     *
     * @return
     */
    public List<LiwcClasses> getLiwcClassList() {

        return liwcClassList;
    }

    /**
     *
     * @return
     */
    public List<LiwcWords> getLiwcWordList() {
        return liwcWordList;
    }


}

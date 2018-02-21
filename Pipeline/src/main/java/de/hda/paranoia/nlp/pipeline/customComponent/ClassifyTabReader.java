package de.hda.paranoia.nlp.pipeline.customComponent;



import de.tudarmstadt.ukp.dkpro.tc.api.io.TCReaderSingleLabel;

import java.io.File;
import java.net.URL;

import java.lang.Override;
import java.lang.Double;
import java.util.*;


import java.io.InputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;


import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.UimaContext;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;



/**
 * Created by Andreas on 12.09.2017.
 */
public class ClassifyTabReader extends JCasCollectionReader_ImplBase implements TCReaderSingleLabel{


    /**
     * Path to the file containing the sentences
     */
    public static final String PARAM_ELAN_TAB_FILE = "elanTabFile";
    @ConfigurationParameter(name = PARAM_ELAN_TAB_FILE, mandatory = true,
            description = "Description", defaultValue = "true")
    private String elanTabFile;

    public static final String PARAM_OUTCOME_CLASS_FILTER = "classFilter";
    @ConfigurationParameter(name = PARAM_OUTCOME_CLASS_FILTER, mandatory = false,
            description = "Description", defaultValue = "true")

    /**
     * Contains class filter string
     */
    private String classFilter;

    /**
     * Contains offset for classication.
     */
    private int offset;

    /**
     * Contains document textlines.
     */
    private List<TextLine> result= new ArrayList<TextLine>();

    /**
     * Reads .txt File by Line
     * @param context connects the resource with the pipeline
     * @throws ResourceInitializationException
     */
    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);
        String[] tabArray = null;
        try {


            URL resourceUrl = ResourceUtils.resolveLocation(elanTabFile, this, context);

            InputStream is = resourceUrl.openStream();


            for (String lines : FileUtils.readLines(new File(resourceUrl.toURI()), "utf-8")) {
                tabArray = lines.split("\t");
                if(classFilter.matches("true")){

                if (tabArray.length == 9) {

                    result.add(new TextLine(tabArray[0], Double.parseDouble(tabArray[3]), tabArray[6], tabArray[8]));
                }
                }else{
                    if (tabArray.length == 9 && tabArray[0].matches(classFilter)) {
                        result.add(new TextLine(tabArray[0], Double.parseDouble(tabArray[3]), tabArray[6], tabArray[8]));
                    }
                }
            }

            Collections.sort(result, new Comparator<TextLine>() {

                public int compare(TextLine array1, TextLine array2) {

                    return Double.compare(array1.getBegin(), array2.getBegin());
                }
            });

            //addTime();
            printTestSet();

            is.close();
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
        offset = 0;
    }

    /**
     *
     * @return offset < TextArray
     * @throws IOException
     * @throws CollectionException
     */

    public boolean hasNext()
            throws IOException, CollectionException
   {
        return offset < result.size();
    }

    /**
     * Saves the .txt File into JCas
     * @param aJCas aJCas Contains the classified texts from the .txt files.
     * @throws IOException
     * @throws CollectionException
     */
    @Override
    public void getNext(JCas aJCas)
            throws IOException, CollectionException
    {

        String[] tabArray = null;

        aJCas.setDocumentText("(duration:"+result.get(offset).getDuration()+")" + " " +result.get(offset).getText());
        System.out.println("(duration:"+result.get(offset).getDuration()+")" + " " +result.get(offset).getText());
        DocumentMetaData dmd = DocumentMetaData.create(aJCas);
        dmd.setCollectionId("Sitzung" + String.valueOf(offset));

        dmd.setDocumentTitle("ELANTranscribtion" + offset);
        dmd.setDocumentBaseUri("ELANTranscribtion");
        dmd.setDocumentUri(dmd.getDocumentBaseUri().toString() + offset);
        dmd.setDocumentId(String.valueOf(offset));

        TextClassificationOutcome outcome = new TextClassificationOutcome(aJCas);
        outcome.setOutcome(getTextClassificationOutcome(aJCas));
        outcome.addToIndexes();

        offset++;


        int lineCount = offset % 1;
        if( lineCount == 0){
        System.out.println("Number of processed lines: " + offset);
        }
        

    }

    /**
     *
     * @param aJCas aJCas Contains the classified texts from the .txt files.
     * @return class name for classification
     * @throws CollectionException
     */
    public String getTextClassificationOutcome(JCas aJCas)
            throws CollectionException
    {
        return result.get(offset).getSpeaker();
    }



    public void printTestSet(){

        double testPercent;
        testPercent = result.size() * 0.1;

    }

    /**
     *
     * @return progress
     */
    public Progress[] getProgress()
    {
        return new Progress[] { new ProgressImpl(offset, result.size(), "Lines") };
    }



}



package main;

import TCCustomComponents.ClassifyDocumentReader;
import TCCustomComponents.TCComponants;

import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

/**
 * Created by Andreas on 18.12.2017.
 */
public class main
{

    /**
     * Implementation of custom .txt Reader for file classification
     */
    private static CollectionReaderDescription elanTabReader;

    /**
     * Implementation of OpenNlpSegmenter
     */
    private static AnalysisEngineDescription tokenizer;

    /**
     * Implementation of OpenNlpPosTagger
     */
    private static AnalysisEngineDescription posTagger;

    /**
     * Implementation of custom designed UIMA and DKPro Components
     */
    private static TCComponants tcComponents = new TCComponants();

    /**
     *
     * @param args
     */
    public static void main(String[] args)
    {


        String date;

        /**
         * Start time of the Program
         */
        long startTine = System.currentTimeMillis();
        try
        {

            System.setProperty("org.apache.uima.logger.class",
                    "org.apache.uima.util.impl.Log4jLogger_impl");

            /**
             * Loads custom designed UIMA and DKPro Components
             */
            tcComponents.createExtractFeaturesConnector();

            /**
             * Initialization of .txt Reader for file classification
             */
            elanTabReader = createReaderDescription(ClassifyDocumentReader.class,
                    ClassifyDocumentReader.PARAM_ELAN_TAB_FILE, "src/main/resources/sessions/"
            );

            /**
             * Initialization of OpenNlpSegmenter
             */
            tokenizer = createEngineDescription(OpenNlpSegmenter.class,
                    OpenNlpSegmenter.PARAM_LANGUAGE, "de"
            );

            /**
             * Initialization of OpenNlpPosTagger
             */
            posTagger = createEngineDescription(OpenNlpPosTagger.class,
                    OpenNlpPosTagger.PARAM_LANGUAGE, "de"
            );


            /**
             * Using UIMA SimplePipeline method runPipeline to start the experiment
             */
            SimplePipeline.runPipeline(elanTabReader, tokenizer, posTagger, tcComponents.getExtractFeaturesConnector());


            /**
             * This Block will show a Massage and the duration of the program runtime when finished
             */
            System.out.println("Pipline finished");
            long endTime = System.currentTimeMillis();
            long runtime = endTime - startTine;
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            date = sdf.format(runtime);
            System.out.println(date);

        } catch (Exception e)
        {
            e.getMessage();
        }

    }

}

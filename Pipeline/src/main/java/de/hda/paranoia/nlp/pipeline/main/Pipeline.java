package de.hda.paranoia.nlp.pipeline.main;
/**
 * Created by Andreas on 12.08.2017.
 */
import de.hda.paranoia.nlp.pipeline.customComponent.ClassifyTabReader;
import de.hda.paranoia.nlp.pipeline.customComponent.TCComponants;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.collection.CollectionReaderDescription;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.*;
public class Pipeline {


    private static CollectionReaderDescription elanTabReader;
    private static AnalysisEngineDescription  tokenizer;
    private static AnalysisEngineDescription  posTagger;
    private static TCComponants tcComponants = new TCComponants();
    private static ClassLoader classloader = Thread.currentThread().getContextClassLoader();

    public static void main(String[] args) {
        String date;


        /**
         * Start time of the Program
         */
        long startTine = System.currentTimeMillis();
        try {
            /**
             * Loads custom designed UIMA and DKPro Components
             */
            tcComponants.createExtractFeaturesConnector();

            /**
             * Initialization of .txt Reader for file classification
             */
            elanTabReader  = createReaderDescription(ClassifyTabReader.class,
                    ClassifyTabReader.PARAM_ELAN_TAB_FILE, classloader.getResource("merge/Merged-File.txt")
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
            posTagger = createEngineDescription( OpenNlpPosTagger.class,
                    OpenNlpPosTagger.PARAM_LANGUAGE, "de"
            );


            /**
             * Using UIMA SimplePipeline method runPipeline to start the experiment
             */
            SimplePipeline.runPipeline(elanTabReader,tokenizer, posTagger, tcComponants.getExtractFeaturesConnector());

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
        }catch (Exception e){
            e.getMessage();
        }



    }



}

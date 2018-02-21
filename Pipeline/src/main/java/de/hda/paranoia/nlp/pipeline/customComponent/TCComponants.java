package de.hda.paranoia.nlp.pipeline.customComponent;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.ExternalResourceFactory.createExternalResourceDescription;

import de.tudarmstadt.ukp.dkpro.tc.weka.writer.WekaDataWriter;

import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfSentencesDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.syntax.QuestionsRatioFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.syntax.SuperlativeRatioFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.syntax.POSRatioFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.syntax.PastVsFutureFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfTokensDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.length.NrOfCharsDFE;
import de.tudarmstadt.ukp.dkpro.tc.features.style.LongWordsFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.features.style.NumberWordsFeatureExtractor;


import de.tudarmstadt.ukp.dkpro.tc.core.task.uima.ExtractFeaturesConnector;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;

import static java.util.Arrays.asList;

/**
 * Created by Andreas on 13.09.2017.
 */
public class TCComponants {



    private AnalysisEngineDescription extractFeaturesConnector;


    public TCComponants(){

    }

    public void createExtractFeaturesConnector() throws Exception
    {

        try{

            /**
             * Insert Dkpro or custom made UIMA Components in this method.
             * @throws Exception
             */
            AnalysisEngineDescription extractFeaturesConnector = createEngineDescription(ExtractFeaturesConnector.class,
                    ExtractFeaturesConnector.PARAM_OUTPUT_DIRECTORY, "../",
                    ExtractFeaturesConnector.PARAM_DATA_WRITER_CLASS, WekaDataWriter.class,
                    ExtractFeaturesConnector.PARAM_LEARNING_MODE, Constants.LM_SINGLE_LABEL,
                    ExtractFeaturesConnector.PARAM_FEATURE_MODE, Constants.FM_DOCUMENT,
                    ExtractFeaturesConnector.PARAM_ADD_INSTANCE_ID, false,
                    ExtractFeaturesConnector.PARAM_FEATURE_EXTRACTORS, asList(createExternalResourceDescription(NrOfTokensDFE.class),
                            createExternalResourceDescription(NumberWordsFeatureExtractor.class),
                            createExternalResourceDescription(NrOfSentencesDFE.class),
                            createExternalResourceDescription(POSRatioFeatureExtractor.class),
                            createExternalResourceDescription(NrOfCharsDFE.class),
                            createExternalResourceDescription(LongWordsFeatureExtractor.class),
                            createExternalResourceDescription(PastVsFutureFeatureExtractor.class),
                            createExternalResourceDescription(QuestionsRatioFeatureExtractor.class),
                            createExternalResourceDescription(SuperlativeRatioFeatureExtractor.class),
                            createExternalResourceDescription(SocialNoiseFeatureExtractor.class),
                            createExternalResourceDescription(SpeechBreakFeatureExtractor.class),
                            createExternalResourceDescription(StutterFeatureExtractor.class),
                            createExternalResourceDescription(DialectFeatureExtractor.class),
                            createExternalResourceDescription(DurationFeatureExtractor.class),
                            createExternalResourceDescription(NoiseFeatureExtractor.class),
                            createExternalResourceDescription(SentimentFeatureExtractor.class),
                            createExternalResourceDescription(EmotionFeatureExtractor.class)

                    )

            );


            this.extractFeaturesConnector = extractFeaturesConnector;
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     *
     * @return extractFeaturesConnector
     */
    public AnalysisEngineDescription getExtractFeaturesConnector() {
        return extractFeaturesConnector;
    }


}

package de.hda.paranoia.nlp.pipeline.customComponent;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.junit.Assert.*;

/**
 * Created by Andreas on 17.10.2017.
 */
public class StutterFeatureExtractorTest {

    private StutterFeatureExtractor stutterFeatureExtractor;

    @Before
    public void setUp() throws Exception {
        System.setProperty("org.apache.uima.logger.class",
                "org.apache.uima.util.impl.Log4jLogger_impl");

        stutterFeatureExtractor = new StutterFeatureExtractor();
    }

    @After
    public void tearDown() throws Exception {
        stutterFeatureExtractor = null;
    }

    @Test
    public void extract(){

        try {
            AnalysisEngine engine = createEngine(BreakIteratorSegmenter.class);
            JCas aJCas = engine.newJCas();
            aJCas.setDocumentText("I- I- I- ich ha- ha- einen sch- sch- schönen Tage ge- ge- habt.");
            engine.process(aJCas);
            List<Feature> features = new ArrayList<Feature>(stutterFeatureExtractor.extract(aJCas));
            Assert.assertEquals(5, features.size());
            assertEquals(3, stutterFeatureExtractor.getNrOfStutterWordsIncomplete());
            assertEquals(6, stutterFeatureExtractor.getNrOfStutterSentenceIncomplete());


        }catch (Exception e){
            e.getMessage();
        }


    }

}
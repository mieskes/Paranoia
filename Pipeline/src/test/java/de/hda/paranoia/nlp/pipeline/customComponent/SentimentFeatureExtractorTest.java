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

/**
 * Created by Andreas on 12.10.2017.
 */
public class SentimentFeatureExtractorTest {
    private SentimentFeatureExtractor sentimentFeatureExtractor;
    @Before
    public void setUp() throws Exception {
        System.setProperty("org.apache.uima.logger.class",
                "org.apache.uima.util.impl.Log4jLogger_impl");
         sentimentFeatureExtractor = new SentimentFeatureExtractor();
    }

    @After
    public void tearDown() throws Exception {
        SentimentFeatureExtractor sentimentFeatureExtractor = null;
    }

    @Test
    public void extract() throws Exception{
        AnalysisEngine engine = createEngine(BreakIteratorSegmenter.class);
        JCas aJCas = engine.newJCas();
        aJCas.setDocumentText("Ich lehne dein dummes Geschwätz ab. Das Leben ist schön!");
        engine.process(aJCas);
        List<Feature> features = new ArrayList<Feature>(sentimentFeatureExtractor.extract(aJCas));

        Assert.assertEquals(4, features.size());
        Assert.assertEquals(1, sentimentFeatureExtractor.getNrOfNegativeSentiment());
        Assert.assertEquals(0, sentimentFeatureExtractor.getNrOfPositiveSentimen());



    }

}
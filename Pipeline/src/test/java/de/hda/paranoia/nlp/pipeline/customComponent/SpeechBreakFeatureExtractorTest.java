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
 * Created by Andreas on 09.10.2017.
 */
public class SpeechBreakFeatureExtractorTest {

    private SpeechBreakFeatureExtractor speechBreakFeatureExtractor;

    @Before
    public void setUp() throws Exception {
        System.setProperty("org.apache.uima.logger.class",
                "org.apache.uima.util.impl.Log4jLogger_impl");
        speechBreakFeatureExtractor = new SpeechBreakFeatureExtractor();
    }

    @After
    public void tearDown() throws Exception {
        speechBreakFeatureExtractor = null;
    }

    @Test
    public void extract() throws Exception{
        AnalysisEngine engine = createEngine(BreakIteratorSegmenter.class);
        JCas aJCas = engine.newJCas();
        aJCas.setDocumentText("Dieser (.) enthält (...) mehrere (.....) Pausen (....), die (..) eine Gesamtzeit von (p:00:10:21) von 00:25:21 ergeben.");
        engine.process(aJCas);

        List<Feature> features = new ArrayList<Feature>(speechBreakFeatureExtractor.extract(aJCas));
        Assert.assertEquals(14, features.size());
        Assert.assertEquals(1, speechBreakFeatureExtractor.getNrSpeechBreakOneSec());
        Assert.assertEquals(1, speechBreakFeatureExtractor.getNrSpeechBreakTwoSec());
        Assert.assertEquals(1, speechBreakFeatureExtractor.getNrSpeechBreakThreeSec());
        Assert.assertEquals(1, speechBreakFeatureExtractor.getNrSpeechBreakFourSec());
        Assert.assertEquals(1, speechBreakFeatureExtractor.getNrSpeechBreakFiveSec());
        Assert.assertEquals(1, speechBreakFeatureExtractor.getNrSpeechBreakGreaterFiveSec());
        Assert.assertEquals(6, speechBreakFeatureExtractor.getTotalNrOfBreaks());
       // Assert.assertEquals(1, speechBreakFeatureExtractor.getTime());

    }

}
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
 * Created by Andreas on 05.10.2017.
 */
public class SocialNoiseFeatureExtractorTest {

    private SocialNoiseFeatureExtractor socialNoiseFeatureExtractor;

    @Before
    public void setUp() throws Exception {
        System.setProperty("org.apache.uima.logger.class",
                "org.apache.uima.util.impl.Log4jLogger_impl");
        socialNoiseFeatureExtractor = new SocialNoiseFeatureExtractor();
    }

    @After
    public void tearDown() throws Exception {
        socialNoiseFeatureExtractor = null;
    }

    @Test
    public void extract() throws Exception {
        AnalysisEngine engine = createEngine(BreakIteratorSegmenter.class);
        JCas aJCas = engine.newJCas();
        aJCas.setDocumentText("uh-huh das ist aber toll. hm-mm ja. ähm hörst du mir überhaupt zu? hmm? na. wow...");
        engine.process(aJCas);

        List<Feature> features = new ArrayList<Feature>(socialNoiseFeatureExtractor.extract(aJCas));
        Assert.assertEquals(11, features.size());
        assertEquals(new Integer(2), socialNoiseFeatureExtractor.getAproval());
        assertEquals(new Integer(1), socialNoiseFeatureExtractor.getDisaproval());
        assertEquals(new Integer(1), socialNoiseFeatureExtractor.getExclamation());
        assertEquals(new Integer(1), socialNoiseFeatureExtractor.getHesitantly());
        assertEquals(new Integer(1), socialNoiseFeatureExtractor.getQuestioning());



    }

}
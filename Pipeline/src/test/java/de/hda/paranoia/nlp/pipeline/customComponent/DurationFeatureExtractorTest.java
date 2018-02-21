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
 * Created by Andreas on 04.10.2017.
 */
public class DurationFeatureExtractorTest {

    private DurationFeatureExtractor durationFeatureExtractor;
    @Before
    public void setUp() throws Exception {
        System.setProperty("org.apache.uima.logger.class",
                "org.apache.uima.util.impl.Log4jLogger_impl");
        durationFeatureExtractor = new DurationFeatureExtractor();
    }

    @After
    public void tearDown() throws Exception {
        durationFeatureExtractor = null;
    }

    @Test
    public void extract() throws Exception{
        AnalysisEngine engine = createEngine(BreakIteratorSegmenter.class);
        JCas aJCas = engine.newJCas();
        aJCas.setDocumentText("(duration:00:00:12.123) Dauer des Gesprächs.");
        engine.process(aJCas);

        List<Feature> features = new ArrayList<Feature>(durationFeatureExtractor.extract(aJCas));
        Assert.assertEquals(1, features.size());
        Assert.assertEquals(new Double(12.123), features.get(0).getValue());



    }

}
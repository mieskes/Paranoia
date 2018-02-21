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
public class NoiseFeatureExtractorTest {

    private NoiseFeatureExtractor noiseFeatureExtractor;

    @Before
    public void setUp() throws Exception {
        System.setProperty("org.apache.uima.logger.class",
                "org.apache.uima.util.impl.Log4jLogger_impl");

         noiseFeatureExtractor = new NoiseFeatureExtractor();
    }

    @After
    public void tearDown() throws Exception {
        noiseFeatureExtractor = null;
    }

    @Test
    public void extract() throws Exception{

        AnalysisEngine engine = createEngine(BreakIteratorSegmenter.class);
        JCas aJCas = engine.newJCas();
        aJCas.setDocumentText(" / Das / ist (?:nicht) zu verstehen. (unverständlich)");
        engine.process(aJCas);

        List<Feature> features = new ArrayList<Feature>(noiseFeatureExtractor.extract(aJCas));
        Assert.assertEquals(5, features.size());


        Assert.assertEquals(new Integer(4), noiseFeatureExtractor.getSumOfNoise());
        Assert.assertEquals(new Integer(1), noiseFeatureExtractor.getIncomprehensibleParagraphCount());
        Assert.assertEquals(new Integer(2), noiseFeatureExtractor.getIncomprehensibleCount());
        Assert.assertEquals(new Integer(1), noiseFeatureExtractor.getIncomprehensibleInterCount());


        for (Feature feature : features){
            System.out.println(feature.asList());
        }



    }

}
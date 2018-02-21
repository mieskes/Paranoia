package de.hda.paranoia.nlp.pipeline.customComponent;

import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;

/**
 * Created by Andreas on 19.10.2017.
 */
public class EmotionFeatureExtractorTest {


    private EmotionFeatureExtractor emotionFeatureExtractor;

    @Before
    public void setUp() throws Exception {
        System.setProperty("org.apache.uima.logger.class",
                "org.apache.uima.util.impl.Log4jLogger_impl");
        emotionFeatureExtractor = new EmotionFeatureExtractor();
    }

    @After
    public void tearDown() throws Exception {
        emotionFeatureExtractor = null;
    }

    @Test
    public void load() {
        try {
            emotionFeatureExtractor.loadLiwcClasses();
            emotionFeatureExtractor.loadLiwcWords();
        } catch (Exception e) {
            e.getMessage();
        }

        List<LiwcClasses> classList = emotionFeatureExtractor.getLiwcClassList();
        System.out.println(classList.size());
        for (LiwcClasses liwcClass : classList) {
            System.out.println("Class ID: " + liwcClass.getId() + " / " + "Class Name: " + liwcClass.getName());
        }

        List<LiwcWords> wordList = emotionFeatureExtractor.getLiwcWordList();

        System.out.print(wordList.isEmpty());
        for (LiwcWords liwcWords : wordList) {
            System.out.print("Word: " + liwcWords.getWord() + " / ");
            for (int id : liwcWords.getIds()) {
                System.out.print("Id: " + id + " ");
            }
            System.out.println();
        }


    }
        @Test
        public void extract() throws Exception{
            try {
                AnalysisEngine engine = createEngine(BreakIteratorSegmenter.class);
                JCas aJCas = engine.newJCas();
                aJCas.setDocumentText("ab abbrichst abbrichst abends aber aber aber");
                engine.process(aJCas);
                List<Feature> features = new ArrayList<Feature>(emotionFeatureExtractor.extract(aJCas));

                for (Feature feature : features) {
                    System.out.println(feature.getName() + " " + feature.getValue());
                }

            } catch (Exception e) {
                e.getMessage();
            }
        }

        //Assert.assertEquals(7, classList.size());

}
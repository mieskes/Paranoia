package de.hda.paranoia.nlp.pipeline.customComponent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.ExternalResourceFactory.createExternalResourceDescription;

/**
 * Created by Andreas on 18.09.2017.
 */
public class TCComponantsTest {

    private TCComponants tCComponants;

    @Before
    public void setUp() throws Exception {
        TCComponants tCComponants= new TCComponants();
    }

    @After
    public void tearDown() throws Exception {
        tCComponants = null;
    }

    @Test
    public void createExtractFeaturesConnector() throws Exception {
//        try{
//
//
//            for (JCas jCas :  new JCasIterable(tCComponants.getExtractFeaturesConnector())){
//
//            }
//
//        }catch(Exception e){
//            System.out.println(e.getMessage());
//        }
    }

    @Test
    public void getExtractFeaturesConnector() throws Exception {
    }

}
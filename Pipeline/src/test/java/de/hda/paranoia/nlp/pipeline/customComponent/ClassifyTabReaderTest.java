package de.hda.paranoia.nlp.pipeline.customComponent;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.jcas.JCas;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import static org.junit.Assert.*;

/**
 * Created by Andreas on 14.09.2017.
 */
public class ClassifyTabReaderTest {

    private CollectionReaderDescription elanTabReader ;



    @Before
    public void setUp() throws Exception {
        System.setProperty("org.apache.uima.logger.class",
                "org.apache.uima.util.impl.Log4jLogger_impl");

         elanTabReader = createReaderDescription(ClassifyTabReader.class,
                ClassifyTabReader.PARAM_ELAN_TAB_FILE, "src/main/resources/merge/Merged-File.txt"

        );

    }

    @After
    public void tearDown() throws Exception {
        CollectionReaderDescription elanTabReader = null;
    }


    @Test
    public void test() throws Exception {

        for (JCas jCas :  new JCasIterable(elanTabReader)){
            DocumentMetaData md = DocumentMetaData.get(jCas);
            dumpMetaData(md);
            //assertNull("Failed creating ClassifyTabReader Object",elanTabReader );
            assertNotNull("CollectionID should not be null", md.getCollectionId());
            //assertNotNull("Base URI should not be null", md.getDocumentBaseUri());
            assertNotNull("URI should not be null", md.getDocumentUri());
            System.out.println(jCas.getDocumentText());
            for (TextClassificationOutcome outcome : JCasUtil.select(jCas,
                    TextClassificationOutcome.class)) {
                //assertTrue("Outcomes should be set", outcome.getOutcome().equals("y"));
                System.out.println("TextClassificationOutcome: " + outcome.getOutcome());
            }
        }
    }


    private void dumpMetaData(final DocumentMetaData aMetaData)
    {
        System.out.println("Collection ID: " + aMetaData.getCollectionId());
        System.out.println("ID           : " + aMetaData.getDocumentId());
        System.out.println("Base URI     : " + aMetaData.getDocumentBaseUri());
        System.out.println("URI          : " + aMetaData.getDocumentUri());
    }



}
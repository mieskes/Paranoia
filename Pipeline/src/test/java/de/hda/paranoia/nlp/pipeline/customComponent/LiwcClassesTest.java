package de.hda.paranoia.nlp.pipeline.customComponent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Andreas on 19.10.2017.
 */
public class LiwcClassesTest {

    private LiwcClasses liwcClasses;

    @Before
    public void setUp() throws Exception {
        System.setProperty("org.apache.uima.logger.class",
                "org.apache.uima.util.impl.Log4jLogger_impl");
        liwcClasses = new LiwcClasses("1", "Pronoun");

    }

    @After
    public void tearDown() throws Exception {
        liwcClasses = null;
    }

    @Test
    public void test(){
        assertEquals(1, liwcClasses.getId());
        assertEquals("Pronoun", liwcClasses.getName());

    }

}
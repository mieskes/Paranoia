package de.hda.paranoia.nlp.pipeline.customComponent;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andreas on 19.10.2017.
 */
public class LiwcWordsTest {

    private LiwcWords liwcWords;
    private List<String> ids = new ArrayList<String>();

    @Before
    public void setUp() throws Exception {
        System.setProperty("org.apache.uima.logger.class",
                "org.apache.uima.util.impl.Log4jLogger_impl");


        ids.add("1");
        ids.add("89");
        ids.add("123");
        liwcWords = new LiwcWords("ab", ids);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test(){
        Assert.assertEquals("ab", liwcWords.getWord());
        Assert.assertEquals(3, liwcWords.getIds().size());

    }

}
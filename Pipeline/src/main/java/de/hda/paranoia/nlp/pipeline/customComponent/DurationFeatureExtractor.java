package de.hda.paranoia.nlp.pipeline.customComponent;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.apache.uima.jcas.JCas;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Andreas on 26.09.2017.
 */
public class DurationFeatureExtractor extends FeatureExtractorResource_ImplBase
        implements DocumentFeatureExtractor {

    /**
     * The String is added to features as description of the value 'segment duration in Seconds' in Weka.
     */
    public static final String SEGMENT_DURATION_IN_SEC = "segmentDurationInSec";


    /**
     *
     * @param aJCas Contains the classified texts from the .txt files
     * @return a list of Features
     * @throws TextClassificationException
     */
    public List<Feature> extract(JCas aJCas)
            throws TextClassificationException {

        List<Feature> featList = new ArrayList<Feature>();
        double segmentDurationInSec = 0;
        Date transformTime;
        long time;
        String [] text = aJCas.getDocumentText().split(" ");

        //Creates a pattern to find the duration of speech and extracts the person talking.
        String regexTime = Pattern.quote("(duration:") + "(\\d\\d:\\d\\d:\\d\\d.\\d\\d\\d)" + Pattern.quote(")");

        Pattern timePattern = Pattern.compile(regexTime);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        //Transforms the extracted time in to milliseconds
        for(String word : text) {

            if(timePattern.matcher(word).matches()) {
                Matcher m = timePattern.matcher(word);
                while (m.find()) {
                    try {
                        transformTime = sdf.parse(m.group(1));
                        time = transformTime.getTime();
                        SimpleDateFormat seconds = new SimpleDateFormat("ss.SSS");
                        segmentDurationInSec = Double.parseDouble(seconds.format(new Date(time)));
                    } catch (Exception e) {
                        e.getMessage();
                    }
                }
            }
        }



        featList.add(new Feature(SEGMENT_DURATION_IN_SEC, segmentDurationInSec));


        return featList;
    }

}

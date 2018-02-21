package TCCustomComponents;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andreas on 04.01.2018.
 */
public class SegmentCountFeatureExtractor extends FeatureExtractorResource_ImplBase
        implements DocumentFeatureExtractor
{

    /**
     * The String is added to features as description of the value 'relative frequency of therapist segments' in Weka.
     */
    public static final String REL_FREQ_SEGMENTS_THERAPIST = "relFreqSegmentsTherapist";

    /**
     * The String is added to features as description of the value 'relative frequency of patient segments' in Weka.
     */
    public static final String REL_FREQ_SEGMENTS_PATIENT = "relFreqSegmentsPatient";

    /**
     *
     * @param aJCas Contains the classified texts from the .txt files
     * @return a list of Features
     * @throws TextClassificationException
     */
    public List<Feature> extract(JCas aJCas)
            throws TextClassificationException
    {
        List<Feature> featList = new ArrayList<Feature>();
        double therapistSegments = 0;
        double patientSegment = 0;

        String[] tabs;
        String[] lines = aJCas.getDocumentText().split("\n");

        for (String line : lines)
        {

            tabs = line.split("\t");
            if (tabs[1].matches("Therapeut"))
            {
                therapistSegments++;

            }

            if (tabs[1].matches("Patient"))
            {
                patientSegment++;

            }
        }

        therapistSegments /= lines.length;
        patientSegment /= lines.length;

        System.out.println("T: " + therapistSegments + " Pa:" + patientSegment + " Ti: " + lines.length);

        featList.add(new Feature(REL_FREQ_SEGMENTS_THERAPIST, therapistSegments));
        featList.add(new Feature(REL_FREQ_SEGMENTS_PATIENT, patientSegment));

        return featList;

    }

}

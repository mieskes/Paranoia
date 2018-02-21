package TCCustomComponents;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.DocumentFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import org.apache.uima.jcas.JCas;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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
     * The String is added to features as description of the value 'relative frequency of speech time Milliseconds
     * by the therapist' in Weka.
     */
    public static final String REL_FREQ_SECONDS_THERAPIST = "relFreqMSecondsTherapist";

    /**
     * The String is added to features as description of the value 'relative frequency of speech time Milliseconds
     * by the patient' in Weka.
     */
    public static final String REL_FREQ_SECONDS_PATIENT = "relFreqMSecondsPatient";


    /**
     *
     * @param aJCas Contains the classified texts from the .txt files
     * @return a list of Features
     * @throws TextClassificationException
     */
    public List<Feature> extract(JCas aJCas)
            throws TextClassificationException {

        List<Feature> featList = new ArrayList<Feature>();
        String[] tabs;
        double patientTime = 0;
        double therapistTime = 0;
        double segmentDurationInSec = 0;
        Date transformTime;
        long time = 0;
        String[] lines = aJCas.getDocumentText().split("\n");

        //Creates a pattern to find the duration of speech and extracts the person talking.
        String regexTime = Pattern.quote("(duration:") + "(\\d\\d:\\d\\d:\\d\\d.\\d\\d\\d)" + Pattern.quote(")");

        Pattern timePattern = Pattern.compile(regexTime);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("CET"));

        //Transforms the extracted time in to milliseconds
        for(String line : lines) {
            tabs = line.split("\t");

            if(timePattern.matcher(tabs[0]).matches()) {
                Matcher m = timePattern.matcher(tabs[0]);
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


            //Adds time to therapist;
            if(tabs[1].matches("Therapeut")){
                therapistTime += (Double.parseDouble(tabs[2]));
            }

            //Adds time to patient
            if(tabs[1].matches("Patient")){
                patientTime += Double.parseDouble(tabs[2]);
            }
        }


        if(therapistTime != 0 && patientTime != 0 && time != 0) {
            therapistTime /= (double) time;
            patientTime /= (double) time;
        }


        //Refactors the time into seconds.milliseconds
        DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance();
        DecimalFormat df = new DecimalFormat("####.##");
        sym.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(sym);

        therapistTime = Double.parseDouble(df.format(therapistTime));
        patientTime = Double.parseDouble(df.format(patientTime));




        featList.add(new Feature(SEGMENT_DURATION_IN_SEC, segmentDurationInSec));
        featList.add(new Feature(REL_FREQ_SECONDS_THERAPIST, therapistTime));
        featList.add(new Feature(REL_FREQ_SECONDS_PATIENT, patientTime));


        return featList;
    }

}

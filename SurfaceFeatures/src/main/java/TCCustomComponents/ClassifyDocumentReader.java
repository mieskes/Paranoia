package TCCustomComponents;

import com.google.gson.annotations.Since;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.tc.api.io.TCReaderSingleLabel;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome;

import de.tudarmstadt.ukp.dkpro.tc.core.io.SingleLabelReaderBase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.uima.UimaContext;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Andreas on 18.12.2017.
 */
public class ClassifyDocumentReader extends JCasCollectionReader_ImplBase implements TCReaderSingleLabel {

    /**
     *
     */
    public static final String PARAM_ELAN_TAB_FILE = "elanTabFile";

    /**
     *
     */
    @ConfigurationParameter(name = PARAM_ELAN_TAB_FILE, mandatory = true,
            description = "Description", defaultValue = "true")
    private String elanTabFile;

    /**
     *
     */
    String date;

    /**
     *
     */
    List<List<TextLine>> docContainer = new ArrayList<List<TextLine>>();

    /**
     *
     */
    private List<TextLine> result = new ArrayList<TextLine>();

    /**
     *
     */
    private List<String> docText = new ArrayList<String>();

    /**
     *
     */
    private double segmentDuration;

    /**
     *
     */
    private int offset;

    /**
     *
     */
    private URL resourceUrl;

    /**
     *
     */
    private List<File> files;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException {

        super.initialize(context);
        String[] tabArray = null;
        try {

            resourceUrl = ResourceUtils.resolveLocation(elanTabFile, this, context);

            File dir = new File(elanTabFile);
            System.out.println("Getting all files in " + dir.getCanonicalPath() + " including those in subdirectories");
            files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

            for (File file : files) {
                StringBuilder stringBuilder = new StringBuilder();
                URL resourceUrl = ResourceUtils.resolveLocation(file.toString());
                System.out.println(resourceUrl);
                InputStream is = resourceUrl.openStream();
                for (String lines : IOUtils.readLines(is, "utf-8")) {
                    tabArray = lines.split("\t");
                    if (tabArray.length == 9) {
                        result.add(new TextLine(tabArray[0], Double.parseDouble(tabArray[3]), Double.parseDouble(tabArray[7]), tabArray[8]));
                    }
                }

                docContainer.add(new ArrayList<>(result));
                result.clear();
            }

            Collections.sort(result, new Comparator<TextLine>() {

                public int compare(TextLine array1, TextLine array2) {

                    return Double.compare(array1.getBegin(), array2.getBegin());
                }
            });

            offset = 0;
        } catch (Exception e) {
            throw new ResourceInitializationException(e);

        }
    }

    @Override
    public boolean hasNext()
            throws IOException, CollectionException {

        return offset < files.size();
    }

    @Override
    public void getNext(JCas aJCas)
            throws IOException, CollectionException {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            URI resourceURI = resourceUrl.toURI();
            result = docContainer.get(offset);
            for (TextLine line : result) {
                segmentDuration += line.getDuration();
                stringBuilder.append("\t" + line.getSpeaker() + "\t" + line.getDuration() + "\t" + line.getText() + "\n");
            }

            String text = stringBuilder.toString();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
            sdf.setTimeZone(TimeZone.getTimeZone("CET"));
            date = sdf.format(new Date((long) segmentDuration));
            //System.out.println(date);

            //
            if (text.isEmpty()) {
                System.out.println("Empty");
            }
            aJCas.setDocumentText("(duration:" + date + ")" + text);

            DocumentMetaData dmd = DocumentMetaData.create(aJCas);
            dmd.setCollectionId("Rating " + new File(resourceURI.getPath()).getParentFile().getName());

            dmd.setDocumentTitle("ELANTranscribtion " + new File(resourceURI.getPath()).getName());
            dmd.setDocumentBaseUri("ELANTranscribtion " + dmd.getDocumentBaseUri());
            dmd.setDocumentUri(dmd.getDocumentBaseUri().toString() + new File(resourceURI.getPath()).getName());
            dmd.setDocumentId(String.valueOf(offset));

            TextClassificationOutcome outcome = new TextClassificationOutcome(aJCas);
            outcome.setOutcome(getTextClassificationOutcome(aJCas));
            outcome.addToIndexes();
        } catch (Exception e) {
            e.getMessage();
        }

        segmentDuration = 0;
        offset++;
    }

    @Override
    public String getTextClassificationOutcome(JCas jcas)
            throws CollectionException {

        return files.get(offset).getParentFile().getName();
    }

    public Progress[] getProgress() {
        return new Progress[]
                {
                        new ProgressImpl(offset, files.size(), "Document")
                };
    }

}

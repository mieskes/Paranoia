package TCCustomComponents;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andreas on 19.10.2017.
 */
public class LiwcWords {

    /**
     * Contains the LIWC-Words
     */
    private String word;

    /**
     * Contains the ids of the LIWC-Words
     */
    private List<Integer> ids = new ArrayList<Integer>();

    /**
     *
     * @param word Sets the LIWC-Words
     * @param ids Sets the LIWC-Words
     */
    public LiwcWords(String word, List<String> ids){
        this.word = word;

        for(String id : ids){
            this.ids.add(Integer.parseInt(id));
        }


    }

    /**
     *
     * @return the LIWC-Words
     */
    public String getWord() {
        return word;
    }

    /**
     *
     * @return LIWC-ids
     */
    public List<Integer> getIds() {
        return ids;
    }


}

package TCCustomComponents;

/**
 * Created by Andreas on 19.10.2017.
 */
public class LiwcClasses {


    /**
     * Contains the Name of the LIWC-Class
     */
    private String name;

    /**
     * Contains the id of the LIWC-Class
     */
    private int id;

    /**
     *
     * @param id Sets the id of the LIWC-Class
     * @param name Sets the Name of the LIWC-Class
     */
    public LiwcClasses(String id, String name){
        this.name = name;
        this.id = Integer.parseInt(id);

    }

    /**
     *
     * @return the name of the LIWC-Class
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return the id of the LIWC-Class
     */
    public int getId() {
        return id;
    }



}

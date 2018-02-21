package TCCustomComponents;

/**
 * Created by Andreas on 12.09.2017.
 */
public class TextLine
{

    /**
     *
     */
    private String speaker;

    /**
     *
     */
    private double begin;

    /**
     *
     */
    private String text;

    /**
     *
     */
    private double duration;

    /**
     *
     * @param speaker Sets the speaker.
     * @param begin Sets the beginn of speech.
     * @param duration Sets the speech duration.
     * @param text Sets the text of the speaker.
     */
    public TextLine(String speaker, double begin, double duration, String text)
    {
        this.speaker = speaker;
        this.begin = begin;
        this.duration = duration;
        this.text = text;
    }

    /**
     *
     * @return
     */
    public String getSpeaker()
    {
        return speaker;
    }

    /**
     *
     * @param speaker
     */
    public void setSpeaker(String speaker)
    {
        this.speaker = speaker;
    }

    /**
     *
     * @return
     */
    public double getBegin()
    {
        return begin;
    }

    /**
     *
     * @param begin
     */
    public void setBegin(double begin)
    {
        this.begin = begin;
    }

    /**
     *
     * @return
     */
    public String getText()
    {
        return text;
    }

    /**
     *
     * @return
     */
    public double getDuration()
    {
        return this.duration;
    }

    /**
     *
     * @param text
     */
    public void setText(String text)
    {
        this.text = text;
    }

}

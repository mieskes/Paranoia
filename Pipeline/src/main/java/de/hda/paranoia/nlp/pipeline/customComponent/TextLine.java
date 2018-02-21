package de.hda.paranoia.nlp.pipeline.customComponent;

/**
 * Created by Andreas on 12.09.2017.
 */
public class TextLine {

    /**
     * Contains Speaker
     */
    private String speaker;

    /**
     * Contains begin of speech
     */
    private double begin;

    /**
     * Contains spoken text
     */
    private String text;

    /**
     * Contains duration of speech
     */
    private String duration;
    /**
     *
     * @param speaker Sets the speaker.
     * @param begin Sets the beginn of speech.
     * @param duration Sets the speech duration.
     * @param text Sets the text of the speaker.
     */
    public TextLine(String speaker, double begin, String duration, String text){
        this.speaker = speaker;
        this.begin = begin;
        this.duration = duration;
        this.text = text;
    }

    public String getSpeaker() {
        return speaker;
    }

    public void setSpeaker(String speaker) {
        this.speaker = speaker;
    }

    public double getBegin() {
        return begin;
    }

    public void setBegin(double begin) {
        this.begin = begin;
    }

    public String getText() {
        return text;
    }

    public String getDuration(){
        return this.duration;
    }

    public void setText(String text) {
        this.text = text;
    }


}

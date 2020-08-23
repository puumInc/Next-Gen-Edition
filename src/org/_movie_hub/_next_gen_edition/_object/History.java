package org._movie_hub._next_gen_edition._object;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.List;

/**
 * @author Mandela aka puumInc
 */
public class History implements Serializable {

    public static final long serialVersionUID = 2L;

    private String date;
    private String jobName;
    private String timeWhenItStarted;
    private String timeWhenItStopped;
    private String status;
    private List<String> listOfMedia;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getTimeWhenItStarted() {
        return timeWhenItStarted;
    }

    public void setTimeWhenItStarted(String timeWhenItStarted) {
        this.timeWhenItStarted = timeWhenItStarted;
    }

    public String getTimeWhenItStopped() {
        return timeWhenItStopped;
    }

    public void setTimeWhenItStopped(String timeWhenItStopped) {
        this.timeWhenItStopped = timeWhenItStopped;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getListOfMedia() {
        return listOfMedia;
    }

    public void setListOfMedia(List<String> listOfMedia) {
        this.listOfMedia = listOfMedia;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this, History.class);
    }
}

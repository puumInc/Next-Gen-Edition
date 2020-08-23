package org._movie_hub._next_gen_edition._object;

import javafx.collections.ObservableList;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * @author Mandela aka puumInc
 */
public class Job implements Serializable {

    public static final long serialVersionUID = 1L;

    private String jobName;
    private File destinationFolder;
    private List<String> allMediaPaths;
    private double cost;
    private double sourceSize;
    private boolean forUpload;
    private double byteSent;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public File getDestinationFolder() {
        return destinationFolder;
    }

    public void setDestinationFolder(File destinationFolder) {
        this.destinationFolder = destinationFolder;
    }

    public List<String> getAllMediaPaths() {
        return allMediaPaths;
    }

    public void setAllMediaPaths(ObservableList<String> allMediaPaths) {
        this.allMediaPaths = allMediaPaths;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getSourceSize() {
        return sourceSize;
    }

    public void setSourceSize(double sourceSize) {
        this.sourceSize = sourceSize;
    }

    public boolean isForUpload() {
        return forUpload;
    }

    public void setForUpload(boolean forUpload) {
        this.forUpload = forUpload;
    }

    public double getByteSent() {
        return byteSent;
    }

    public void setByteSent(double byteSent) {
        this.byteSent = byteSent;
    }

}

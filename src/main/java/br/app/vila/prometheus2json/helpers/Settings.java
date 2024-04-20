package br.app.vila.prometheus2json.helpers;

import java.util.List;

/**
 * Definition of the settings.
 *
 * @author fvilarin
 */
public class Settings {
    private List<Job> jobs = null;

    public List<Job> getJobs() {
        return this.jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }
}

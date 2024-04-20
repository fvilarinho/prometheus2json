package br.app.vila.prometheus2json.helpers;

import br.app.vila.prometheus2json.constants.Constants;

/**
 * Definition of a job that will fetch prometheus metrics and push it to a destination (file or HTTP/HTTPs endpoint) in
 * a specific polling interval (seconds).
 *
 * @author fvilarin
 */
public class Job {
    private String id;
    private Url source;
    private Url destination;
    private int interval = Constants.DEFAULT_POLLING_INTERVAL;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getInterval() {
        return this.interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public Url getSource() {
        return this.source;
    }

    public void setSource(Url source) {
        this.source = source;
    }

    public Url getDestination() {
        return this.destination;
    }

    public void setDestination(Url destination) {
        this.destination = destination;
    }
}

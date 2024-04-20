package br.app.vila.prometheus2json.helpers;

/**
 * Definition of a prometheus metric.
 *
 * @author fvilarin
 */
public class Metric {
    private long timestamp;
    private String name;
    private String labels;
    private double value;

    public Metric(long timestamp, String name, String labels, double value) {
        super();

        setTimestamp(timestamp);
        setName(name);
        setLabels(labels);
        setValue(value);
    }

    public String getLabels() {
        return this.labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getValue() {
        return this.value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}

package br.app.vila.prometheus2json.helpers;

/**
 * Definition of a job header.
 *
 * @author fvilarin
 */
public class Header {
    private String name;
    private String value;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

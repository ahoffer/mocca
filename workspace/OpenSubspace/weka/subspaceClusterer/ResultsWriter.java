package weka.subspaceClusterer;

import i9.subspace.base.Cluster;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

public class ResultsWriter {
    static String clustererExpKey = "EXP";
    static String clustererNameKey = "ALGO";
    static String dataNameKey = "DATA";
    static String extension = ".csv";

    String path;

    /* The name of a measure or parameter (key) and either the measurement or the value of the parameter */
    TreeMap<String, String> output = new TreeMap<String, String>();

    public void put(String name, Double value) {
        output.put(name, String.format("%f", value));
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void writeResults() throws Exception {

        CsvListWriter writer = getListWriter("RSLT");
        String[] header = output.navigableKeySet().toArray(new String[0]);
        writer.writeHeader(header);
        writer.write(output.values().toArray(new String[0]));
        writer.close();
    }

    public void writeClusters(ArrayList<Cluster> clusters) {
        // CsvListWriter writer = getListWriter("CLSTR");
        for (Cluster each : clusters) {
            System.out.println(each.toString());
        }
    }

    File getFile(String name) {
        return new File(getPath() + getKey() + "_" + name + extension);
    }

    CsvListWriter getListWriter(String name) {

        File file = getFile(name);
        CsvListWriter temp = null;
        try {
            temp = new CsvListWriter(new FileWriter(file.getCanonicalFile()),
                    new CsvPreference.Builder('"', ',', "\n").build());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp;
    }

    public String getKey() {
        return output.get(clustererExpKey);
    }

    public String getPath() {
        return path;
    }

    public void setKey(String key) {
        output.put(clustererExpKey, key);
    }

    public void setClustererName(String name) {
        output.put(clustererNameKey, name);
    }

    public void setDataName(String name) {
        output.put(dataNameKey, name);
    }
}// class

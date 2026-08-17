package com.example.sample.worker;

public class DataDumpRequestPayload {
    private String exportName;

    public DataDumpRequestPayload() {}

    public DataDumpRequestPayload(String exportName) {
        this.exportName = exportName;
    }

    public String getExportName() { return exportName; }
    public void setExportName(String exportName) { this.exportName = exportName; }
}

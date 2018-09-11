package models;

public enum ReasonToChangeBen {


     ERROR_IN_DATA_COLLECTION,A,B,C,D;

    public String getName() {
        String tmp = name();
        return tmp.charAt(0) + tmp.toLowerCase().substring(1).replaceAll("_", " ");
    }
}
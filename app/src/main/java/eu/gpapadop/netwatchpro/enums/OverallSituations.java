package eu.gpapadop.netwatchpro.enums;

public enum OverallSituations {
    Excellent(1, "excellent"),
    Good(2, "good"),
    Average(3, "average"),
    Warning(4, "warning"),
    Danger(5, "danger");

    private int situationRisk;
    private String stringValue;

    OverallSituations(int newSituationRisk, String newStringValue){
        this.situationRisk = newSituationRisk;
        this.stringValue = newStringValue;
    }

    public int getSituationRisk(){
        return this.situationRisk;
    }

    public String getStringValue(){
        return this.stringValue;
    }
}

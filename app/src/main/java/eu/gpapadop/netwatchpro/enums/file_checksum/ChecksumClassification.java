package eu.gpapadop.netwatchpro.enums.file_checksum;

public enum ChecksumClassification {
    MobWin("Riskware.Android.MobWin"),
    Agent("Trojan.Android.Agent"),
    Airpush("Trojan.Android.Airpush"),
    Domob("Trojan.Android.Domob"),
    Dowgin("Trojan.Android.Dowgin"),
    FakeInst("Trojan.Android.FakeInst"),
    Opfake("Trojan.Android.Opfake"),
    WqMobile("Trojan.Android.WqMobile");

    private String categoryName;
    ChecksumClassification(String newCategoryName){
        this.categoryName = newCategoryName;
    }

    public String getCategoryName(){
        return this.categoryName;
    }
}

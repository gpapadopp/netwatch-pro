package eu.gpapadop.netwatchpro.enums.permissions_danger;

public enum MostDangerousPermissions {
    BIND_DEVICE_ADMIN("BIND_DEVICE_ADMIN", 5),
    CALL_PHONE("CALL_PHONE", 5),
    CAMERA("CAMERA", 5),
    CAPTURE_AUDIO_OUTPUT("CAPTURE_AUDIO_OUTPUT", 5),
    DELETE_PACKAGES("DELETE_PACKAGES", 5),
    INSTALL_PACKAGES("INSTALL_PACKAGES", 5),
    MODIFY_PHONE_STATE("MODIFY_PHONE_STATE", 5),
    NFC("NFC", 5),
    RECORD_AUDIO("RECORD_AUDIO", 5),
    REQUEST_INSTALL_PACKAGES("REQUEST_INSTALL_PACKAGES", 5),
    SEND_SMS("SEND_SMS", 5),
    SET_ALWAYS_FINISH("SET_ALWAYS_FINISH", 5),
    SET_ANIMATION_SCALE("SET_ANIMATION_SCALE", 5),
    SET_DEBUG_APP("SET_DEBUG_APP", 5),
    SET_PROCESS_LIMIT("SET_PROCESS_LIMIT", 5),
    SET_TIME("SET_TIME", 5),
    SET_TIME_ZONE("SET_TIME_ZONE", 5),
    SMS_FINANCIAL_TRANSACTIONS("SMS_FINANCIAL_TRANSACTIONS", 5),
    SYSTEM_ALERT_WINDOW("SYSTEM_ALERT_WINDOW", 5),
    WRITE_APN_SETTINGS("WRITE_APN_SETTINGS", 5),
    WRITE_CALL_LOG("WRITE_CALL_LOG", 5),
    WRITE_CONTACTS("WRITE_CONTACTS", 5),
    WRITE_EXTERNAL_STORAGE("WRITE_EXTERNAL_STORAGE", 5),
    WRITE_GSERVICES("WRITE_GSERVICES", 5),
    WRITE_SECURE_SETTINGS("WRITE_SECURE_SETTINGS", 5),
    WRITE_SETTINGS("WRITE_SETTINGS", 5),
    WRITE_VOICEMAIL("WRITE_VOICEMAIL", 5);

    private String permissionName;
    private int permissionPoints;

    MostDangerousPermissions(String newPermissionName, int newPermissionPoints){
        this.permissionName = "android.permission." + newPermissionName;
        this.permissionPoints = newPermissionPoints;
    }

    public String getPermissionName(){
        return this.permissionName;
    }

    public int getPermissionPoints(){
        return this.permissionPoints;
    }
}

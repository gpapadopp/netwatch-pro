package eu.gpapadop.netwatchpro.enums.permissions_danger;

public enum MostDangerousPermissions {
    BIND_DEVICE_ADMIN("BIND_DEVICE_ADMIN"),
    CALL_PHONE("CALL_PHONE"),
    CAMERA("CAMERA"),
    CAPTURE_AUDIO_OUTPUT("CAPTURE_AUDIO_OUTPUT"),
    DELETE_PACKAGES("DELETE_PACKAGES"),
    INSTALL_PACKAGES("INSTALL_PACKAGES"),
    MODIFY_PHONE_STATE("MODIFY_PHONE_STATE"),
    NFC("NFC"),
    RECORD_AUDIO("RECORD_AUDIO"),
    REQUEST_INSTALL_PACKAGES("REQUEST_INSTALL_PACKAGES"),
    SEND_SMS("SEND_SMS"),
    SET_ALWAYS_FINISH("SET_ALWAYS_FINISH"),
    SET_ANIMATION_SCALE("SET_ANIMATION_SCALE"),
    SET_DEBUG_APP("SET_DEBUG_APP"),
    SET_PROCESS_LIMIT("SET_PROCESS_LIMIT"),
    SET_TIME("SET_TIME"),
    SET_TIME_ZONE("SET_TIME_ZONE"),
    SMS_FINANCIAL_TRANSACTIONS("SMS_FINANCIAL_TRANSACTIONS"),
    SYSTEM_ALERT_WINDOW("SYSTEM_ALERT_WINDOW"),
    WRITE_APN_SETTINGS("WRITE_APN_SETTINGS"),
    WRITE_CALL_LOG("WRITE_CALL_LOG"),
    WRITE_CONTACTS("WRITE_CONTACTS"),
    WRITE_EXTERNAL_STORAGE("WRITE_EXTERNAL_STORAGE"),
    WRITE_GSERVICES("WRITE_GSERVICES"),
    WRITE_SECURE_SETTINGS("WRITE_SECURE_SETTINGS"),
    WRITE_SETTINGS("WRITE_SETTINGS"),
    WRITE_VOICEMAIL("WRITE_VOICEMAIL");

    private String permissionName;
    MostDangerousPermissions(String newPermissionName){
        this.permissionName = "android.permission." + newPermissionName;
    }

    public String getPermissionName(){
        return this.permissionName;
    }
}

package eu.gpapadop.netwatchpro.enums.permissions_danger;

public enum HighRiskPermissions {
    ACCESS_BACKGROUND_LOCATION("ACCESS_BACKGROUND_LOCATION"),
    ACCESS_COARSE_LOCATION("ACCESS_COARSE_LOCATION"),
    ACCESS_FINE_LOCATION("ACCESS_FINE_LOCATION"),
    ACTIVITY_RECOGNITION("ACTIVITY_RECOGNITION"),
    ANSWER_PHONE_CALLS("ANSWER_PHONE_CALLS"),
    BIND_ACCESSIBILITY_SERVICE("BIND_ACCESSIBILITY_SERVICE"),
    BIND_APPWIDGET("BIND_APPWIDGET"),
    BIND_AUTOFILL_SERVICE("BIND_AUTOFILL_SERVICE"),
    BIND_CARRIER_MESSAGING_CLIENT_SERVICE("BIND_CARRIER_MESSAGING_CLIENT_SERVICE"),
    BIND_CARRIER_MESSAGING_SERVICE("BIND_CARRIER_MESSAGING_SERVICE"),
    BIND_CARRIER_SERVICES("BIND_CARRIER_SERVICES"),
    BIND_CHOOSER_TARGET_SERVICE("BIND_CHOOSER_TARGET_SERVICE"),
    BIND_CONTROLS("BIND_CONTROLS"),
    BIND_DREAM_SERVICE("BIND_DREAM_SERVICE"),
    BIND_INCALL_SERVICE("BIND_INCALL_SERVICE"),
    BIND_INPUT_METHOD("BIND_INPUT_METHOD"),
    BIND_MIDI_DEVICE_SERVICE("BIND_MIDI_DEVICE_SERVICE"),
    BIND_NFC_SERVICE("BIND_NFC_SERVICE"),
    BIND_NOTIFICATION_LISTENER_SERVICE("BIND_NOTIFICATION_LISTENER_SERVICE"),
    BIND_PRINT_SERVICE("BIND_PRINT_SERVICE"),
    BIND_QUICK_ACCESS_WALLET_SERVICE("BIND_QUICK_ACCESS_WALLET_SERVICE"),
    BIND_QUICK_SETTINGS_TILE("BIND_QUICK_SETTINGS_TILE"),
    BIND_REMOTEVIEWS("BIND_REMOTEVIEWS"),
    BIND_SCREENING_SERVICE("BIND_SCREENING_SERVICE"),
    BIND_TELECOM_CONNECTION_SERVICE("BIND_TELECOM_CONNECTION_SERVICE"),
    BIND_TEXT_SERVICE("BIND_TEXT_SERVICE"),
    BIND_TV_INPUT("BIND_TV_INPUT"),
    BIND_VISUAL_VOICEMAIL_SERVICE("BIND_VISUAL_VOICEMAIL_SERVICE"),
    BIND_VOICE_INTERACTION("BIND_VOICE_INTERACTION"),
    BIND_VPN_SERVICE("BIND_VPN_SERVICE"),
    BIND_VR_LISTENER_SERVICE("BIND_VR_LISTENER_SERVICE"),
    BIND_WALLPAPER("BIND_WALLPAPER"),
    INTERNET("INTERNET"),
    NFC_PREFERRED_PAYMENT_INFO("NFC_PREFERRED_PAYMENT_INFO"),
    NFC_TRANSACTION_EVENT("NFC_TRANSACTION_EVENT"),
    PROCESS_OUTGOING_CALLS("PROCESS_OUTGOING_CALLS"),
    RECEIVE_MMS("RECEIVE_MMS"),
    RECEIVE_SMS("RECEIVE_SMS"),
    RECEIVE_WAP_PUSH("RECEIVE_WAP_PUSH"),
    SEND_RESPOND_VIA_MESSAGE("SEND_RESPOND_VIA_MESSAGE"),
    USE_BIOMETRIC("USE_BIOMETRIC"),
    USE_FINGERPRINT("USE_FINGERPRINT"),
    USE_FULL_SCREEN_INTENT("USE_FULL_SCREEN_INTENT"),
    USE_SIP("USE_SIP"),
    WRITE_CALENDAR("WRITE_CALENDAR"),
    WRITE_SYNC_SETTINGS("WRITE_SYNC_SETTINGS");

    private String permissionName;
    HighRiskPermissions(String newPermissionName){
        this.permissionName = "android.permission." + newPermissionName;
    }

    public String getPermissionName(){
        return this.permissionName;
    }
}

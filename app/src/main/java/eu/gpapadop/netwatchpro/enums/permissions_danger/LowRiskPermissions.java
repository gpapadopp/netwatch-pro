package eu.gpapadop.netwatchpro.enums.permissions_danger;

public enum LowRiskPermissions {
    ACCEPT_HANDOVER("ACCEPT_HANDOVER"),
    BIND_CONDITION_PROVIDER_SERVICE("BIND_CONDITION_PROVIDER_SERVICE"),
    REQUEST_COMPANION_RUN_IN_BACKGROUND("REQUEST_COMPANION_RUN_IN_BACKGROUND"),
    REQUEST_COMPANION_USE_DATA_IN_BACKGROUND("REQUEST_COMPANION_USE_DATA_IN_BACKGROUND"),
    REQUEST_DELETE_PACKAGES("REQUEST_DELETE_PACKAGES"),
    REQUEST_IGNORE_BATTERY_OPTIMIZATIONS("REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"),
    REQUEST_PASSWORD_COMPLEXITY("REQUEST_PASSWORD_COMPLEXITY"),
    VIBRATE("VIBRATE");

    private String permissionName;
    LowRiskPermissions(String newPermissionName){
        this.permissionName = "android.permission." + newPermissionName;
    }

    public String getPermissionName(){
        return this.permissionName;
    }
}

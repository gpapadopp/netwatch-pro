package eu.gpapadop.netwatchpro.enums.permissions_danger;

public enum LowRiskPermissions {
    ACCEPT_HANDOVER("ACCEPT_HANDOVER", 2),
    BIND_CONDITION_PROVIDER_SERVICE("BIND_CONDITION_PROVIDER_SERVICE", 2),
    REQUEST_COMPANION_RUN_IN_BACKGROUND("REQUEST_COMPANION_RUN_IN_BACKGROUND", 2),
    REQUEST_COMPANION_USE_DATA_IN_BACKGROUND("REQUEST_COMPANION_USE_DATA_IN_BACKGROUND", 2),
    REQUEST_DELETE_PACKAGES("REQUEST_DELETE_PACKAGES", 2),
    REQUEST_IGNORE_BATTERY_OPTIMIZATIONS("REQUEST_IGNORE_BATTERY_OPTIMIZATIONS", 2),
    REQUEST_PASSWORD_COMPLEXITY("REQUEST_PASSWORD_COMPLEXITY", 2),
    VIBRATE("VIBRATE", 2);

    private String permissionName;
    private int permissionPoints;

    LowRiskPermissions(String newPermissionName, int newPermissionPoints){
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

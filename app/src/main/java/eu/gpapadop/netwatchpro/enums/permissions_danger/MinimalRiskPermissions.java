package eu.gpapadop.netwatchpro.enums.permissions_danger;

public enum MinimalRiskPermissions {
    REORDER_TASKS("REORDER_TASKS");

    private String permissionName;
    MinimalRiskPermissions(String newPermissionName){
        this.permissionName = "android.permission." + newPermissionName;
    }

    public String getPermissionName(){
        return this.permissionName;
    }
}

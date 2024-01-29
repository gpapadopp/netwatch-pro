package eu.gpapadop.netwatchpro.enums.permissions_danger;

public enum MinimalRiskPermissions {
    REORDER_TASKS("REORDER_TASKS", 1);

    private String permissionName;
    private int permissionPoints;

    MinimalRiskPermissions(String newPermissionName, int newPermissionPoints){
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

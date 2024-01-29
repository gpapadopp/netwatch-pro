package eu.gpapadop.netwatchpro.utils;

import java.util.List;

import eu.gpapadop.netwatchpro.classes.last_scans.App;
import eu.gpapadop.netwatchpro.enums.OverallSituations;

public class OverallSituationUtils {
    private PermissionsDangerEnumUtils permissionsDangerEnumUtils;

    public OverallSituationUtils(){
        this.permissionsDangerEnumUtils = new PermissionsDangerEnumUtils();
    }

    public OverallSituations calculateOverallSituation(List<App> allScanApps){
        int totalPermissionCounter = 0;
        int totalMinimalPointsSum = 0;
        int totalLowPointsSum = 0;
        int totalModeratePointsSum = 0;
        int totalHighPointsSum = 0;
        int totalMostDangerousPointsSum = 0;
        for (App singleApp : allScanApps){
            totalPermissionCounter += singleApp.getAllPermissions().size();
            totalMinimalPointsSum += this.getMinimalSum(singleApp);
            totalLowPointsSum += this.getLowSum(singleApp) * 2;
            totalModeratePointsSum += this.getModerateSum(singleApp) * 3;
            totalHighPointsSum += this.getHighSum(singleApp) * 4;
            totalMostDangerousPointsSum += this.getMostDangerSum(singleApp) * 5;
        }
        double ceilDangerousNumber = Math.ceil((double) (totalMinimalPointsSum + totalLowPointsSum + totalModeratePointsSum + totalHighPointsSum + totalMostDangerousPointsSum) / totalPermissionCounter);

        if (ceilDangerousNumber == 1.0){
            return OverallSituations.Excellent;
        }
        if (ceilDangerousNumber == 2.0){
            return OverallSituations.Good;
        }
        if (ceilDangerousNumber == 3.0){
            return OverallSituations.Average;
        }
        if (ceilDangerousNumber == 4.0){
            return OverallSituations.Warning;
        }
        if (ceilDangerousNumber == 5.0){
            return OverallSituations.Danger;
        }
        return OverallSituations.Excellent;
    }

    private int getMinimalSum(App singleApp){
        return this.permissionsDangerEnumUtils.getMinimalRiskPermissions(singleApp.getAllPermissions()).size();
    }

    private int getLowSum(App singleApp){
        return this.permissionsDangerEnumUtils.getLowRiskPermissions(singleApp.getAllPermissions()).size();
    }

    private int getModerateSum(App singleApp){
        return this.permissionsDangerEnumUtils.getModerateRiskPermissions(singleApp.getAllPermissions()).size();
    }

    private int getHighSum(App singleApp){
        return this.permissionsDangerEnumUtils.getHighRiskPermissions(singleApp.getAllPermissions()).size();
    }

    private int getMostDangerSum(App singleApp){
        return this.permissionsDangerEnumUtils.getMostDangerousPermissions(singleApp.getAllPermissions()).size();
    }
}

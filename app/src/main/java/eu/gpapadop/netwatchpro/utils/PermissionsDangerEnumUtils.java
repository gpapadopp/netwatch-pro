package eu.gpapadop.netwatchpro.utils;

import java.util.ArrayList;
import java.util.List;

import eu.gpapadop.netwatchpro.enums.permissions_danger.HighRiskPermissions;
import eu.gpapadop.netwatchpro.enums.permissions_danger.LowRiskPermissions;
import eu.gpapadop.netwatchpro.enums.permissions_danger.MinimalRiskPermissions;
import eu.gpapadop.netwatchpro.enums.permissions_danger.ModerateRiskPermissions;
import eu.gpapadop.netwatchpro.enums.permissions_danger.MostDangerousPermissions;

public class PermissionsDangerEnumUtils {
    public PermissionsDangerEnumUtils(){}

    public List<String> getMinimalRiskPermissions(List<String> allPermissions){
        List<String> minimalPermissions = new ArrayList<>();
        for (MinimalRiskPermissions minimalRiskPermissions : MinimalRiskPermissions.values()){
            if (allPermissions.contains(minimalRiskPermissions.getPermissionName())){
                minimalPermissions.add(minimalRiskPermissions.getPermissionName());
            }
        }
        return minimalPermissions;
    }

    public List<String> getLowRiskPermissions(List<String> allPermissions){
        List<String> lowPermissions = new ArrayList<>();
        for (LowRiskPermissions lowRiskPermissions : LowRiskPermissions.values()){
            if (allPermissions.contains(lowRiskPermissions.getPermissionName())){
                lowPermissions.add(lowRiskPermissions.getPermissionName());
            }
        }
        return lowPermissions;
    }

    public List<String> getModerateRiskPermissions(List<String> allPermissions){
        List<String> moderatePermissions = new ArrayList<>();
        for (ModerateRiskPermissions moderateRiskPermissions : ModerateRiskPermissions.values()){
            if (allPermissions.contains(moderateRiskPermissions.getPermissionName())){
                moderatePermissions.add(moderateRiskPermissions.getPermissionName());
            }
        }
        return moderatePermissions;
    }

    public List<String> getHighRiskPermissions(List<String> allPermissions){
        List<String> highPermissions = new ArrayList<>();
        for (HighRiskPermissions highRiskPermissions : HighRiskPermissions.values()){
            if (allPermissions.contains(highRiskPermissions.getPermissionName())){
                highPermissions.add(highRiskPermissions.getPermissionName());
            }
        }
        return highPermissions;
    }

    public List<String> getMostDangerousPermissions(List<String> allPermissions){
        List<String> mostDangerousPermissions = new ArrayList<>();
        for (MostDangerousPermissions mostDangerousPermission : MostDangerousPermissions.values()){
            if (allPermissions.contains(mostDangerousPermission.getPermissionName())){
                mostDangerousPermissions.add(mostDangerousPermission.getPermissionName());
            }
        }
        return mostDangerousPermissions;
    }
}

package vipers;

import battlecode.common.*;

public class SoldierRobot extends BaseRobot {
    Direction d = Direction.EAST;


    public SoldierRobot(RobotController rc) {
        super(rc);
    }

    @Override
    public void run() throws GameActionException {
        RobotInfo[] enemyInfo = rc.senseHostileRobots(rc.getLocation(), rc.getType().sensorRadiusSquared);
        RobotInfo[] allies = rc.senseNearbyRobots(9, myTeam);
        boolean nearbyArchon = false;
        for (RobotInfo ri : allies) {
            if (ri.type == RobotType.ARCHON) {
                nearbyArchon = true;
            }
        }
        if (enemyInfo.length > 0 && allies.length > 1) {
            // trys to gang up on enemies with low health/dps ratio
            RobotInfo lowHdps = Utility.lowestHDPS(enemyInfo);
            if (rc.isWeaponReady()) {
                // look for adjacent enemies to attack
                if (rc.canAttackLocation(lowHdps.location)) {
                    rc.setIndicatorString(0, "trying to attack");
                    rc.attackLocation(lowHdps.location);
                }
            }
        } else if (enemyInfo.length > 0 && allies.length < 1 && !nearbyArchon)
            kite();
        else {
            defaultBehavior();
        }
    }

    public void kite() throws GameActionException {
        boolean dig = false;
        if (rc.isWeaponReady()) {
            RobotInfo[] enemyInfo = rc.senseHostileRobots(rc.getLocation(), rc.getType().attackRadiusSquared);
            //RobotInfo[] zombieInfo = rc.senseNearbyRobots(rc.getType().attackRadiusSquared, Team.ZOMBIE);

            //RobotInfo[] enemyInfo = Utility.combine(opponentInfo, zombieInfo);
            if (enemyInfo.length > 0) {
                rc.attackLocation(enemyInfo[0].location);
                d = rc.getLocation().directionTo(enemyInfo[0].location).opposite();
            }
        }
        boolean toTurn = Utility.isBlocked(rc, rc.getLocation().add(d, 3)) && rc.isCoreReady();
        if (toTurn) {
            if (rc.senseRubble((rc.getLocation().add(d, 1))) > 100.0) {
                dig = true;
            }
            if (Utility.isBlocked(rc, rc.getLocation().add(d.rotateRight().rotateRight(), 3)))
                d = d.rotateLeft();
            else {
                d = d.rotateRight();
            }
        }
        if (rc.isCoreReady()) {
            if (dig) {
                rc.clearRubble(d);
            } else if (rc.canMove(d)) {
                rc.move(d);
            }
        }
    }
}

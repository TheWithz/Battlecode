package team184;

import battlecode.common.*;

public class GuardRobot extends BaseRobot {

    Direction d = Direction.EAST;

    public GuardRobot(RobotController rc) {
        super(rc);
    }

    @Override
    public void run() throws GameActionException {
        guardCode();
    }

    private void guardCode() throws GameActionException {
        RobotInfo[] enemyArray = rc.senseHostileRobots(rc.getLocation(), 1000000);
        
        if (enemyArray.length > 0) {
            RobotInfo lowHdps = Utility.lowestHDPS(enemyArray);
            if (rc.isWeaponReady()) {
                //look for adjacent enemies to attack
                if (rc.canAttackLocation(lowHdps.location)) {
                    rc.setIndicatorString(0, "trying to attack");
                    rc.attackLocation(lowHdps.location);
                }
            }
            //could not find any enemies adjacent to attack
            //try to move toward them
            if (rc.isCoreReady()) {
                MapLocation goal = lowHdps.location;
                Direction toEnemy = rc.getLocation().directionTo(goal);
                tryToMove(toEnemy);
            }
        } else {//there are no enemies nearby
            //check to see if we are in the way of friends
            //we are obstructing them
            if (rc.isCoreReady()) {
                RobotInfo[] nearbyFriends = rc.senseNearbyRobots(2, myTeam);
                if (nearbyFriends.length > 3) {
                    Direction away = Direction.values()[random.nextInt(8)];
                    tryToMove(away);
                } else {//maybe a friend is in need!
                    RobotInfo[] alliesToHelp = rc.senseNearbyRobots(1000000, myTeam);
                    RobotInfo weakestOneRob = Utility.getRobotWithLowestHP(alliesToHelp);
                    if (weakestOneRob != null) {//found a friend most in need
                        MapLocation weakestOne = weakestOneRob.location;
                        Direction towardFriend = rc.getLocation().directionTo(weakestOne);
                        tryToMove(towardFriend);
                    }
                }
            }
        }
    }

    public void forward() throws GameActionException {
        RobotInfo[] enemyInfo = rc.senseHostileRobots(rc.getLocation(), rc.getType().attackRadiusSquared);
        if (rc.isWeaponReady()) {
            if (enemyInfo.length > 0 && rc.canAttackLocation(enemyInfo[0].location)) {
                rc.attackLocation(enemyInfo[0].location);
            }
        }
        if (rc.isCoreReady()) {
            if (enemyInfo.length > 0) {
                d = rc.getLocation().directionTo(enemyInfo[0].location);
            }
            if (rc.canMove(d))
                rc.move(d);
        }
    }
}

package vipers;

import battlecode.common.*;

import java.util.ArrayList;

public class TurretRobot  extends BaseRobot {

	public TurretRobot(RobotController rc){
		super(rc);
	}

	@Override
	public void run() throws GameActionException {
		if(rc.getType() == RobotType.TTM){
			RobotInfo[] visibleEnemyArray = rc.senseHostileRobots(rc.getLocation(), 1000000);
			Signal[] incomingSignals = rc.emptySignalQueue();
			ArrayList<MapLocation> attackableEnemyLocations = new ArrayList<MapLocation>();
			for(RobotInfo ri : visibleEnemyArray){
				attackableEnemyLocations.add(ri.location);
			}

			if(attackableEnemyLocations.size()>0){
				rc.unpack();
				//could not find any enemies adjacent to attack
				//try to move toward them
			}else{//there are no enemies nearby
				//check to see if we are in the way of friends
				//we are obstructing them
				if(rc.isCoreReady()){
					RobotInfo[] nearbyFriends = rc.senseNearbyRobots(2, rc.getTeam());
					if(nearbyFriends.length>3){
						Direction away = randomDirection();
						tryToMove(away);
					}else{//maybe a friend is in need!
						RobotInfo[] alliesToHelp = rc.senseNearbyRobots(1000000,rc.getTeam());
						MapLocation weakestOne = Utility.getRobotWithLowestHP(alliesToHelp).location;
						if(weakestOne!=null){//found a friend most in need
							Direction towardFriend = rc.getLocation().directionTo(weakestOne);
							tryToMove(towardFriend);
						}
					}
				}
			}
			return;
		}
		RobotInfo[] visibleEnemyArray = rc.senseHostileRobots(rc.getLocation(), 1000000);
		Signal[] incomingSignals = rc.emptySignalQueue();
		ArrayList<MapLocation> attackableEnemyLocations = new ArrayList<MapLocation>();
		for(RobotInfo ri : visibleEnemyArray){
			attackableEnemyLocations.add(ri.location);
		}
		for(Signal s : incomingSignals){
			if(s.getTeam() == myTeam.opponent()){
				if(rc.canAttackLocation(s.getLocation())){
					attackableEnemyLocations.add(s.getLocation());
				}
			}
			else if(s.getMessage() != null){
				MessageSignal message = new MessageSignal(s);
				if(message.getMessageType() == MessageSignal.MessageType.ROBOT){
					if(rc.canAttackLocation(message.getPingedLocation())){
						attackableEnemyLocations.add(message.getPingedLocation());
					}
				}
			}
		}

		if(attackableEnemyLocations.size()>0){
			if(rc.isWeaponReady()){
				//look for adjacent enemies to attack
				for(MapLocation oneEnemy:attackableEnemyLocations){
					if(rc.canAttackLocation(oneEnemy)){
						rc.setIndicatorString(0,"trying to attack");
						rc.attackLocation(oneEnemy);
						break;
					}
				}
			}
			//could not find any enemies adjacent to attack
			//try to move toward them
		}else{//there are no enemies nearby
			//check to see if we are in the way of friends
			//we are obstructing them
			if(rc.isCoreReady()){
				RobotInfo[] nearbyFriends = rc.senseNearbyRobots(2, rc.getTeam());
				if(nearbyFriends.length>3){
					Direction away = randomDirection();
					rc.pack();
				}
			}
		}
	}
}


package team184;

import battlecode.common.*;

import java.util.ArrayList;

public class ArchonRobot extends BaseRobot{
	private RobotType[] buildRobotTypes = {
			RobotType.SCOUT, RobotType.SCOUT,
			RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER, RobotType.SOLDIER,
			RobotType.GUARD, RobotType.GUARD,
	};

	int heiarchy = -1;
	public ArchonRobot(RobotController rc){
		super(rc);
	}
	
	private int leaderId;
	private MapLocation destination;
	private MapLocation leaderLocation;
	private boolean sentGoal;
	private Direction teamDirection;
	private ArrayList<MapLocation> neutralBotLocations = new ArrayList<MapLocation>();
	private ArrayList<MapLocation> knownZombieDenLocations = new ArrayList<MapLocation>();
	private int lastSentGoal;
	private boolean[] mapEdgesFound = new boolean[8];
	private boolean foundEnemyArchon;
	private boolean foundZombieDen;
	private MapLocation enemyArchon;
	private int[] mapEdges = new int[8];

	public void getSignals(){
		Signal[] queue = rc.emptySignalQueue();
		for(Signal signal : queue){
			if(signal.getTeam() == myTeam){
				if(signal.getMessage() != null){
					MessageSignal msgSig= new MessageSignal(signal);
					switch(msgSig.getMessageType()){
					case ROBOT:
						if(msgSig.getPingedTeam() == Team.NEUTRAL){
							rc.setIndicatorString(0,  "Found neutral");
							destination = msgSig.getPingedLocation();
						}
						if(msgSig.getPingedType() == RobotType.ARCHON && msgSig.getPingedTeam() == myTeam.opponent()){
							rc.setIndicatorString(0, "Found enemy Archon");
							foundEnemyArchon = true;
							sentGoal = false;
							enemyArchon = msgSig.getPingedLocation();
						}
						if(msgSig.getPingedType() == RobotType.ZOMBIEDEN){
							rc.setIndicatorString(2, "Found Zombie Den");
							knownZombieDenLocations.add(msgSig.getPingedLocation());
							foundZombieDen = true;
						}
						break;
					case PARTS:
						destination = msgSig.getPingedLocation();
						break;
					case MAP_EDGE:
						Direction edge = msgSig.getPingedDirection();
						MapLocation ml = msgSig.getPingedLocation();
						int loc = edge.ordinal();
						if(edge == Direction.EAST){
							mapEdgesFound[loc] = true;
							mapEdges[loc] = ml.x;
						}
						if(edge == Direction.NORTH){
							mapEdgesFound[loc] = true;
							mapEdges[loc] = ml.y;
						}
						if(edge == Direction.SOUTH){
							mapEdgesFound[loc] = true;
							mapEdges[loc] = ml.y;
						}
						if(edge == Direction.WEST){
							mapEdgesFound[loc] = true;
							mapEdges[loc] = ml.x;
						}
						break;
					default:
						break;
					}
				}
			}
		}
	}

	@Override
	public void initialize() throws GameActionException{
		Signal[] signals = rc.emptySignalQueue();
		rc.broadcastSignal(100*100);
		heiarchy = signals.length;
		rc.setIndicatorString(1, "I am the " + heiarchy + ": " + rc.getRoundNum());
		if(heiarchy == 0){
			teamLocation = rc.getLocation().add(teamDirection);
			rc.broadcastMessageSignal(1337, 0, 100*100);
			leaderId = rc.getID();
			
		}
		else{
			heiarchy-=1;
			for(Signal s : signals){
				if(s.getMessage() != null){
					if(s.getMessage()[0] == 1337){
						leaderId = s.getID();
						
						leaderLocation = s.getLocation();
						destination = leaderLocation;
					}
				}
			}
		}
		if(leaderLocation == null){
			System.out.println("no");
			leaderLocation = new MapLocation(0, 0);
		}
	}

	@Override
	public void prerun() throws GameActionException{

		if(rc.getRoundNum() % 5 == 0){
			RobotInfo[] robotsNearMe = rc.senseNearbyRobots();
			for(RobotInfo ri : robotsNearMe){
				if(ri.team == Team.NEUTRAL && !neutralBotLocations.contains(ri.location)){
					neutralBotLocations.add(ri.location);
				}
			}
		}
		getSignals();
		
		
		if(heiarchy == 0 && (!sentGoal || rc.getRoundNum() - lastSentGoal > 10)){
			MapLocation goal;
			MessageSignal goalDirection = new MessageSignal(rc);
			if(mapEdgesFound[teamDirection.ordinal()]){
				
			}
			if(foundZombieDen){
				goal = knownZombieDenLocations.get(knownZombieDenLocations.size()-1);
			}
			else if(foundEnemyArchon){
				goal = enemyArchon;
			}
			else{
				goal = rc.getLocation().add(teamDirection, 4);
			}
			goalDirection.setCommand(goal, MessageSignal.CommandType.MOVE);
			goalDirection.send(30*30);
			sentGoal = true;
			while(!rc.onTheMap(rc.getLocation().add(teamDirection, 4))){
				teamDirection = teamDirection.rotateRight();
				sentGoal = false;
			}
			
			lastSentGoal = rc.getRoundNum();
		}
		else{
			super.prerun();
		}
	}
	
	@Override
	public void run() throws GameActionException {

		//try to heal nearby robots
		RobotInfo[] nearbyAllies = rc.senseNearbyRobots(2, myTeam);
		RobotInfo friendWithLowestHP = Utility.getRobotWithLowestHP(nearbyAllies);
		if(rc.isCoreReady() && friendWithLowestHP != null){
			if(friendWithLowestHP.type != RobotType.ARCHON){
				rc.repair(friendWithLowestHP.location);
			}
		}
		
		MapLocation closestNeutral = Utility.closestLocation(neutralBotLocations, rc.getLocation());
		if (closestNeutral != null) {
			rc.setIndicatorString(2, "Finding Neutal");
			if(rc.canSense(closestNeutral) && rc.senseRobotAtLocation(closestNeutral) == null){
				neutralBotLocations.remove(closestNeutral);
			}
			else if (rc.getLocation().distanceSquaredTo(closestNeutral) < 2) {
				if (rc.isCoreReady()) {
					rc.activate(closestNeutral);
					neutralBotLocations.remove(closestNeutral);
				}
			} else if (rc.isCoreReady() && rc.canMove(rc.getLocation().directionTo(closestNeutral))) {
				try {
					rc.move(rc.getLocation().directionTo(closestNeutral));
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		//try to build a robot
		RobotType robot = buildRobotTypes[(random.nextInt(buildRobotTypes.length))];
		for(Direction d : Direction.values()){
			if (rc.canBuild(d, robot)) {
				if (rc.isCoreReady()) {
					rc.build(d, robot);
					MessageSignal teamFirstDirective = new MessageSignal(rc);
					teamFirstDirective.setCommand(leaderLocation, MessageSignal.CommandType.MOVE);
					teamFirstDirective.send(2);
				}
			}
		}

		RobotInfo[] enemies = rc.senseHostileRobots(rc.getLocation(), RobotType.ARCHON.sensorRadiusSquared);
		if(enemies.length > 0){
			tryToRetreat(enemies);
		}else{
			defaultBehavior();
		}
	}
}

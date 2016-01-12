package vipers;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class BugNav {
	private static MapLocation dest;
	
	private enum BugState{
		DIRECT, BUG
	}
	
	private enum WallSide{
		LEFT, RIGHT
	}
	
	private static BugState bugState;
	public static WallSide bugWallSide = WallSide.LEFT;
	private static int bugStartDistSq;
	private static Direction bugLastMoveDir;
	private static Direction bugLookStartDir;
	private static int bugRotationCount;
}

package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import static java.lang.Math.max;

public class Bot {

    private static final int maxSpeed = 9;
    private static final int boostSpeed = 15;

    private GameState gameState;
    private Car opponent;
    private Car myCar;
    private final static Command FIX = new FixCommand();
    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);



    public Bot(GameState gameState) {
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;
    }

    public Command run() {
        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block+1, myCar.speed);
        List<Object> blocksboost = getBlocksInFront(myCar.position.lane, myCar.position.block+1, boostSpeed);

        //EMERGENCY FIX
        if (myCar.damage == 5) {
            return FIX;
        }

        //Avoidance logic, prioritizes middle lanes
        if (blocks.contains(Terrain.MUD) || blocks.contains(Terrain.WALL)) {
        if (myCar.position.lane == 1) {
            List<Object> rightblocks = getBlocksInFront(myCar.position.lane + 1, myCar.position.block-1, myCar.speed - 1);

            if (!(rightblocks.contains(Terrain.MUD) || rightblocks.contains(Terrain.WALL))) {
                return TURN_RIGHT;
            }
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups) && (blocks.get(myCar.speed-1) != Terrain.WALL)) {
                return LIZARD;
            }
            return TURN_RIGHT;

        } else if (myCar.position.lane == 2) {
            List<Object> rightblocks = getBlocksInFront(myCar.position.lane + 1, myCar.position.block-1, myCar.speed - 1);
            List<Object> leftblocks = getBlocksInFront(myCar.position.lane - 1, myCar.position.block-1, myCar.speed - 1);

            if (!(rightblocks.contains(Terrain.MUD) || rightblocks.contains(Terrain.WALL))) {
                return TURN_RIGHT;
            }
            if (!(leftblocks.contains(Terrain.MUD) || leftblocks.contains(Terrain.WALL))) {
                return TURN_LEFT;
            }
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups) && (blocks.get(myCar.speed-1) != Terrain.WALL)) {
                return LIZARD;
            }
            if (blocks.contains(Terrain.WALL) && !rightblocks.contains(Terrain.WALL)) {
                return TURN_RIGHT;
            }
            if (blocks.contains(Terrain.WALL) && !leftblocks.contains(Terrain.WALL)) {
                return TURN_LEFT;
            }
        } else if (myCar.position.lane == 3) {
            List<Object> rightblocks = getBlocksInFront(myCar.position.lane + 1, myCar.position.block-1, myCar.speed - 1);
            List<Object> leftblocks = getBlocksInFront(myCar.position.lane - 1, myCar.position.block-1, myCar.speed - 1);

            if (!(leftblocks.contains(Terrain.MUD) || leftblocks.contains(Terrain.WALL))) {
                return TURN_LEFT;
            }
            if (!(rightblocks.contains(Terrain.MUD) || rightblocks.contains(Terrain.WALL))) {
                return TURN_RIGHT;
            }
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups) && (blocks.get(myCar.speed-1) != Terrain.WALL)) {
                return LIZARD;
            }
            if (blocks.contains(Terrain.WALL) && !leftblocks.contains(Terrain.WALL)) {
                return TURN_LEFT;
            }
            if (blocks.contains(Terrain.WALL) && !rightblocks.contains(Terrain.WALL)) {
                return TURN_RIGHT;
            }
        } else {
            List<Object> leftblocks = getBlocksInFront(myCar.position.lane - 1, myCar.position.block-1, myCar.speed - 1);

            if (!(leftblocks.contains(Terrain.MUD) || leftblocks.contains(Terrain.WALL))) {
                return TURN_LEFT;
            }
            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups) && (blocks.get(myCar.speed-1) != Terrain.WALL)) {
                return LIZARD;
            }
            return TURN_LEFT;
        }
    }
        //FIX A LOT SO HOPEFULLY DOESNT BOTTLENECK SPEED
        if(myCar.damage > 0){
            return FIX;
        }
        //BOOSTOO: WILL TRY TO BOOST IF next 15 blocks no obstacles
        //         IF POSSIBLE< FIXES FIRST BEFORE BOOSTING
        if(hasPowerUp(PowerUps.BOOST, myCar.powerups) &&!(blocksboost.contains(Terrain.MUD) || blocksboost.contains(Terrain.WALL))){
            return BOOST;
        }

        //GO FAST VVROOOOMMMMM
        if(myCar.speed < maxSpeed){
            return ACCELERATE;
        }

        //POWER-UPUU
        if (hasPowerUp(PowerUps.TWEET,myCar.powerups)) {
            return new TweetCommand(opponent.position.lane,opponent.position.block+opponent.speed+1);
        }
        if (hasPowerUp(PowerUps.OIL, myCar.powerups)) {
            return OIL;
        }
        if (hasPowerUp(PowerUps.EMP, myCar.powerups)) {
            return EMP;
        }

        return ACCELERATE;
    }

    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Returns map of blocks and the objects in the for the current lanes, returns the amount of blocks that can be
     * traversed at max speed.
     **/
    private List<Object> getBlocksInFront(int lane, int block,int speed) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + speed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }

}

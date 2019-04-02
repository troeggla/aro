package nl.cwi.dis.aro.views.directionhandlers;

import nl.cwi.dis.aro.views.RockerView.Direction;

public class DiagonalFourWayDirectionHandler implements DirectionHandler {
    private static final double ANGLE_360 = 360;

    private static final double ANGLE_4D_OF_0P = 0;
    private static final double ANGLE_4D_OF_1P = 90;
    private static final double ANGLE_4D_OF_2P = 180;
    private static final double ANGLE_4D_OF_3P = 270;

    private Direction prevDirection;

    @Override
    public Direction getMoveDirection(double angle) {
        if (ANGLE_4D_OF_0P <= angle && ANGLE_4D_OF_1P > angle) {
            return Direction.DIRECTION_DOWN_RIGHT;
        } else if (ANGLE_4D_OF_1P <= angle && ANGLE_4D_OF_2P > angle) {
            return Direction.DIRECTION_DOWN_LEFT;
        } else if (ANGLE_4D_OF_2P <= angle && ANGLE_4D_OF_3P > angle) {
            return Direction.DIRECTION_UP_LEFT;
        } else if (ANGLE_4D_OF_3P <= angle && ANGLE_360 > angle) {
            return Direction.DIRECTION_UP_RIGHT;
        }

        return Direction.DIRECTION_CENTER;
    }

    @Override
    public Direction getStateChangeDirection(double angle) {
        Direction newDirection;

        if (ANGLE_4D_OF_0P <= angle && ANGLE_4D_OF_1P > angle && prevDirection != Direction.DIRECTION_DOWN_RIGHT) {
            newDirection = Direction.DIRECTION_DOWN_RIGHT;
        } else if (ANGLE_4D_OF_1P <= angle && ANGLE_4D_OF_2P > angle && prevDirection != Direction.DIRECTION_DOWN_LEFT) {
            newDirection = Direction.DIRECTION_DOWN_LEFT;
        } else if (ANGLE_4D_OF_2P <= angle && ANGLE_4D_OF_3P > angle && prevDirection != Direction.DIRECTION_UP_LEFT) {
            newDirection = Direction.DIRECTION_UP_LEFT;
        } else if (ANGLE_4D_OF_3P <= angle && ANGLE_360 > angle && prevDirection != Direction.DIRECTION_UP_RIGHT) {
            newDirection = Direction.DIRECTION_UP_RIGHT;
        } else {
            newDirection = Direction.DIRECTION_CENTER;
        }

        prevDirection = newDirection;
        return newDirection;
    }
}

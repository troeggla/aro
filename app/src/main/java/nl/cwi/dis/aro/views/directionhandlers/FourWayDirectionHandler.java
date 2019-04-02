package nl.cwi.dis.aro.views.directionhandlers;

import nl.cwi.dis.aro.views.RockerView.Direction;

public class FourWayDirectionHandler implements DirectionHandler {
    private static final double ANGLE_0 = 0;
    private static final double ANGLE_360 = 0;

    private static final double ANGLE_ROTATE45_4D_OF_0P = 45;
    private static final double ANGLE_ROTATE45_4D_OF_1P = 135;
    private static final double ANGLE_ROTATE45_4D_OF_2P = 225;
    private static final double ANGLE_ROTATE45_4D_OF_3P = 315;

    private Direction prevDirection = Direction.DIRECTION_CENTER;

    @Override
    public Direction getMoveDirection(double angle) {
        if (ANGLE_0 <= angle && ANGLE_ROTATE45_4D_OF_0P > angle || ANGLE_ROTATE45_4D_OF_3P <= angle && ANGLE_360 > angle) {
            return Direction.DIRECTION_RIGHT;
        } else if (ANGLE_ROTATE45_4D_OF_0P <= angle && ANGLE_ROTATE45_4D_OF_1P > angle) {
            return Direction.DIRECTION_DOWN;
        } else if (ANGLE_ROTATE45_4D_OF_1P <= angle && ANGLE_ROTATE45_4D_OF_2P > angle) {
            return Direction.DIRECTION_LEFT;
        } else if (ANGLE_ROTATE45_4D_OF_2P <= angle && ANGLE_ROTATE45_4D_OF_3P > angle) {
            return Direction.DIRECTION_UP;
        }

        return Direction.DIRECTION_CENTER;
    }

    @Override
    public Direction getStateChangeDirection(double angle) {
        Direction newDirection;

        if ((ANGLE_0 <= angle && ANGLE_ROTATE45_4D_OF_0P > angle || ANGLE_ROTATE45_4D_OF_3P <= angle && ANGLE_360 > angle) && prevDirection != Direction.DIRECTION_RIGHT) {
            newDirection = Direction.DIRECTION_RIGHT;
        } else if (ANGLE_ROTATE45_4D_OF_0P <= angle && ANGLE_ROTATE45_4D_OF_1P > angle && prevDirection != Direction.DIRECTION_DOWN) {
            newDirection = Direction.DIRECTION_DOWN;
        } else if (ANGLE_ROTATE45_4D_OF_1P <= angle && ANGLE_ROTATE45_4D_OF_2P > angle && prevDirection != Direction.DIRECTION_LEFT) {
            newDirection = Direction.DIRECTION_LEFT;
        } else if (ANGLE_ROTATE45_4D_OF_2P <= angle && ANGLE_ROTATE45_4D_OF_3P > angle && prevDirection != Direction.DIRECTION_UP) {
            newDirection = Direction.DIRECTION_UP;
        } else {
            newDirection = Direction.DIRECTION_CENTER;
        }

        prevDirection = newDirection;
        return newDirection;
    }
}

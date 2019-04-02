package nl.cwi.dis.aro.views.directionhandlers;

import nl.cwi.dis.aro.views.RockerView.Direction;

public class EightWayDirectionHandler implements DirectionHandler {
    private static final double ANGLE_0 = 0;
    private static final double ANGLE_360 = 360;

    private static final double ANGLE_8D_OF_0P = 22.5;
    private static final double ANGLE_8D_OF_1P = 67.5;
    private static final double ANGLE_8D_OF_2P = 112.5;
    private static final double ANGLE_8D_OF_3P = 157.5;
    private static final double ANGLE_8D_OF_4P = 202.5;
    private static final double ANGLE_8D_OF_5P = 247.5;
    private static final double ANGLE_8D_OF_6P = 292.5;
    private static final double ANGLE_8D_OF_7P = 337.5;

    private Direction prevDirection = Direction.DIRECTION_CENTER;

    @Override
    public Direction getMoveDirection(double angle) {
        if (ANGLE_0 <= angle && ANGLE_8D_OF_0P > angle || ANGLE_8D_OF_7P <= angle && ANGLE_360 > angle) {
            return Direction.DIRECTION_RIGHT;
        } else if (ANGLE_8D_OF_0P <= angle && ANGLE_8D_OF_1P > angle) {
            return Direction.DIRECTION_DOWN_RIGHT;
        } else if (ANGLE_8D_OF_1P <= angle && ANGLE_8D_OF_2P > angle) {
            return Direction.DIRECTION_DOWN;
        } else if (ANGLE_8D_OF_2P <= angle && ANGLE_8D_OF_3P > angle) {
            return Direction.DIRECTION_DOWN_LEFT;
        } else if (ANGLE_8D_OF_3P <= angle && ANGLE_8D_OF_4P > angle) {
            return Direction.DIRECTION_LEFT;
        } else if (ANGLE_8D_OF_4P <= angle && ANGLE_8D_OF_5P > angle) {
            return Direction.DIRECTION_UP_LEFT;
        } else if (ANGLE_8D_OF_5P <= angle && ANGLE_8D_OF_6P > angle) {
            return Direction.DIRECTION_UP;
        } else if (ANGLE_8D_OF_6P <= angle && ANGLE_8D_OF_7P > angle) {
            return Direction.DIRECTION_UP_RIGHT;
        }

        return Direction.DIRECTION_CENTER;
    }

    @Override
    public Direction getStateChangeDirection(double angle) {
        if ((ANGLE_0 <= angle && ANGLE_8D_OF_0P > angle || ANGLE_8D_OF_7P <= angle && ANGLE_360 > angle) && prevDirection != Direction.DIRECTION_RIGHT) {
            prevDirection = Direction.DIRECTION_RIGHT;
        } else if (ANGLE_8D_OF_0P <= angle && ANGLE_8D_OF_1P > angle && prevDirection != Direction.DIRECTION_DOWN_RIGHT) {
            prevDirection = Direction.DIRECTION_DOWN_RIGHT;
        } else if (ANGLE_8D_OF_1P <= angle && ANGLE_8D_OF_2P > angle && prevDirection != Direction.DIRECTION_DOWN) {
            prevDirection = Direction.DIRECTION_DOWN;
        } else if (ANGLE_8D_OF_2P <= angle && ANGLE_8D_OF_3P > angle && prevDirection != Direction.DIRECTION_DOWN_LEFT) {
            prevDirection = Direction.DIRECTION_DOWN_LEFT;
        } else if (ANGLE_8D_OF_3P <= angle && ANGLE_8D_OF_4P > angle && prevDirection != Direction.DIRECTION_LEFT) {
            prevDirection = Direction.DIRECTION_LEFT;
        } else if (ANGLE_8D_OF_4P <= angle && ANGLE_8D_OF_5P > angle && prevDirection != Direction.DIRECTION_UP_LEFT) {
            prevDirection = Direction.DIRECTION_UP_LEFT;
        } else if (ANGLE_8D_OF_5P <= angle && ANGLE_8D_OF_6P > angle && prevDirection != Direction.DIRECTION_UP) {
            prevDirection = Direction.DIRECTION_UP;
        } else if (ANGLE_8D_OF_6P <= angle && ANGLE_8D_OF_7P > angle && prevDirection != Direction.DIRECTION_UP_RIGHT) {
            prevDirection = Direction.DIRECTION_UP_RIGHT;
        }

        return prevDirection;
    }

    @Override
    public void reset() {
        prevDirection = Direction.DIRECTION_CENTER;
    }
}

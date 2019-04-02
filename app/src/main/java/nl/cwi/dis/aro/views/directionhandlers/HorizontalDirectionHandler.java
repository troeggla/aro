package nl.cwi.dis.aro.views.directionhandlers;

import nl.cwi.dis.aro.views.RockerView.Direction;

public class HorizontalDirectionHandler implements DirectionHandler {
    private static final double ANGLE_0 = 0;
    private static final double ANGLE_360 = 360;

    private static final double ANGLE_HORIZONTAL_2D_OF_0P = 90;
    private static final double ANGLE_HORIZONTAL_2D_OF_1P = 270;

    private Direction prevDirection;

    @Override
    public Direction getMoveDirection(double angle) {
        if (ANGLE_0 <= angle && ANGLE_HORIZONTAL_2D_OF_0P > angle || ANGLE_HORIZONTAL_2D_OF_1P <= angle && ANGLE_360 > angle) {
            return Direction.DIRECTION_RIGHT;
        } else if (ANGLE_HORIZONTAL_2D_OF_0P <= angle && ANGLE_HORIZONTAL_2D_OF_1P > angle) {
            return Direction.DIRECTION_LEFT;
        }

        return Direction.DIRECTION_CENTER;
    }

    @Override
    public Direction getStateChangeDirection(double angle) {
        Direction newDirection;

        if ((ANGLE_0 <= angle && ANGLE_HORIZONTAL_2D_OF_0P > angle || ANGLE_HORIZONTAL_2D_OF_1P <= angle && ANGLE_360 > angle) && prevDirection != Direction.DIRECTION_RIGHT) {
            newDirection = Direction.DIRECTION_RIGHT;
        } else if (ANGLE_HORIZONTAL_2D_OF_0P <= angle && ANGLE_HORIZONTAL_2D_OF_1P > angle && prevDirection != Direction.DIRECTION_LEFT) {
            newDirection = Direction.DIRECTION_LEFT;
        } else {
            newDirection = Direction.DIRECTION_CENTER;
        }

        prevDirection = newDirection;
        return newDirection;
    }
}

package nl.cwi.dis.aro.views.directionhandlers;

import nl.cwi.dis.aro.views.RockerView.Direction;

public class VerticalDirectionHandler implements DirectionHandler {
    private static final double ANGLE_360 = 360;

    private static final double ANGLE_VERTICAL_2D_OF_0P = 0;
    private static final double ANGLE_VERTICAL_2D_OF_1P = 180;

    private Direction prevDirection;

    @Override
    public Direction getMoveDirection(double angle) {
        if (ANGLE_VERTICAL_2D_OF_0P <= angle && ANGLE_VERTICAL_2D_OF_1P > angle) {
            return Direction.DIRECTION_DOWN;
        } else if (ANGLE_VERTICAL_2D_OF_1P <= angle && ANGLE_360 > angle) {
            return Direction.DIRECTION_UP;
        }

        return Direction.DIRECTION_CENTER;
    }

    @Override
    public Direction getStateChangeDirection(double angle) {
        if (ANGLE_VERTICAL_2D_OF_0P <= angle && ANGLE_VERTICAL_2D_OF_1P > angle && prevDirection != Direction.DIRECTION_DOWN) {
            prevDirection =  Direction.DIRECTION_DOWN;
        } else if (ANGLE_VERTICAL_2D_OF_1P <= angle && ANGLE_360 > angle && prevDirection != Direction.DIRECTION_UP) {
            prevDirection =  Direction.DIRECTION_UP;
        }

        return prevDirection;
    }

    @Override
    public void reset() {
        prevDirection = Direction.DIRECTION_CENTER;
    }
}

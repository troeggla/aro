package nl.cwi.dis.aro.views.directionhandlers;

import nl.cwi.dis.aro.views.RockerView.Direction;

public interface DirectionHandler {
    Direction getStateChangeDirection(double angle);
    Direction getMoveDirection(double angle);
    void reset();
}

package nl.cwi.dis.aro;

import org.junit.Test;

import nl.cwi.dis.aro.extras.UserAnnotation;

import static org.junit.Assert.*;

public class UserAnnotationTest {
    @Test
    public void newObjectHasTimestamp() {
        UserAnnotation annotation = new UserAnnotation(
                123456,
                "/path/to/video1.mp4",
                5.0,
                7.0
        );

        assertEquals(123456, annotation.getTimestamp(), 0);
    }

    @Test
    public void newObjectHasTimestampZero() {
        UserAnnotation annotation = new UserAnnotation(
                "/path/to/video1.mp4",
                5.0,
                7.0
        );

        assertEquals(0, annotation.getTimestamp(), 0);
    }

    @Test
    public void newObjectHasArousal() {
        UserAnnotation annotation = new UserAnnotation(
                "/path/to/video1.mp4",
                5.0,
                7.0
        );

        assertEquals(5, annotation.getArousal(), 0);
    }

    @Test
    public void newObjectHasValence() {
        UserAnnotation annotation = new UserAnnotation(
                "/path/to/video1.mp4",
                5.0,
                7.0
        );

        assertEquals(7, annotation.getValence(), 0);
    }

    @Test
    public void getVideoNameOnlyReturnsName() {
        UserAnnotation annotation = new UserAnnotation(
                "/path/to/video1.mp4",
                5.0,
                7.0
        );

        assertEquals("video1.mp4", annotation.getVideoName());
    }

    @Test
    public void getVideoNameWithoutPath() {
        UserAnnotation annotation = new UserAnnotation(
                "video1.mp4",
                5.0,
                7.0
        );

        assertEquals("video1.mp4", annotation.getVideoName());
    }

    @Test
    public void getVideoNameEmpty() {
        UserAnnotation annotation = new UserAnnotation(
                "",
                5.0,
                7.0
        );

        assertEquals("", annotation.getVideoName());
    }
}

package nl.cwi.dis.aro;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import nl.cwi.dis.aro.extras.UserAnnotation;
import nl.cwi.dis.aro.extras.UserSession;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UserSessionTest {
    @Test
    public void newObjectHasName() {
        UserSession session = new UserSession(
                "name",
                100,
                "male",
                new ArrayList<>()
        );

        assertEquals("name", session.getName());
    }

    @Test
    public void newObjectHasVideoIndexZero() {
        UserSession session = new UserSession(
                "name",
                100,
                "male",
                new ArrayList<>()
        );

        assertEquals(0, session.getVideoIndex());
    }

    @Test
    public void newObjectHasNoAnnotations() {
        UserSession session = new UserSession(
                "name",
                100,
                "male",
                new ArrayList<>()
        );

        assertEquals(0, session.getAnnotations().size());
    }

    @Test
    public void newObjectHasNoQuestionnaireResponses() {
        UserSession session = new UserSession(
                "name",
                100,
                "male",
                new ArrayList<>()
        );

        assertEquals(0, session.getQuestionnaireResponses().size());
    }

    @Test
    public void newObjectHasVideoPathFirstEntry() {
        ArrayList<String> videos = new ArrayList<>(Arrays.asList(
                "video0.mp4",
                "video1.mp4"
        ));

        UserSession session = new UserSession(
                "name",
                100,
                "male",
                videos
        );

        assertEquals("video0.mp4", session.getCurrentVideoPath());
    }

    @Test
    public void videoIndexIsIncremented() {
        ArrayList<String> videos = new ArrayList<>(Arrays.asList(
                "video0.mp4",
                "video1.mp4"
        ));

        UserSession session = new UserSession(
                "name",
                100,
                "male",
                videos
        );

        assertEquals(0, session.getVideoIndex());
        session.incrementVideoIndex();
        assertEquals(1, session.getVideoIndex());
    }

    @Test
    public void videoPathIsChangedOnIncrement() {
        ArrayList<String> videos = new ArrayList<>(Arrays.asList(
                "video0.mp4",
                "video1.mp4"
        ));

        UserSession session = new UserSession(
                "name",
                100,
                "male",
                videos
        );

        assertEquals("video0.mp4", session.getCurrentVideoPath());
        session.incrementVideoIndex();
        assertEquals("video1.mp4", session.getCurrentVideoPath());
    }

    @Test
    public void videoPathReturnsNullAtListEnd() {
        ArrayList<String> videos = new ArrayList<>(Arrays.asList(
                "video0.mp4"
        ));

        UserSession session = new UserSession(
                "name",
                100,
                "male",
                videos
        );

        assertEquals("video0.mp4", session.getCurrentVideoPath());
        session.incrementVideoIndex();
        assertNull(session.getCurrentVideoPath());
    }

    @Test
    public void addAnnotation() {
        UserSession session = new UserSession(
                "name",
                100,
                "male",
                new ArrayList<>(Arrays.asList("video0.mp4"))
        );

        session.addAnnotation(5.0, 7.0);

        assertEquals(1, session.getAnnotations().size());
        UserAnnotation annotation = session.getAnnotations().get(0);

        assertEquals("video0.mp4", annotation.getVideoName());
        assertEquals(5.0, annotation.getArousal(), 0);
        assertEquals(7.0, annotation.getValence(), 0);
        assertEquals(System.currentTimeMillis() / 1000.0, annotation.getTimestamp(), 1);
    }

    @Test
    public void addQuestionnaireResponse() {
        UserSession session = new UserSession(
                "name",
                100,
                "male",
                new ArrayList<>(Arrays.asList("video1.mp4"))
        );

        session.addQuestionnaireResponse(5.3, 7.9);

        assertEquals(1, session.getQuestionnaireResponses().size());
        UserAnnotation response = session.getQuestionnaireResponses().get(0);

        assertEquals("video1.mp4", response.getVideoName());
        assertEquals(5.3, response.getArousal(), 0);
        assertEquals(7.9, response.getValence(), 0);
        assertEquals(0, response.getTimestamp(), 0);
    }
}

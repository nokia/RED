package org.rf.ide.core.testdata.text.read.recognizer.tasks;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class TaskTagsRecognizerTest {

    @Test
    public void taskTagsIsRecognized_whenRobotVersionIs31() throws Exception {
        final TaskTagsRecognizer recognizer = new TaskTagsRecognizer();

        assertThat(recognizer.isApplicableFor(new RobotVersion(2, 8))).isFalse();
        assertThat(recognizer.isApplicableFor(new RobotVersion(2, 9))).isFalse();
        assertThat(recognizer.isApplicableFor(new RobotVersion(3, 0))).isFalse();
        assertThat(recognizer.isApplicableFor(new RobotVersion(3, 0, 9))).isFalse();
        assertThat(recognizer.isApplicableFor(new RobotVersion(3, 1))).isTrue();
        assertThat(recognizer.isApplicableFor(new RobotVersion(3, 1, 5))).isTrue();
        assertThat(recognizer.isApplicableFor(new RobotVersion(3, 2))).isTrue();
    }

    @Test
    public void newInstanceReturnsFreshRecognizer() {
        final TaskTagsRecognizer recognizer = new TaskTagsRecognizer();

        final ATokenRecognizer newRecognizer = recognizer.newInstance();
        assertThat(newRecognizer).isInstanceOf(TaskTagsRecognizer.class).isNotSameAs(recognizer);
    }

    @Test
    public void recognitionTest() {
        final TaskTagsRecognizer recognizer = new TaskTagsRecognizer();

        assertThat(recognizer.hasNext("", 1, 0)).isFalse();
        assertThat(recognizer.hasNext("tag", 1, 0)).isFalse();
        assertThat(recognizer.hasNext("tags", 1, 0)).isFalse();
        assertThat(recognizer.hasNext("[tag]", 1, 0)).isFalse();

        assertThat(recognizer.hasNext("[tags]", 1, 0)).isTrue();
        final RobotToken recognizedToken = recognizer.next();
        assertThat(recognizedToken.getText()).isEqualTo("[tags]");
        assertThat(recognizedToken.getTypes()).containsOnly(RobotTokenType.TASK_SETTING_TAGS_DECLARATION);
    }
}

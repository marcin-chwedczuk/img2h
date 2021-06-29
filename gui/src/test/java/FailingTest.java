import org.junit.jupiter.api.Test;
import org.testfx.assertions.api.Assertions;

import static org.assertj.core.api.Assertions.assertThat;

public class FailingTest {
    @Test
    void failing() {
        assertThat(1).isEqualTo(2);
    }
}

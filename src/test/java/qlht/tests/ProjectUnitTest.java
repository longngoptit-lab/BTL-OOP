package qlht.tests;

import org.junit.jupiter.api.Test;

/** Cầu nối JUnit để mvn test chạy cùng các kiểm tra Java thuần trong script. */
class ProjectUnitTest {
    @Test void domainRules() throws Exception { DomainValidationTest.main(new String[0]); }
    @Test void weeklyLayout() throws Exception { WeekScheduleTest.main(new String[0]); }
}

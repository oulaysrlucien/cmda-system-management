package org.cmda.management;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CmdaSystemManagementApplicationTests {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void r8E3MemberSchemaIsMigrated() {
		Integer migrationVersion = jdbcTemplate.queryForObject(
				"SELECT MAX(CAST(version AS UNSIGNED)) FROM flyway_schema_history WHERE success = 1",
				Integer.class
		);
		Integer journeyStagesCount = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM member_journey_stages",
				Integer.class
		);
		Integer lifeStatesCount = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM member_life_states",
				Integer.class
		);
		Integer journeyHistoryTableCount = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM information_schema.tables "
						+ "WHERE table_schema = DATABASE() AND table_name = 'member_journey_history'",
				Integer.class
		);
		Integer baptismDateColumnCount = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM information_schema.columns "
						+ "WHERE table_schema = DATABASE() AND table_name = 'cmda_members' "
						+ "AND column_name = 'baptism_date'",
				Integer.class
		);

		assertThat(migrationVersion).isGreaterThanOrEqualTo(3);
		assertThat(journeyStagesCount).isEqualTo(10);
		assertThat(lifeStatesCount).isEqualTo(3);
		assertThat(journeyHistoryTableCount).isEqualTo(1);
		assertThat(baptismDateColumnCount).isEqualTo(1);
	}

}

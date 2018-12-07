package service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.factory.BackendApplication;
import com.factory.dto.LineDto;
import com.factory.service.StationServiceImpl;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackendApplication.class)
public class StationServiceImplTest {

	@Autowired
	private StationServiceImpl stationService;

	@Test
	public void testInitData() {
		try {
			stationService.initData();
		} catch (Exception e) {
			fail("initData threw exception");
		}
	}

	@Test
	public void testFindAllLines() {
		List<LineDto> lines = stationService.findAllLines();
		assertEquals(53, lines.size());
	}

}

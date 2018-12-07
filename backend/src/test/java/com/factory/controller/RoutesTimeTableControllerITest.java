package com.factory.controller;

import com.factory.BackendApplication;
import com.factory.common.api.RestApiConstants;
import com.factory.common.api.RestApiEndpoints;
import com.factory.common.api.RestApiRequestParams;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {BackendApplication.class}, loader = SpringBootContextLoader.class)
@TestPropertySource(properties = {"factory.lines=https://s3-eu-west-1.amazonaws.com/zadatak.5dananajavi.com/lines-test-1"})
public class RoutesTimeTableControllerITest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private void getRoutesAndValidateEveryHasProperBusLineForSecondActivity(String date, String line) throws Exception {
        MvcResult result = mockMvc.perform(get(RestApiEndpoints.ROUTES)
                .param(RestApiRequestParams.START, "45.262348,19.814235")
                .param(RestApiRequestParams.END, "45.261223,19.820446")
                .param(RestApiRequestParams.DATE, date)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andReturn();
        JSONArray routes = new JSONObject(result.getResponse().getContentAsString()).getJSONArray(RestApiConstants.ROUTES);
        for (int i = 0; i < 3; i++) {
            JSONArray activities = routes.getJSONObject(i).getJSONArray(RestApiConstants.ACTIVITIES);
            Assert.assertEquals(activities.getJSONObject(1).getString(RestApiConstants.BUS_NUMBER), line);
        }
    }

    @Test
    public void should_return_proper_bus_for_work_day() throws Exception {
        getRoutesAndValidateEveryHasProperBusLineForSecondActivity("2018-12-11T07:00:00.000", "Line 1");
    }

    @Test
    public void should_return_proper_bus_for_sunday() throws Exception {
        getRoutesAndValidateEveryHasProperBusLineForSecondActivity("2018-12-08T16:00:00.000", "Line 2");
    }

    @Test
    public void should_return_proper_bus_for_saturday() throws Exception {
        getRoutesAndValidateEveryHasProperBusLineForSecondActivity("2018-12-09T15:00:00.000", "Line 1");
    }

}

package com.factory.controller;

import com.factory.BackendApplication;
import com.factory.common.api.RestApiConstants;
import com.factory.common.api.RestApiEndpoints;
import com.factory.common.api.RestApiRequestParams;
import com.jayway.jsonpath.JsonPath;
import org.json.JSONArray;
import org.json.JSONObject;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {BackendApplication.class}, loader = SpringBootContextLoader.class)
@TestPropertySource(properties = {"factory.zones=https://s3-eu-west-1.amazonaws.com/zadatak.5dananajavi.com/zones"})
public class RoutesControllerITest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private JSONArray getRoutes(String start, String end) throws Exception {
        MvcResult result =  mockMvc.perform(get(RestApiEndpoints.ROUTES)
                .param(RestApiRequestParams.START, start)
                .param(RestApiRequestParams.END, end)
                .param(RestApiRequestParams.DATE, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andReturn();
        return new JSONObject(result.getResponse().getContentAsString()).getJSONArray(RestApiConstants.ROUTES);
    }

    @Test
    public void should_return_only_walk_routes() throws Exception {
        JSONArray routes = getRoutes("19.8287709,45.2547473", "19.8344797,45.2571111");
        for (int i = 0; i < 3; i++) {
            JSONArray activities = routes.getJSONObject(i).getJSONArray(RestApiConstants.ACTIVITIES);
            for (int j = 0; j < activities.length(); j++) {
                assert Integer.valueOf(activities.getJSONObject(j).getString(RestApiConstants.TYPE)) == 1;
            }
        }
    }

    @Test
    public void should_return_routes_including_bus() throws Exception {
        JSONArray routes = getRoutes("19.7906489942927,45.2486308914001", "19.8441568,45.2654541");
        for (int i = 0; i < 3; i++) {
            JSONArray activities = routes.getJSONObject(i).getJSONArray(RestApiConstants.ACTIVITIES);
            List<String> typesList = new ArrayList<>();
            for (int j = 0; j < activities.length(); j++) {
                typesList.add(activities.getJSONObject(j).getString(RestApiConstants.TYPE));
            }
            assert typesList.contains("2");
        }
    }

    @Test
    public void should_return_three_possible_routes() throws Exception {
        assert getRoutes("19.7906489942927,45.2486308914001", "19.8441568,45.2654541").length() == 3;
    }

}

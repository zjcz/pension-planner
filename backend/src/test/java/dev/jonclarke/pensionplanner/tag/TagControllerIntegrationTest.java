package dev.jonclarke.pensionplanner.tag;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "DATABASE_PATH=file:pensionflow?mode=memory&cache=shared")
@AutoConfigureMockMvc
class TagControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    TagRepository tagRepository;

    @Test
    void createListDeleteFlow() throws Exception {
        Cookie cookie = register("tag_alice", "password123");

        // Create
        String createResponse = mockMvc.perform(post("/api/v1/tags")
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Work\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Work"))
                .andReturn().getResponse().getContentAsString();

        Long createdId = ((Number) com.jayway.jsonpath.JsonPath.read(createResponse, "$.id")).longValue();

        // List
        mockMvc.perform(get("/api/v1/tags").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Work"));

        // Create second tag
        mockMvc.perform(post("/api/v1/tags")
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Savings\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/tags").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // Delete
        mockMvc.perform(delete("/api/v1/tags/" + createdId).cookie(cookie))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/tags").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Savings"));
    }

    @Test
    void validationRejectsBlankName() throws Exception {
        Cookie cookie = register("tag_bob", "password123");

        mockMvc.perform(post("/api/v1/tags")
                        .cookie(cookie)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tagsAreIsolatedPerUser() throws Exception {
        Cookie alice = register("tag_carol", "password123");
        Cookie bob = register("tag_dave", "password123");

        mockMvc.perform(post("/api/v1/tags")
                        .cookie(alice)
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Alice Tag\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/tags").cookie(bob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private Cookie register(String username, String password) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse();
        return new Cookie("pp_jwt", cookieValueFrom(response, "pp_jwt"));
    }

    private static String cookieValueFrom(MockHttpServletResponse response, String name) {
        String header = response.getHeader("Set-Cookie");
        for (String part : header.split(";")) {
            String[] keyValue = part.trim().split("=", 2);
            if (keyValue.length == 2 && keyValue[0].equals(name)) {
                return keyValue[1];
            }
        }
        throw new IllegalStateException("Cookie " + name + " not found in " + header);
    }
}

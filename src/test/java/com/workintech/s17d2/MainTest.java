package com.workintech.s17d2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workintech.s17d2.model.*;
import com.workintech.s17d2.rest.DeveloperController;
import com.workintech.s17d2.tax.Taxable;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeveloperController.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(ResultAnalyzer.class)
class MainTest {

    /* -------------------- Core Spring Test Beans -------------------- */

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Environment env;

    @Autowired
    private DeveloperController controller;

    /* -------------------- Mocked Dependency -------------------- */

    @MockBean
    private Taxable mockTaxable;

    /* -------------------- Pure Unit Tests (Model / Enum) -------------------- */

    @Test
    @DisplayName("Test Developer Creation")
    void testDeveloperCreation() {
        Developer developer = new Developer(1, "John Doe", 1000.0, Experience.JUNIOR);

        assertEquals(1, developer.getId());
        assertEquals("John Doe", developer.getName());
        assertEquals(1000.0, developer.getSalary());
        assertEquals(Experience.JUNIOR, developer.getExperience());
    }

    @Test
    @DisplayName("Test Experience Enum Values")
    void testEnumValuesExist() {
        assertNotNull(Experience.valueOf("JUNIOR"));
        assertNotNull(Experience.valueOf("MID"));
        assertNotNull(Experience.valueOf("SENIOR"));
    }

    @Test
    @DisplayName("Test JuniorDeveloper Inheritance")
    void testJuniorDeveloperInheritance() {
        Developer dev = new JuniorDeveloper(1, "Test", 1000.0);
        assertTrue(dev instanceof Developer);
        assertEquals(Experience.JUNIOR, dev.getExperience());
    }

    @Test
    @DisplayName("Test MidDeveloper Inheritance")
    void testMidDeveloperInheritance() {
        Developer dev = new MidDeveloper(1, "Test", 1000.0);
        assertTrue(dev instanceof Developer);
        assertEquals(Experience.MID, dev.getExperience());
    }

    @Test
    @DisplayName("Test SeniorDeveloper Inheritance")
    void testSeniorDeveloperInheritance() {
        Developer dev = new SeniorDeveloper(1, "Test", 1000.0);
        assertTrue(dev instanceof Developer);
        assertEquals(Experience.SENIOR, dev.getExperience());
    }

    /* -------------------- Controller Tests -------------------- */

    @BeforeEach
    void setup() throws Exception {

        // Mock Taxable behaviour (CRITICAL)
        when(mockTaxable.getSimpleTaxRate()).thenReturn(15d);
        when(mockTaxable.getMiddleTaxRate()).thenReturn(25d);
        when(mockTaxable.getUpperTaxRate()).thenReturn(35d);

        Developer initial =
                new Developer(1, "Initial Developer", 5000.0, Experience.JUNIOR);

        mockMvc.perform(post("/developers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initial)))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(1)
    @DisplayName("Developer map initialized")
    void developersMapShouldNotBeNull() {
        assertNotNull(controller.developers);
    }

    @Test
    @Order(2)
    @DisplayName("Add Developer")
    void testAddDeveloper() throws Exception {
        Developer dev =
                new Developer(2, "New Developer", 6000.0, Experience.MID);

        mockMvc.perform(post("/developers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dev)))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(3)
    @DisplayName("Get All Developers")
    void testGetAllDevelopers() throws Exception {
        mockMvc.perform(get("/developers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    @Order(4)
    @DisplayName("Get Developer By Id")
    void testGetDeveloperById() throws Exception {
        mockMvc.perform(get("/developers/{id}", 1))
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    @DisplayName("Update Developer")
    void testUpdateDeveloper() throws Exception {
        Developer updated =
                new Developer(1, "Updated", 7000.0, Experience.SENIOR);

        mockMvc.perform(put("/developers/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(6)
    @DisplayName("Delete Developer")
    void testDeleteDeveloper() throws Exception {
        mockMvc.perform(delete("/developers/{id}", 1))
                .andExpect(status().isOk());
    }

    /* -------------------- Application Properties Test -------------------- */

    @Test
    @DisplayName("application.properties check")
    void applicationPropertiesCheck() {

        assertThat(env.getProperty("server.port")).isEqualTo("8585");
        assertThat(env.getProperty("server.servlet.context-path")).isEqualTo("/workintech");

        assertThat(env.getProperty("management.info.env.enabled")).isEqualTo("true");
        assertThat(env.getProperty("management.endpoints.web.exposure.include"))
                .isEqualTo("info,health,mappings");

        assertNotNull(env.getProperty("info.app.name"));
        assertNotNull(env.getProperty("info.app.description"));
        assertNotNull(env.getProperty("info.app.version"));
    }
}
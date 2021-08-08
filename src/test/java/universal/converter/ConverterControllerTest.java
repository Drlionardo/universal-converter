package universal.converter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import universal.converter.controller.ConverterController;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(args = {"src/test/resources/INPUT.scv"})
class ConverterControllerTest {
    @Autowired
    private ConverterController converterController;

    @Test
    public void contextLoads() {
        assertNotNull(converterController);
    }

    @Test
    public void postTest(@Autowired MockMvc mvc) throws Exception {
        String requestJson = "{\n" +
                " \"from\": \"м / с\",\n" +
                " \"to\":  \"км / час\"\n" +
                "}";
        mvc.perform(post("/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("1 м / с = 3.6 км / час"));
    }

    @DisplayName("Return 404 on unknown unit")
    @Test
    public void unknownUnitExceptionTest(@Autowired MockMvc mvc) throws Exception {
        String requestJson = "{\n" +
                " \"from\": \"UnknownUnit1\",\n" +
                " \"to\":  \"UnknownUnit2\"\n" +
                "}";
        mvc.perform(post("/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isNotFound());
    }

    @DisplayName("Return 400 if unable to convert")
    @Test
    public void unableToConvertExceptionTest(@Autowired MockMvc mvc) throws Exception {
        String requestJson = "{\n" +
                " \"from\": \"час\",\n" +
                " \"to\":  \"км\"\n" +
                "}";
        mvc.perform(post("/convert")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
    }
}
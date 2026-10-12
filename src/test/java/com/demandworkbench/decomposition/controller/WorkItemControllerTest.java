package com.demandworkbench.decomposition.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.demandworkbench.decomposition.application.WorkItemCommandService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WorkItemController.class)
class WorkItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkItemCommandService service;

    @Test
    void shouldDeleteSelectedWorkItems() throws Exception {
        var workItemNos = List.of("WORK-1", "SUB-1");
        when(service.delete(workItemNos)).thenReturn(2);

        mockMvc.perform(post("/api/v1/work-items/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemNos":["WORK-1","SUB-1"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.msg.deletedCount").value(2));

        verify(service).delete(workItemNos);
    }

    @Test
    void shouldRejectEmptySelection() throws Exception {
        mockMvc.perform(post("/api/v1/work-items/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemNos":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}

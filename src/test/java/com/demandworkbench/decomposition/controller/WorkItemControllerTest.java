package com.demandworkbench.decomposition.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.demandworkbench.decomposition.application.WorkItemCommandService;
import com.demandworkbench.decomposition.application.WorkItemData;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WorkItemController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkItemCommandService service;

    @Test
    void shouldDeleteSelectedWorkItems() throws Exception {
        var workItemNos = List.of("WORK-1", "SUB-1");
        when(service.delete(1L, workItemNos)).thenReturn(2);

        mockMvc.perform(post("/api/v1/work-items/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemNos":["WORK-1","SUB-1"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.msg.deletedCount").value(2));

        verify(service).delete(1L, workItemNos);
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

    @Test
    void shouldSaveWorkItem() throws Exception {
        var saved = new WorkItemData("DEC-1-MANUAL-1", "DEC-1", "WORK-1", "subtask", "新增子项", "说明",
                "未开始", "前端开发", "high", LocalDate.parse("2026-10-13"), null, null);
        when(service.save(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any(WorkItemData.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/work-items/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workItemNo":null,"decompositionNo":"DEC-1","parentWorkItemNo":"WORK-1",
                                 "type":"subtask","name":"新增子项","description":"说明","status":"未开始",
                                 "role":"前端开发","priority":"高","startDate":"2026-10-13","endDate":null,
                                 "dependency":null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.msg.id").value("DEC-1-MANUAL-1"))
                .andExpect(jsonPath("$.msg.name").value("新增子项"));
    }
}

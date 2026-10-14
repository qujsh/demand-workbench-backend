package com.demandworkbench.requirement.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.demandworkbench.requirement.application.RequirementData;
import com.demandworkbench.requirement.application.PublishSummaryData;
import com.demandworkbench.requirement.application.PublishSummaryService;
import com.demandworkbench.requirement.application.PublishTaskService;
import com.demandworkbench.requirement.application.RequirementService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RequirementController.class)
@AutoConfigureMockMvc(addFilters = false)
class RequirementControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RequirementService service;

    @MockitoBean
    private PublishSummaryService publishSummaryService;

    @MockitoBean
    private PublishTaskService publishTaskService;

    @Test
    void shouldCreateRequirementWithoutNumberQueryParameter() throws Exception {
        var now = LocalDateTime.of(2026, 10, 12, 16, 0);
        var saved = new RequirementData(25L, "REQ-KFYTYY-000025", "KFYTYY", "开发一套预约服务系统",
                "用户描述", "AI 描述", "draft", null, now, now, 0, 0, List.of());
        when(service.save(eq(1L), eq(null), any())).thenReturn(saved);

        mockMvc.perform(post("/api/v1/requirements/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "开发一套预约服务系统",
                                  "userDescription": "用户描述",
                                  "aiDescription": "AI 描述",
                                  "stage": "draft",
                                  "attachments": []
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.msg.requirementNo").value("REQ-KFYTYY-000025"));
    }

    @Test
    void shouldReturnRequirementValidationErrorForInvalidBody() throws Exception {
        mockMvc.perform(post("/api/v1/requirements/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"", "stage":"draft", "attachments":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("S048102000"));
    }

    @Test
    void shouldDeleteRequirementUsingQueryParameter() throws Exception {
        mockMvc.perform(post("/api/v1/requirements/delete")
                        .queryParam("requirementNo", "REQ-KFYTYY-000025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(service).delete(1L, "REQ-KFYTYY-000025");
    }

    @Test
    void shouldReturnSavedPublishSummary() throws Exception {
        when(publishSummaryService.find(1L, "REQ-KFYTYY-000025")).thenReturn(
                new PublishSummaryData("REQ-KFYTYY-000025", "success", "精简任务简介",
                        "publish-summary-v1", LocalDateTime.of(2026, 10, 13, 10, 30)));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/v1/requirements/publish-summary")
                        .queryParam("requirementNo", "REQ-KFYTYY-000025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg.status").value("success"))
                .andExpect(jsonPath("$.msg.content").value("精简任务简介"));
    }
}

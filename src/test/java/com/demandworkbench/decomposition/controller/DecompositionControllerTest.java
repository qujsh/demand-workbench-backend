package com.demandworkbench.decomposition.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.demandworkbench.decomposition.application.DecompositionCommandService;
import com.demandworkbench.decomposition.application.DecompositionErrorCode;
import com.demandworkbench.decomposition.application.DecompositionException;
import com.demandworkbench.decomposition.application.DecompositionQueryService;
import com.demandworkbench.decomposition.application.SubmitResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DecompositionController.class)
@AutoConfigureMockMvc(addFilters = false)
class DecompositionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DecompositionCommandService commandService;

    @MockitoBean
    private DecompositionQueryService queryService;

    @Test
    void shouldWrapSuccessfulSubmission() throws Exception {
        when(commandService.submit(1L, "新增登录功能"))
                .thenReturn(new SubmitResult("DEC-1", "queued", "拆解任务已提交"));

        mockMvc.perform(post("/api/v1/requirements/decompose")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"新增登录功能"}
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.msg.taskId").value("DEC-1"))
                .andExpect(jsonPath("$.msg.status").value("queued"))
                .andExpect(jsonPath("$.errorCode").value(""))
                .andExpect(jsonPath("$.errorMsg").value(""));
    }

    @Test
    void shouldWrapValidationFailure() throws Exception {
        mockMvc.perform(post("/api/v1/requirements/decompose")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.msg").isEmpty())
                .andExpect(jsonPath("$.errorCode").value("S048101001"))
                .andExpect(jsonPath("$.errorMsg").value("待拆解内容不能为空"));
    }

    @Test
    void shouldWrapApplicationFailure() throws Exception {
        when(queryService.query(1L, "DEC-MISSING"))
                .thenThrow(new DecompositionException(DecompositionErrorCode.TASK_NOT_FOUND));

        mockMvc.perform(get("/api/v1/requirements/decompose")
                        .queryParam("taskId", "DEC-MISSING"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.msg").isEmpty())
                .andExpect(jsonPath("$.errorCode").value("S048101002"))
                .andExpect(jsonPath("$.errorMsg").value("拆解任务不存在"));
    }
}

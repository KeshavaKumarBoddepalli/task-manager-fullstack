package com.example.backend;

import com.example.backend.task.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class BackendApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void taskLifecycleAndFiltersWork() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title":"Prepare release","description":"Write release notes"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value(TaskStatus.PENDING.name()))
				.andReturn();

		String location = createResult.getResponse().getHeader("Location");
		String id = location.substring(location.lastIndexOf('/') + 1);

		mockMvc.perform(get("/api/tasks")
						.param("status", "PENDING")
						.param("search", "release"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));

		mockMvc.perform(put("/api/tasks/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title":"Prepare production release","description":"Updated notes","status":"IN_PROGRESS"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Prepare production release"))
				.andExpect(jsonPath("$.status").value("IN_PROGRESS"));

		mockMvc.perform(patch("/api/tasks/{id}/complete", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("COMPLETED"));

		mockMvc.perform(delete("/api/tasks/{id}", id))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/tasks/{id}", id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message", containsString("was not found")));
	}

	@Test
	void rejectsBlankTaskTitle() throws Exception {
		mockMvc.perform(post("/api/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title":"   "}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Title is required"));
	}
}


package com.example.todoappmultidb.rest;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.example.todoappmultidb.model.User;
import com.example.todoappmultidb.model.dto.UserDTO;
import com.example.todoappmultidb.service.UserService;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;

import javassist.NotFoundException;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = UserRestController.class)
public class UserRestControllerTest {

	@MockBean
	UserService userService;

	@Autowired
	private MockMvc mvc;

	private ObjectMapper objMapper;

	@Before
	public void setup() {
		objMapper = new ObjectMapper();
		// objMapper.setSerializationInclusion(Include.NON_EMPTY);
		objMapper.registerModule(new JavaTimeModule());
		objMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	}

	@Test
	public void testGetAllUsersEmpty() throws Exception {
		when(userService.getAllUser()).thenThrow(new NotFoundException("Not found any user"));
		this.mvc.perform(get("/api/users").accept(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print())
				.andExpect(status().isNoContent()).andExpect(status().reason("Not found any user"));
	}

	@Test
	public void testGetAllUsers() throws Exception {
		when(userService.getAllUser())
				.thenReturn(asList(new UserDTO(1l, "nome1", "email1"), new UserDTO(2l, "nome2", "email2")));
		this.mvc.perform(get("/api/users").accept(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print())
				.andExpect(status().isOk()).andExpect(jsonPath("$[0].id", is(1)))
				.andExpect(jsonPath("$[0].name", is("nome1"))).andExpect(jsonPath("$[0].email", is("email1")))
				.andExpect(jsonPath("$[1].id", is(2))).andExpect(jsonPath("$[1].name", is("nome2")))
				.andExpect(jsonPath("$[1].email", is("email2")));
	}

	@Test
	public void testGetOneUserByIdWithNoExistingUser() throws Exception {
		when(userService.getUserById(1l)).thenThrow(new NotFoundException("Not found user with id 1"));
		this.mvc.perform(get("/api/users/1").accept(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent())
				.andExpect(status().reason("Not found user with id 1"));
	}

	@Test
	public void testGetOneUserByIdWithExistingUser() throws Exception {
		when(userService.getUserById(anyLong())).thenReturn(new UserDTO(1l, "nome1", "email1"));
		this.mvc.perform(get("/api/users/1").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1))).andExpect(jsonPath("$.name", is("nome1")))
				.andExpect(jsonPath("$.email", is("email1")));
	}

	@Test
	public void testPostInsertNewUser() throws Exception {
		when(userService.insertNewUser(new UserDTO(null, "nome1", "email1")))
				.thenReturn(new UserDTO(1l, "nome1", "email1"));
		this.mvc.perform(
				post("/api/users/new").content(objMapper.writeValueAsString(new UserDTO(null, "nome1", "email1")))
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.name", is("nome1"))).andExpect(jsonPath("$.email", is("email1")));
	}

	@Test
	public void testPostInsertNewUserNull() throws Exception {

		when(userService.insertNewUser(new UserDTO(null, null, null)))
				.thenThrow(new IllegalArgumentException("User with null property"));
		this.mvc.perform(post("/api/users/new").content(objMapper.writeValueAsString(new UserDTO(null, null, null)))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andDo(MockMvcResultHandlers.print()).andExpect(status().isConflict())
				.andExpect(status().reason("User with null property"));
	}

	@Test
	public void testPutUpdateUser() throws Exception {
		when(userService.updateUser(1, new UserDTO(null, "nome1", "email1")))
				.thenReturn(new UserDTO(1l, "nome1", "email1"));
		this.mvc.perform(
				put("/api/users/update/1").content(objMapper.writeValueAsString(new UserDTO(null, "nome1", "email1")))
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.name", is("nome1"))).andExpect(jsonPath("$.email", is("email1")));
	}

	@Test
	public void testPutUpdateUserNullProperties() throws Exception {

		when(userService.updateUser(1, new UserDTO(null, null, null)))
				.thenThrow(new IllegalArgumentException("User with null property"));
		this.mvc.perform(put("/api/users/update/1").content(objMapper.writeValueAsString(new UserDTO(null, null, null)))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andDo(MockMvcResultHandlers.print()).andExpect(status().isConflict())
				.andExpect(status().reason("User with null property"));
	}

	@Test
	public void testPutUpdateUserNotExistingUser() throws Exception {

		when(userService.updateUser(1, new UserDTO(null, "nome1", "email1")))
				.thenThrow(new NotFoundException("Try to update not existing user"));
		this.mvc.perform(
				put("/api/users/update/1").content(objMapper.writeValueAsString(new UserDTO(null, "nome1", "email1")))
						.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andDo(MockMvcResultHandlers.print()).andExpect(status().isNotFound())
				.andExpect(status().reason("Try to update not existing user"));
	}
}

package com.slack.api.bot.slackinhousebot.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.slack.api.Slack;
import com.slack.api.SlackConfig;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.users.UsersInfoRequest;
import com.slack.api.methods.response.api.ApiTestResponse;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.users.UsersInfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/v1/slack")
@CrossOrigin("**")
public class SlackController {

	@Value( "${secret.token.bot-oauth}" )
	private String botToken;

	@Value( "${secret.token.user-oauth}" )
	private String userToken;

	private Slack slack;

	public SlackController() {
		SlackConfig config = new SlackConfig();
		config.setPrettyResponseLoggingEnabled(true);
		this.slack = Slack.getInstance(config);
	}

	@GetMapping("/test")
	public String getResponse() throws SlackApiException, IOException {
		Slack slack = Slack.getInstance();
		ApiTestResponse response = slack.methods().apiTest(r -> r.foo("bar"));
		return response.toString();
	}

	@GetMapping("/msg/{text}")
	public ResponseEntity<?> postNewMessage(@PathVariable String text) {
		return postAMessage(text);
	}


	@PostMapping(value = "/events", consumes = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> handleEvent(HttpEntity<String> payload) {
		String json = payload.getBody();
		JsonObject jsonObject = new Gson().fromJson(json, JsonObject.class);
		System.out.println(jsonObject.toString());

		JsonObject event = new Gson().fromJson(jsonObject.get("event"), JsonObject.class);
		JsonElement eventType = event.get("type");
		if (eventType.toString().replaceAll("\"", "").equals("message"))
			return handleMessageEvent(event);
		if (eventType.toString().replaceAll("\"", "").equals("reaction_added"))
			return handleReactionAddedEvent(event);
		else return new ResponseEntity<>(HttpStatus.OK);
	}

	private ResponseEntity<?> handleReactionAddedEvent(JsonObject event) {
		String reaction = event.get("reaction").toString().replaceAll("\"", "");
		String user = event.get("user").toString().replaceAll("\"", "");

		MethodsClient methods = slack.methods(userToken);

		UsersInfoRequest request = UsersInfoRequest.builder()
				.user(user)
				.build();
		try {
			UsersInfoResponse response = methods.usersInfo(request);

			if (response.isOk()) {
				String tagUser = "@" + response.getUser().getName();
				return postAMessage(tagUser + " reacted with :"+reaction+": to a message!");
			}
			else return new ResponseEntity<>( response.getError(), HttpStatus.BAD_REQUEST );

		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage() ,HttpStatus.BAD_REQUEST);
		}

	}

	private ResponseEntity<?> handleMessageEvent(JsonObject event) {
		JsonElement text = event.get("text");
		String response = "Hello, you've written: \n " + text.toString();

		boolean messageSentByBot = event.get("client_msg_id")==null;
		if (!messageSentByBot)
			postAMessage(response);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	private ResponseEntity<?> postAMessage(String text) {
		MethodsClient methods = slack.methods(botToken);

		ChatPostMessageRequest request = ChatPostMessageRequest.builder()
				.channel("#random")
				.text(text)
				.linkNames(true)
				.build();
		try {
			ChatPostMessageResponse response = methods.chatPostMessage(request);

			if (response.isOk()) {
				return new ResponseEntity<>( HttpStatus.OK );
			}
			else return new ResponseEntity<>( response.getError(), HttpStatus.BAD_REQUEST );

		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage() ,HttpStatus.BAD_REQUEST);
		}
	}
}
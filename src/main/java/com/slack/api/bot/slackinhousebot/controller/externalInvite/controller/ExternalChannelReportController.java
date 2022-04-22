package com.slack.api.bot.slackinhousebot.controller.externalInvite.controller;

import com.slack.api.bot.slackinhousebot.controller.externalInvite.service.ExternalChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/report")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ExternalChannelReportController {

	private final ExternalChannelService externalChannelService;

	@PostMapping("/external-channel")
	public String handleReportTask(HttpServletRequest request) {
		return externalChannelService.handleExternalChannelReport(request);
	}
}

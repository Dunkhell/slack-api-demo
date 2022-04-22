package com.slack.api.bot.slackinhousebot.controller.externalInvite.service;

import com.slack.api.Slack;
import com.slack.api.SlackConfig;
import com.slack.api.bot.slackinhousebot.controller.externalInvite.model.ExternalChannelReport;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.conversations.ConversationsListConnectInvitesRequest;
import com.slack.api.methods.response.conversations.ConversationsListConnectInvitesResponse;
import com.slack.api.model.connect.ConnectInvite;
import com.slack.api.model.connect.ConnectInviteAcceptance;
import com.slack.api.model.connect.ConnectInviteDetail;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ExternalChannelService {

	@Value( "${secret.token.bot-oauth}" )
	private String botToken;

	@Value( "${secret.token.user-oauth}" )
	private String userToken;

	private final Slack slack;

	public ExternalChannelService() {
		SlackConfig config = new SlackConfig();
		config.setPrettyResponseLoggingEnabled(true);
		this.slack = Slack.getInstance(config);
	}

	@SneakyThrows
	public static String getBody(HttpServletRequest request) {
		return new String(IOUtils.toByteArray(request.getInputStream()));
	}

	public String handleExternalChannelReport(HttpServletRequest request) {
		MethodsClient methods = slack.methods(botToken);

		String workspaceId = getWorkspaceIdFromRequest(request);

		ConversationsListConnectInvitesRequest slackCall =  ConversationsListConnectInvitesRequest.builder()
				.teamId(workspaceId)
				.build();

		ConversationsListConnectInvitesResponse slackResponse;
		try {
			slackResponse = methods.conversationsListConnectInvites(slackCall);
		} catch (Exception e) {
			return "Error fetching invites";
		}

		List<ExternalChannelReport> reports = buildReportEntities(slackResponse.getInvites());

		StringBuilder result = new StringBuilder();
		for (ExternalChannelReport given : reports) {
			result.append(given.toString());
			result.append("\n\n");
		}
		return result.toString();
	}

	private String getWorkspaceIdFromRequest(HttpServletRequest request) {
		String body = getBody(request);
		String workspaceIdSubstring = body.substring(body.indexOf("team_id="));
		String workspaceId = workspaceIdSubstring.substring(workspaceIdSubstring.indexOf("=")+1, workspaceIdSubstring.indexOf("&"));
		workspaceId = "T02DYFZ4MRS"; // Temporary workspaceId for development TODO: REMOVE THIS LINE
		return workspaceId;
	}

	private List<ExternalChannelReport> buildReportEntities(List<ConnectInvite> invites) {
		List<ExternalChannelReport> reports = new ArrayList<>();
		ExternalChannelReport report;
		for (ConnectInvite invite : invites) {
			ConnectInviteDetail invitationData = invite.getInvite();

			report = ExternalChannelReport.builder()
					.direction(invite.getDirection())
					.channelId(invite.getChannel().getId())
					.inviteDateCreate(new Date((long) invitationData.getDateCreated()*1000).toString())
					.inviteId(invitationData.getId())
					.invitingUserEmail(invitationData.getInvitingUser().getProfile().getEmail())
					.invitingUserId(invitationData.getInvitingUser().getId())
					.recipientEmail(invitationData.getRecipientEmail())
					.recipientId(invitationData.getRecipientUserId())
					.isPrivate(invite.getChannel().getIsPrivate()).build();

			if(invite.getAcceptances() != null) {
				ConnectInviteAcceptance connectInviteAcceptance = invite.getAcceptances().get(0);

				ExternalChannelReport.AcceptanceReport acceptanceReport = ExternalChannelReport.AcceptanceReport.builder()
						.acceptanceApprovalStatus(connectInviteAcceptance.getApprovalStatus())
						.acceptanceAcceptingUserEmail(connectInviteAcceptance.getAcceptingUser().getProfile().getEmail())
						.acceptanceAcceptingUserId(connectInviteAcceptance.getAcceptingUser().getId())
						.acceptanceDate(new Date((long)connectInviteAcceptance.getDateAccepted()*1000).toString())
						.build();
				report.setAcceptanceReport(acceptanceReport);
			} else {
				report.setAcceptanceReport(null);
			}
			reports.add(report);
		}
		return reports;
	}

}

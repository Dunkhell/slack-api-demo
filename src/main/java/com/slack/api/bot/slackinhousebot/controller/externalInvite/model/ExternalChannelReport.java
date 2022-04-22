package com.slack.api.bot.slackinhousebot.controller.externalInvite.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalChannelReport {

	private String direction;
	private String inviteId;
	private String inviteDateCreate;
	private String invitingUserId;
	private String invitingUserEmail;
	private String recipientEmail;
	private String recipientId;
	private String channelId;
	private boolean isPrivate;

	private AcceptanceReport acceptanceReport;

	@Override
	public String toString() {
		String result = "ExternalChannelReport {" +
				" \ndirection='" + direction + '\'' +
				", \ninviteId='" + inviteId + '\'' +
				", \ninviteDateCreate='" + inviteDateCreate + '\'' +
				", \ninvitingUserId='" + invitingUserId + '\'' +
				", \ninvitingUserEmail='" + invitingUserEmail + '\'' +
				", \nrecipientEmail='" + recipientEmail + '\'' +
				", \nrecipientId='" + recipientId + '\'' +
				", \nchannelId='" + channelId + '\'' +
				", \nisPrivate=" + isPrivate;
		if(acceptanceReport != null) {
			result += ", \nacceptanceReport=" + acceptanceReport +
					'}';
		}
		else {
			result += ", \nacceptanceReport= Not_Resolved" +
					'}';
		}
		return result;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AcceptanceReport {
		private String acceptanceApprovalStatus;
		private String acceptanceAcceptingUserId;
		private String acceptanceAcceptingUserEmail;
		private String acceptanceDate;

		@Override
		public String toString() {
			return "AcceptanceReport {" +
					"\nacceptanceApprovalStatus='" + acceptanceApprovalStatus + '\'' +
					", \nacceptanceAcceptingUserId='" + acceptanceAcceptingUserId + '\'' +
					", \nacceptanceAcceptingUserEmail='" + acceptanceAcceptingUserEmail + '\'' +
					", \nacceptanceDate='" + acceptanceDate + '\'' +
					'}';
		}
	}
}

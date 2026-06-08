package org.cmda.management.dtos.member;

public record MemberArchiveRequest(
        Long archiveReasonId,
        String archiveComment
) {
}

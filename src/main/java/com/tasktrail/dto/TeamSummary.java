package com.tasktrail.dto;

/** The four numbers at the top of the employer dashboard. */
public record TeamSummary(long total, long done, long pending, long carriedOpen) {
}

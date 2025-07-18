package io.f1.backend.domain.stat.dto;

import java.util.List;

public record StatPageResponse(
	int totalPages,
	int currentPage,
	int totalElements,
	List<StatResponse> ranks
) { }

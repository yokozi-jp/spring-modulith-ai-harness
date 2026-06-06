package com.example.demo.category.application.query.dto;

import org.jmolecules.architecture.cqrs.QueryModel;

/** Category 一覧用クエリモデル。 */
@QueryModel
public record CategorySummaryDto(String id, String name, int sortOrder) {}

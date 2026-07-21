package com.example.demo.catalog.application.query.dto;

import org.jmolecules.architecture.cqrs.QueryModel;

/** Product 一覧用クエリモデル。 */
@QueryModel
public record ProductSummaryDto(String id, String name, String status, String categoryId) {}

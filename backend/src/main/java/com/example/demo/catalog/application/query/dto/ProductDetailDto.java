package com.example.demo.catalog.application.query.dto;

import org.jmolecules.architecture.cqrs.QueryModel;

/** Product 詳細クエリモデル。 */
@QueryModel
public record ProductDetailDto(String id) {}

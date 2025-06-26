package br.com.king.flick_business.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Getter;

@Getter
public class PageResponse<T> {
  private final List<T> content;
  private final int number;
  private final int size;
  private final long totalElements;

  private final int totalPages;
  private final boolean first;
  private final boolean last;

  public PageResponse(Page<T> page) {
    this.content = page.getContent();
    this.number = page.getNumber();
    this.size = page.getSize();
    this.totalElements = page.getTotalElements();
    this.totalPages = page.getTotalPages();
    this.first = page.isFirst();
    this.last = page.isLast();
  }
}
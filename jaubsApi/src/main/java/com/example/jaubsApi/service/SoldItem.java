package com.example.jaubsApi.service;

import java.time.LocalDate;

public record SoldItem(BookItem item, String buyer, LocalDate date) {
}
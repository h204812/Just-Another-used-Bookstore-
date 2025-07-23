package org.jaubs.clients;


import org.jaubs.service.BookItem;
import org.jaubs.service.SoldItem;
import org.jaubs.configuration.feignclientConfiuguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "jaubs-api-client", url = "http://localhost:8081",configuration = feignclientConfiuguration.class)
public interface JaubsApiClient {

    @GetMapping("/jaubsapi/show-items")
    List<BookItem> showBookItems();

    @GetMapping("/jaubsapi/admin/show-update-form")
    BookItem getItem(@RequestParam("itemId") Long itemId);

    @PostMapping("/jaubsapi/save-item")
    void saveBookItem(@ModelAttribute BookItem item); // May require @RequestBody depending on your usage

    @PostMapping("/jaubsapi/delete-item")
    void deleteBookItem(@RequestParam("id") Long id);

    @PostMapping("/jaubsapi/buy-item")
    void buyBookItem(@RequestParam("id") Long id);

    @GetMapping("/jaubsapi/show-bought-items")
    List<SoldItem> showBoughtItems(@RequestParam("email") String email);
}

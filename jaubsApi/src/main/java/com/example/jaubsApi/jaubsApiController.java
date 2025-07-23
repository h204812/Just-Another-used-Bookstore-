package com.example.jaubsApi;

import com.example.jaubsApi.service.BookItem;
import com.example.jaubsApi.service.JaubsBookService;
import com.example.jaubsApi.service.SoldItem;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Book;
import java.util.List;

@RestController
public class jaubsApiController {
    private final JaubsBookService jaubsbookService;

    public jaubsApiController(JaubsBookService jaubsbookService) {
        this.jaubsbookService = jaubsbookService;
    }


    @GetMapping("/jaubsapi/show-items")
    public List<BookItem> showBookItems(){
        return jaubsbookService.findAllOpenItems();
    }

    @GetMapping("/jaubsapi/admin/show-update-form")
    public BookItem getItem(@RequestParam @NotNull Long itemId){
        return jaubsbookService.getItem(itemId);
    }

    @PostMapping("/jaubsapi/save-item")
    public void saveBookItem(@ModelAttribute BookItem item){
        if(item.getId()==null){
            jaubsbookService.createBookItem(item);
        }
        else jaubsbookService.updateBookItem(item);
    }

    @PostMapping("/jaubsapi/delete-item")
    public void deleteBookItem(@RequestParam @NotNull Long id)
    {
        jaubsbookService.deleteItem(id);
    }

    @PostMapping("/jaubsapi/buy-item")
    public void buyBookItem(@RequestParam @NotNull Long id){
        jaubsbookService.buyItem(id);
    }

    @GetMapping("/jaubsapi/show-bought-items")
    public List<SoldItem> showBoughtItems(String email){
        return jaubsbookService.findSoldItems(email);
    }



}

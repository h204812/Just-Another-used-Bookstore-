package com.example.jaubsApi.service;

import jakarta.annotation.PostConstruct;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class JaubsBookService {
    private final AtomicLong idGenerator = new AtomicLong();

    private final List<BookItem> items = Collections.synchronizedList(new ArrayList<>());

    private final List<SoldItem> soldItems =  Collections.synchronizedList(new ArrayList<>());

    @PostConstruct
    private void initializeBookItems() {

        long id = idGenerator.getAndIncrement();
        items.add(new BookItem(id,"Dead Man's Folly", "Agatha Christie", 3.99,
                BookItem.BookCondition.FAIR, "Few pencil marks on several pages but other than it - it is great!"
                , "admindoe", false));

        id = idGenerator.getAndIncrement();
        items.add(new BookItem(id,"Nemesis", "Agatha Christie", 4.99,
                BookItem.BookCondition.ASNEW, "Perfect Condition. Almost new."
                , "admindoe", false));

        id = idGenerator.getAndIncrement();
        items.add(new BookItem(id,"C++ Programming Language", "Bjarne Stroustrup", 14.99,
                BookItem.BookCondition.FAIR, "In Good Condition"
                , "admindoe", false));


    }


    public void createBookItem(BookItem item) {

        // extract the user of the Application
        Authentication token
                =  SecurityContextHolder.getContext().getAuthentication();
        Jwt principal = (Jwt) token.getPrincipal();

        // create a new Book item
        long id = idGenerator.getAndIncrement();
        item.setId(id);
        item.setSold(false);
        item.setCreator(principal.getClaimAsString("Email"));
        items.add(item);
    }

    public void updateBookItem(BookItem item) {

        items.stream()
                .filter(e -> e.getId() == item.getId())
                .findFirst()
                .ifPresent(e -> e.copyFrom(item));

    }

    public BookItem getItem(long id) {

        return items.stream()
                .filter(e -> e.getId() == id)
                .findFirst()
                .orElseThrow();

    }

    public void deleteItem(long id) {
        items.removeIf(e -> e.getId() == id);
    }

    public void buyItem(long id) {

        // extract the user of the Application
        Authentication token
                = SecurityContextHolder.getContext().getAuthentication();
        Jwt principal = (Jwt) token.getPrincipal();

        items.stream()
                .filter(e -> e.getId() == id)
                .findFirst()
                .ifPresent(e -> {
                    e.setSold(true);
                    String email = principal.getClaimAsString("email");
                    LocalDate now = LocalDate.now();
                    soldItems.add(new SoldItem(e, email, now));
                    System.out.println("** Added to solditems list " + email);
                });

    }

    public List<BookItem> findAllOpenItems() {
        return this.items.stream()
                .filter(item -> !item.getSold())
                .toList();
    }

    public List<SoldItem> findSoldItems(String user) {
        return this.soldItems.stream()
                .filter(e -> user.equalsIgnoreCase(e.buyer()))
                .toList();

    }
}

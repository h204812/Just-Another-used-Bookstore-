package org.jaubs;

import jakarta.validation.constraints.NotNull;
import org.jaubs.clients.JaubsApiClient;
import org.jaubs.service.BookItem;
import org.jaubs.service.GitLabService;
import org.jaubs.service.JaubsBookService;
import org.jaubs.service.SoldItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class JaubsUIController {

//    @Autowired
//    private JaubsBookService bookService;

    @Autowired
    private JaubsApiClient client;

    @Autowired
    private GitLabService gitLabService;

    @GetMapping("/")
    public String slash() {
        return "redirect:/jaubs/ui";
    }

    @GetMapping("/jaubs/ui")
    public ModelAndView home(
            OAuth2AuthenticationToken token,
            @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient client) {

        OidcUser principal = (OidcUser)token.getPrincipal();

        ModelAndView model = generateDefaultModel(token);

        // Add accesstoken, refreshtoken and idtoken
        OAuth2AccessToken accessToken = client.getAccessToken();
        OAuth2RefreshToken refreshToken = client.getRefreshToken();
        OidcIdToken idToken = principal.getIdToken();

        model.setViewName("home");
        model.addObject("accesstoken",
                JaubsUtils.prettyBody(tokenValue(accessToken)));
        model.addObject("refreshtoken",
                JaubsUtils.prettyBody(tokenValue(refreshToken)));
        model.addObject("idtoken",
                JaubsUtils.prettyBody(tokenValue(idToken)));
        return model;
    }

    @GetMapping("/jaubs/ui/show-items")
    public ModelAndView showBookItems(OAuth2AuthenticationToken token) {

        List<BookItem> openItems = client.showBookItems();

        ModelAndView model = generateDefaultModel(token);
        model.setViewName("items");
        model.addObject("items", openItems);
        return model;
    }

    @GetMapping("/jaubs/ui/admin/show-create-form")
    public ModelAndView showCreateBookForm(OAuth2AuthenticationToken token) {

        BookItem defaultItem = BookItem.emptyItem();

        ModelAndView model = generateDefaultModel(token);
        model.setViewName("book-item-form");
        model.addObject("item", defaultItem);
        return model;
    }

    @GetMapping("/jaubs/ui/admin/show-update-form")
    public ModelAndView showUpdateBookForm(OAuth2AuthenticationToken token,
                                           @RequestParam @NotNull Long itemId) {

        BookItem item = client.getItem(itemId);

        ModelAndView model = generateDefaultModel(token);
        model.setViewName("book-item-form");
        model.addObject("item", item);
        return model;
    }

    @PostMapping("/jaubs/ui/admin/save-item")
    public String saveBookItem(@ModelAttribute BookItem item) {
        client.saveBookItem(item);
        return "redirect:/jaubs/ui/show-items";
    }

    @GetMapping("/jaubs/ui/admin/delete-item-conf")
    public ModelAndView deleteBookItemConf(OAuth2AuthenticationToken token,
                                     @RequestParam @NotNull Long itemId) {

        BookItem item = client.getItem(itemId);

        ModelAndView model = generateDefaultModel(token);
        model.setViewName("delete-item-conf");
        model.addObject("item", item);
        return model;
    }

    @PostMapping("/jaubs/ui/admin/delete-item")
    public String deleteBookItem(@RequestParam @NotNull Long id) {

        client.deleteBookItem(id);

        return "redirect:/jaubs/ui/show-items";
    }

    @GetMapping("/jaubs/ui/buy-item-conf")
    public ModelAndView buyBookItemConf(OAuth2AuthenticationToken token,
                                        @RequestParam @NotNull Long itemId) {

        BookItem item = client.getItem(itemId);

        ModelAndView model = generateDefaultModel(token);
        model.setViewName("buy-item-conf");
        model.addObject("item", item);
        return model;
    }

    @PostMapping("/jaubs/ui/buy-item")
    public String buyBookItem(@RequestParam @NotNull Long id) {

       client.buyBookItem(id);

        return "redirect:/jaubs/ui/show-items";
    }

    @GetMapping("/jaubs/ui/show-bought-items")
    public ModelAndView showBoughtItems(OAuth2AuthenticationToken token) {

        OidcUser principal = (OidcUser)token.getPrincipal();

        List<SoldItem> soldItems
                = client.showBoughtItems(principal.getEmail());

        ModelAndView model = generateDefaultModel(token);
        model.setViewName("bought-items");
        model.addObject("soldItems", soldItems);
        return model;
    }
    @GetMapping("/jaubs/gitlab-books")
    public ModelAndView showGitLabProjects(
            OAuth2AuthenticationToken loginToken ,
            @RegisteredOAuth2AuthorizedClient("gitlab-oauth")
            OAuth2AuthorizedClient gitlabClient)
    {
        OAuth2AccessToken gitlabToken = gitlabClient.getAccessToken();
        System.out.println(">>> GitLab Toekn = "+ gitlabToken.getTokenValue());

        List<GitLabService.GitLabProject> projects
                = gitLabService.getProjects(gitlabToken.getTokenValue());

        ModelAndView model = generateDefaultModel(loginToken);
        model.setViewName("gitlab-projects");
        model.addObject("projects",projects);

        return model;
    }

    @GetMapping("/login")
    public String showLoginPage(){
        return "redirect:/oauth2/authorization/keycloak-oidc";
    }



    /*
     * Sets some basic user information. The call can add more properties
     * to it before passing to the view file.
     */
    private ModelAndView generateDefaultModel(OAuth2AuthenticationToken token) {

        OidcUser principal = (OidcUser) token.getPrincipal();

        ModelAndView model = new ModelAndView();
        model.addObject("user", principal);
        return model;
    }

    private String tokenValue(OAuth2Token token) {
        return (token != null) ? token.getTokenValue() : "Not Available";
    }

}

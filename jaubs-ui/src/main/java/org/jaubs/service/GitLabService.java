package org.jaubs.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class GitLabService {

    @Value("https://gitlab.com/api/v4")
    private String GITLAB_API;

    public record  GitLabProject(int itemId,String book,String Author,
                                 float price,String condition,String notes){

    }

    private final RestClient apiClient = RestClient.create();

    public List<GitLabProject> getProjects(String token){

        return  apiClient
                .get()
                .uri(GITLAB_API+"/projects?owned=true")
                .header("Authorization","Bearer "+token)
                .retrieve()
                .body(new ParameterizedTypeReference<List<GitLabProject>>() {
                });
    }
}

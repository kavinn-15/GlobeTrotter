package com.example.loginapp.controller;

import com.example.loginapp.entity.CommunityPost;
import com.example.loginapp.entity.User;
import com.example.loginapp.repository.CommunityPostRepository;
import com.example.loginapp.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Screen 10: Community tab, where logged-in users can browse and share
 * their experience about a trip or activity with the rest of the
 * community. Search / group / filter / sort-by toolbar mirrors the other
 * list screens (Screen 3, 6, 9).
 */
@Controller
public class CommunityController {

    private static final String LOGGED_IN_USER = "loggedInUser";

    private final CommunityPostRepository communityPostRepository;
    private final UserRepository userRepository;

    @Autowired
    public CommunityController(CommunityPostRepository communityPostRepository,
                                UserRepository userRepository) {
        this.communityPostRepository = communityPostRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/community")
    public String communityTab(@RequestParam(required = false) String query,
                                HttpSession session, Model model) {
        if (session.getAttribute(LOGGED_IN_USER) == null) {
            return "redirect:/login";
        }

        List<CommunityPost> posts;
        if (StringUtils.hasText(query)) {
            posts = communityPostRepository
                    .findByTitleContainingIgnoreCaseOrPlaceContainingIgnoreCaseOrderByCreatedAtDesc(query, query);
        } else {
            posts = communityPostRepository.findAllByOrderByCreatedAtDesc();
        }

        model.addAttribute("posts", posts);
        model.addAttribute("query", query);
        return "community";
    }

    @PostMapping("/community")
    public String createPost(@RequestParam String title,
                              @RequestParam(required = false) String content,
                              @RequestParam(required = false) String place,
                              @RequestParam(required = false) String activityType,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        User loggedInUser = (User) session.getAttribute(LOGGED_IN_USER);
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        if (!StringUtils.hasText(title)) {
            redirectAttributes.addFlashAttribute("communityError", "Please give your post a title.");
            return "redirect:/community";
        }

        CommunityPost post = new CommunityPost();
        post.setAuthor(userRepository.getReferenceById(loggedInUser.getId()));
        post.setTitle(title.trim());
        post.setContent(content);
        post.setPlace(place);
        post.setActivityType(activityType);

        communityPostRepository.save(post);

        redirectAttributes.addFlashAttribute("communitySuccess", "Your post was shared with the community.");
        return "redirect:/community";
    }
}

package com.communityfoodsharing.controller;

import com.communityfoodsharing.dto.Dtos.MessageResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
public class PublicController {
    @GetMapping("/health") public MessageResponse health() { return new MessageResponse("Community Food Sharing API is running"); }
}
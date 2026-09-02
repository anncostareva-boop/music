package com.music.app.controller;


import com.music.app.service.interfaces.SongService;
import com.music.app.service.interfaces.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller

public class HistoryController {

UserService userService;
SongService songService;

public HistoryController(UserService userService,  SongService songService) {
    this.userService=userService;
    this.songService=songService;
}

    @PostMapping("/api/history/record")
    @ResponseBody
    public ResponseEntity<Void> recordListeningHistory(@RequestParam("songId") Long songId,
                                                       Authentication auth) {

        System.out.println("HISTORY ENDPOINT REACHED: " + songId);



        if (auth != null && auth.isAuthenticated()) {
                String username = auth.getName();
                Long userId = userService.getUserIdByUsername(username);

                songService.addToHistory(songId, userId);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

    }
}

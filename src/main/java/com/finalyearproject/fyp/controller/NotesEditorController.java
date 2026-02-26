package com.finalyearproject.fyp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NotesEditorController {

    @GetMapping("/notes-editor")
    public String openNotesEditor() {
        return "notesEditor/index";
    }

}

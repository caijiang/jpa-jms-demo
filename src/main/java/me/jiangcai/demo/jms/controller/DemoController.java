package me.jiangcai.demo.jms.controller;

import me.jiangcai.demo.jms.entity.Note;
import me.jiangcai.demo.jms.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;

/**
 * 如何演示 cache 的冲突
 *
 * @author CJ
 */
@Controller
public class DemoController {

    private final NoteRepository noteRepository;

    @Autowired
    public DemoController(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/")
    public String index(Model model) {
        model.addAttribute("notes", noteRepository.findAll());
        return "notes.html";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void change(String text, @PathVariable("id") long id) {
        Note note = noteRepository.getOne(id);
        note.setText(text);
        noteRepository.save(note);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/")
    @ResponseStatus(HttpStatus.OK)
    public void add(String text) {
        Note note = new Note();
        note.setText(text);
        note.setCreateTime(LocalDateTime.now());
        noteRepository.save(note);
    }

}

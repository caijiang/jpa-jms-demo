package me.jiangcai.demo.jms.repository;

import me.jiangcai.demo.jms.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author CJ
 */
public interface NoteRepository extends JpaRepository<Note, Long> {
}

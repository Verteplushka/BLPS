package com.example.BLPS.Service;

import com.example.BLPS.Entities.Tag;
import com.example.BLPS.Repositories.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagService {
    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository){
        this.tagRepository = tagRepository;
    }

    public List<Tag> findAll(){
        return tagRepository.findAll();
    }

    public List<Tag> findAllById(List<Integer> tagIds) {
        return tagRepository.findAllById(tagIds);
    }
}

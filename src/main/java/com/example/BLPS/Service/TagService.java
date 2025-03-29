package com.example.BLPS.Service;

import com.example.BLPS.Entities.Tag;
import com.example.BLPS.Repositories.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    public List<Tag> findAll(){
        return tagRepository.findAll();
    }
}

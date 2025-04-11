package com.example.BLPS.Service;

import com.example.BLPS.Entities.Platform;
import com.example.BLPS.Repositories.PlatformRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlatformService {
    private final PlatformRepository platformRepository;

    @Autowired
    public PlatformService(PlatformRepository platformRepository){
        this.platformRepository = platformRepository;
    }

    public Platform findPlatformByName(String name){
        return platformRepository.findPlatformByName(name);
    }

    public List<Platform> findAllById(List<Integer> platformIds) {
        return platformRepository.findAllById(platformIds);
    }
}

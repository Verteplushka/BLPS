package com.example.BLPS.Service;

import com.example.BLPS.Entities.Platform;
import com.example.BLPS.Repositories.PlatformRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlatformService {
    private final PlatformRepository platformRepository;

    public Platform findPlatformByName(String name){
        return platformRepository.findPlatformByName(name);
    }
}

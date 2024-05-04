package com.company.streamingplatform.kafka.consumer;

import com.company.streamingplatform.aws.AmazonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranscodingConsumer {

    private final AmazonService amazonClient;

    @KafkaListener(topics = "transcode-topic", groupId = "streamingPlatform")
    public void consumeMessage(String message) {
        log.info(String.format("Message received from transcode-topic Topic -> %s", message));
        String[] fileData = message.split(",");
        String fileName = fileData[0];
        Long movieId = Long.valueOf(fileData[1]);
        amazonClient.transcodeFile(fileName, "1351620000001-000001", movieId);    // 1080p
        amazonClient.transcodeFile(fileName, "1351620000001-000010", movieId);    // 720p
        amazonClient.transcodeFile(fileName, "1351620000001-000020", movieId);    // 480p
        amazonClient.transcodeFile(fileName, "1351620000001-000040", movieId);    // 360p
    }
}

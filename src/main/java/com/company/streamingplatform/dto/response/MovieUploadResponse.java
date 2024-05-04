package com.company.streamingplatform.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MovieUploadResponse {
    String message;
    String fileName;
}

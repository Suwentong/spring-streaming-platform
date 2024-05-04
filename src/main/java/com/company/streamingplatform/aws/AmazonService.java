package com.company.streamingplatform.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder;
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClientBuilder;
import com.amazonaws.services.elastictranscoder.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.company.streamingplatform.movie.MovieService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class AmazonService {
    private AmazonS3 s3client;

    private AmazonElasticTranscoder transcoderClient;

    private final MovieService movieService;

    @Value("${amazonProperties.endpointUrl}")
    private String endpointUrl;

    @Value("${amazonProperties.bucketName}")
    private String bucketName;

    @Value("${amazonProperties.s3AccessKey}")
    private String s3AccessKey;

    @Value("${amazonProperties.s3SecretKey}")
    private String s3SecretKey;

    @Value("${amazonProperties.transcoderAccessKey}")
    private String transcoderAccessKey;

    @Value("${amazonProperties.transcoderSecretKey}")
    private String transcoderSecretKey;

    @Value("${amazonProperties.pipelineId}")
    private String pipelineId;

    @Value("${amazonProperties.cloudFrontUrl}")
    private String cloudFrontUrl;

    @Autowired
    @Lazy
    public AmazonService(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostConstruct
    private void initializeAmazon() {
        AWSCredentials s3Credentials = new BasicAWSCredentials(this.s3AccessKey, this.s3SecretKey);
        AWSCredentials transcoderCredentials = new BasicAWSCredentials(this.transcoderAccessKey, this.transcoderSecretKey);
        this.s3client = new AmazonS3Client(s3Credentials);
        this.transcoderClient = AmazonElasticTranscoderClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(transcoderCredentials))
                .withRegion("us-east-1")
                .build();
    }

    public String createTranscodingJob(String inputKey, String outputKeyPrefix, String pipelineId, String presetId) {
        CreateJobRequest createJobRequest = new CreateJobRequest()
                .withPipelineId(pipelineId)
                .withInput(new JobInput().withKey(inputKey))
                .withOutput(new CreateJobOutput()
                        .withKey(outputKeyPrefix)
                        .withPresetId(presetId));

        try {
            CreateJobResult createJobResult = transcoderClient.createJob(createJobRequest);
            return createJobResult.getJob().getId();
        } catch (AmazonElasticTranscoderException e) {
            log.error("Error while creating transcoding job: {}", e.getMessage());
            return null;
        }
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    private String generateFileName(MultipartFile multiPart) {
        return new Date().getTime() + "-" + Objects.requireNonNull(
                multiPart.getOriginalFilename()).replace(" ", "_");
    }

    private void uploadFileTos3bucket(String fileName, File file) {
        s3client.putObject(new PutObjectRequest(bucketName, "input/" + fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));
    }

    public String deleteFileFromS3Bucket(String fileUrl) {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        s3client.deleteObject(new DeleteObjectRequest(bucketName + "/", fileName));
        return "Successfully deleted";
    }

    public String uploadFile(MultipartFile multipartFile) {
        String fileName = "";
        File file = null;
        try {
            file = convertMultiPartToFile(multipartFile);
            fileName = generateFileName(multipartFile);
            uploadFileTos3bucket(fileName, file);
        } catch (Exception e) {
            log.error("Error during file upload to S3: {}", e.getMessage());
            throw new IllegalStateException("Error during file upload to S3: " + e.getMessage());
        } finally {
            if (file != null && file.exists()) {
                boolean result = file.delete();
                if (!result) {
                    log.error("Failed to delete file: {}", fileName);
                }
            }
        }
        return fileName;
    }

    public void transcodeFile(String fileName, String presetId, Long movieId) {
        String format = switch (presetId) {
            case "1351620000001-000001" -> "1080p";
            case "1351620000001-000010" -> "720p";
            case "1351620000001-000020" -> "480p";
            case "1351620000001-000040" -> "360p";
            default -> "original";
        };

        String outputFileName = addFormatToFileName(fileName, format);

        String response = createTranscodingJob(
                    "input/" + fileName,
                    "output/" + outputFileName,
                    pipelineId,
                    presetId);

        log.info("Transcode job finished. File name: {}. Transcode result: {}", outputFileName, response);

        Map<String, String> newMovieUrl = new HashMap<>();
        newMovieUrl.put(format, cloudFrontUrl + "output/" + outputFileName);

        movieService.updateMovieUrl(movieId, newMovieUrl);
    }

    private String addFormatToFileName(String fileName, String resolution) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            return fileName.substring(0, dotIndex) + "_" + resolution + fileName.substring(dotIndex);
        } else {
            return fileName + "_" + resolution;
        }
    }
}

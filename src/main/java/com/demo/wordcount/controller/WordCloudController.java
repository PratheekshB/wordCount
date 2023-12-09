package com.demo.wordcount.controller;

import com.demo.wordcount.service.DocumentService;
import com.demo.wordcount.service.WordCountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/word-cloud")
public class WordCloudController {
    private static final Logger logger = LoggerFactory.getLogger(WordCloudController.class);

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            DocumentService.processDocument(file);
            logger.info("Document uploaded successfully: {}", file.getOriginalFilename());
            return ResponseEntity.ok("Document uploaded successfully");
        } catch (IOException e) {
            logger.error("Error processing the document: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the document");
        }
        catch (MaxUploadSizeExceededException e) {
            logger.error("Error processing the document: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File size exceeded the limit. Max allowed size is 5MB");
        }
    }

    @GetMapping("/word-counts")
    public ResponseEntity<List<Map.Entry<String, Integer>>> getWordCounts() {
        try {
            Map<String, Integer> wordCounts;
            synchronized (WordCountService.class) {
                wordCounts = WordCountService.getWordCounts();
            }
            List<Map.Entry<String, Integer>> sortedWordCounts = wordCounts.entrySet()
                    .stream()
                    .sorted((entry1, entry2) -> {
                        int compare = entry2.getValue().compareTo(entry1.getValue());
                        return compare != 0 ? compare : entry1.getKey().compareTo(entry2.getKey());
                    })
                    .collect(Collectors.toList());
            logger.info("Retrieved word counts successfully");
            return ResponseEntity.ok(sortedWordCounts);
        } catch (Exception e) {
            logger.error("Error retrieving word counts: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/word-near/{word}")
    public ResponseEntity<Map<String, Integer>> getWordNear(@PathVariable String word,
                                                            @RequestParam(defaultValue = "1") int distanceThreshold) {
            try {
                Map<String, Integer> nearWords = WordCountService.getWordNear(word, distanceThreshold);
                if (nearWords != null) {
                    logger.info("Retrieved near words for '{}'", word);
                    return ResponseEntity.ok(nearWords);
                } else {
                    logger.error("Error retrieving near words for '{}'", word);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                }
            } catch (Exception e) {
                logger.error("Error retrieving near words for '{}': {}", word, e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        }
    }

package com.demo.wordcount.service;

import com.demo.wordcount.exception.FileProcessingException;
import com.demo.wordcount.exception.FileSizeLimitExceededException;
import com.demo.wordcount.exception.UnsupportedFileTypeException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger (DocumentService.class);
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    public static void processDocument(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream ( )) {
            String originalFilename = file.getOriginalFilename ( );
            long fileSize = file.getSize ( );

            if (fileSize > MAX_FILE_SIZE) {
                logger.warn ("File size exceeded the limit. Max allowed size is 5 MB.");
                throw new FileSizeLimitExceededException ("File size exceeded the limit. Max allowed size is 5MB.");
            }

            if (originalFilename != null) {
                if (originalFilename.endsWith (".pdf")) {
                    processPdfDocument (inputStream);
                } else {
                    throw new UnsupportedFileTypeException ("Unsupported file type. Only PDF files are supported.");
                }
            } else {
                logger.warn ("Unable to determine document type. Processing as generic document.");
                throw new UnsupportedFileTypeException ("Unable to determine document type. Processing as generic document.");
            }
        } catch (IOException e) {
            logger.error ("Please Select  any PDF size less than 5MB");
            handleIOException (e);
        }
    }

    private static void processPdfDocument(InputStream inputStream) throws IOException {
        try {
            if (inputStream != null && inputStream.available ( ) > 0) {
                PDDocument document = PDDocument.load (inputStream);
                PDFTextStripper textStripper = new PDFTextStripper ( );
                String text = textStripper.getText (document);
                String[] words = text.split ("\\s+");
                for (String word : words) {
                    WordCountService.updateWordCount (word);
                }
                document.close ( );
            } else {
                logger.warn ("Input stream is null or empty. Unable to process the document.");
                throw new FileNotFoundException ("Please pass a valid PDF file.");
            }
        } catch (Exception e) {
            logger.error ("Error processing PDF document", e);
            handleException (e);
        }
    }


    private static void handleIOException(IOException e) throws IOException {
        throw new IOException("Please Select  any PDF size less than 5MB", e);
    }
    private static void handleException(Exception e) {
        logger.error ("Error handling other document types", e);
        throw new FileProcessingException ("Error handling other document types", e);
    }
}

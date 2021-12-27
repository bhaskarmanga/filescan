package com.fission.scan.filescan.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;



@RestController
@RequestMapping(value = "/upload")
public class FileUploadController {
	
	private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);
	
	@Value("${file.upload-dir}")
	String FILE_DIR;
	
	@PostMapping(value = "/files")
	public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
		
		logger.info("uploading file.....");
		boolean fileUploaded = true;
		
		File myfile = new File(FILE_DIR + file.getOriginalFilename());
		FileOutputStream fos = null;
		try {
			logger.info("File name: " + myfile.getName());
			myfile.createNewFile();
			fos = new FileOutputStream(myfile);
			fos.write(file.getBytes());
		}catch(FileNotFoundException fie) {
			fileUploaded = false;
			logger.error("File not found....", fie.getMessage());
			fie.printStackTrace();
		}catch(IOException ioe) {
			fileUploaded = false;
			logger.error("Exception in reading file.....", ioe.getMessage());
			ioe.printStackTrace();
		}finally {
			try {
				logger.info("closing file output stream..");
				if(fos != null)
					fos.close();
			} catch (IOException e) {
				logger.warn("Exception in closing file output stream..", e.getMessage());
				e.printStackTrace();
			}
		}
		if(fileUploaded) {
			logger.info("File upload completed successfully....");
			return new ResponseEntity<>("success",HttpStatus.OK);
		}else {
			logger.info("File upload failed....");
			return new ResponseEntity<>("failed",HttpStatus.BAD_REQUEST);
		}
	}

}

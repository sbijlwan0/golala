package com.easygo.web.rest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.easygo.domain.Document;
import com.easygo.repository.DocumentRepository;
import com.easygo.service.dto.ResultStatus;
import com.easygo.config.ConstApp;

import io.undertow.util.BadRequestException;

@RestController
@RequestMapping("/api")
public class DocumentResource {
	
	private final Logger log = LoggerFactory.getLogger(DocumentResource.class);
	
	@Autowired
	DocumentRepository docRepo;
	
	
	@PutMapping("/document")
	public ResponseEntity<?> updateDocument(@Valid @RequestBody Document doc) throws BadRequestException{
		
		log.debug("rest request to update document.");
		
		if(null==doc.getId())
			throw new BadRequestException("Id Must Not Be null.");
		
		Document result=docRepo.save(doc);
		
		return new ResponseEntity<>(new ResultStatus("Success","Documents Updated",result),HttpStatus.OK);
		
	}
	
	
	@GetMapping("/document")
	public ResponseEntity<?> getAllDocument() {
		
		log.debug("rest request to get All document.");
		
		return new ResponseEntity<>(new ResultStatus("Success","Documents Updated",docRepo.findAll()),HttpStatus.OK);
		
	}
	
	@GetMapping("/document/{id}")
	public ResponseEntity<?> getDocumentById(@PathVariable("id") String id) {
		
		log.debug("rest request to get document by id.",id);
		
		return new ResponseEntity<>(new ResultStatus("Success","Documents Updated",docRepo.findById(id)),HttpStatus.OK);
		
	}
	
	@GetMapping("/documentByUserId/{userId}")
	public ResponseEntity<?> getDocumentByUserId(@PathVariable("userId") String userId) {
		
		log.debug("rest request to get document by user id.",userId);
		
		return new ResponseEntity<>(new ResultStatus("Success","Documents Updated",docRepo.findOneByUserId(userId).get()),HttpStatus.OK);
		
	}
	
	
	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public ResponseEntity<?> upload(@RequestPart(value = "file") MultipartFile file)
			throws IOException, BadRequestException {

		// String path = uploadImage(file);

		if (file.isEmpty()) {
			throw new BadRequestException("no file found");
		}

		byte[] bytes = file.getBytes();
		Path path = Paths.get(ConstApp.getFilePath() + "/" + new Date().getTime() + "-"
				+ file.getOriginalFilename().replace(" ", "_"));

		Files.write(path, bytes);
		
//		mailService.sendMail("upadhyaypremanish@gmail.com", "test doc", "test document", "/task/image/"+path.toString().substring(ConstApp.getFilelength()));
		
		
		return new ResponseEntity<>("/document/image/"+path.toString().substring(ConstApp.getFilelength()), HttpStatus.OK);

	}
	
	
	@GetMapping(value = "/document/image/{imgName}", produces = MediaType.IMAGE_JPEG_VALUE)
	public ResponseEntity<?> getImage(@PathVariable String imgName) throws IOException {
		File file = new File(ConstApp.getFilePath() + imgName);
		if (!file.exists())
			file = new File(ConstApp.getFilePath() + "notfound.jpg");

		if (imgName.substring(imgName.length() - 4, imgName.length()).equalsIgnoreCase(".jpg")
				|| imgName.substring(imgName.length() - 4, imgName.length()).equalsIgnoreCase("jpeg")
				|| imgName.substring(imgName.length() - 4, imgName.length()).equalsIgnoreCase(".png")) {

			if (!file.exists())
				file = new File(ConstApp.getFilePath() + "notfound.jpg");

			FileInputStream imgFile = new FileInputStream(file);

			byte[] bytes = StreamUtils.copyToByteArray(imgFile);

			return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);

		}
		
		else if(imgName.substring(imgName.length() - 4, imgName.length()).equalsIgnoreCase(".pdf")) {

			if (!file.exists())
				file = new File(ConstApp.getFilePath() + "notfound.jpg");

			FileInputStream imgFile = new FileInputStream(file);

			byte[] bytes = StreamUtils.copyToByteArray(imgFile);

			return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(bytes);

		}


		else {

			FileInputStream exclFile = new FileInputStream(file);

			ByteArrayInputStream in = new ByteArrayInputStream(StreamUtils.copyToByteArray(exclFile));

			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Disposition", "attachment; filename=" + imgName);

			return ResponseEntity.ok().headers(headers).body(new InputStreamResource(in));

		}

	}

}

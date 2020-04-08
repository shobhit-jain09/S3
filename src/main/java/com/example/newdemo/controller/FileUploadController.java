package com.example.newdemo.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.newdemo.request.CreateRequest;
import com.example.newdemo.service.FileService;


@RestController
@RequestMapping("/file")
public class FileUploadController {
	
	@Autowired
	private FileService fileService;
	
	
	@PostMapping(value ="/uploadFile")
	  public String uploadFile(
	      @RequestParam(name = "file") MultipartFile uploadedfile) {

//	    String extension =
//	        com.google.common.io.Files.getFileExtension(uploadedfile.getOriginalFilename());
//	    if (!fileStorageService.isAllowedFormat(extension)) {
//	      LOG.info(FILE_REQD_MSG);
//	      throw new FileValidationException(FILE_REQD_MSG);
//	    }
	
		String response = fileService.uploadFile(uploadedfile);
		
	    return response;
	  }

	
	@PostMapping("/uploadFileToS3")
    public String uploadFileToS3(@RequestParam("file") MultipartFile file,@RequestBody CreateRequest request) {
		try{
			System.out.println(request.getName());
			fileService.uploadFiles(Arrays.asList(file));
		} catch (Exception e) {
			return "Unable to upload document." + e;
		}
		return "Document uploaded successfully!!";
    }

    @PostMapping("/uploadMultipleFilesToS3")
    public String uploadMultipleFilesToS3(@RequestParam("files") MultipartFile[] files) {
    	try{
			fileService.uploadFiles(Arrays.asList(files));
		} catch (Exception e) {
			return "Unable to upload document." + e;
		}
		return "Document uploaded successfully!!";
    }
    @GetMapping("/downloadfromS3")
    public File downloadfromS3() {
    	File newfile=null;
    	try{
    		newfile=	fileService.download();
    		return newfile;
		} catch (Exception e) {
			return newfile;
		}
		
    }
    
    @GetMapping("/downloadfromS3/base64")
	public Map<String, String > downloadfromS3AsBase64() throws Exception {
		File newfile = null;
		try {
			newfile = fileService.download();
			FileInputStream fileInputStreamReader = new FileInputStream(newfile);
			byte[] bytes = new byte[(int) newfile.length()];
			fileInputStreamReader.read(bytes);
			String encodedBase64 = new String(Base64.getEncoder().encode(bytes));
			Map<String, String> map = new HashMap<String, String>();
			map.put("base64", encodedBase64);
			return map;
		} catch (Exception e) {
			throw e;
		}

	}
    
    
}
